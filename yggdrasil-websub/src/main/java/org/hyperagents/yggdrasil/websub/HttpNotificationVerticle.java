package org.hyperagents.yggdrasil.websub;

import com.google.common.net.HttpHeaders;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperagents.yggdrasil.eventbus.messageboxes.CartagoMessagebox;
import org.hyperagents.yggdrasil.eventbus.messageboxes.HttpNotificationDispatcherMessagebox;
import org.hyperagents.yggdrasil.eventbus.messages.CartagoMessage;
import org.hyperagents.yggdrasil.eventbus.messages.HttpNotificationDispatcherMessage;
import org.hyperagents.yggdrasil.model.Environment;
import org.hyperagents.yggdrasil.utils.EnvironmentConfig;
import org.hyperagents.yggdrasil.utils.HttpInterfaceConfig;
import org.hyperagents.yggdrasil.utils.WebSubConfig;

import java.util.regex.Pattern;

public class HttpNotificationVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(HttpNotificationVerticle.class);

  private NotificationSubscriberRegistry registry;

  @SuppressWarnings({"PMD.SwitchStmtsShouldHaveDefault", "PMD.SwitchDensity"})
  @Override
  public void start() {
    this.registry = new NotificationSubscriberRegistry();
    final var client = WebClient.create(this.vertx);
    final var notificationConfig = this.vertx
      .sharedData()
      .<String, WebSubConfig>getLocalMap("notification-config")
      .get("default");

    final var environmentConfig = this.vertx
      .sharedData()
      .<String, EnvironmentConfig>getLocalMap("environment-config")
      .get("default");

    final var webSubHubUri = notificationConfig.getWebSubHubUri();
    final var httpConfig =
        this.vertx.sharedData()
                  .<String, HttpInterfaceConfig>getLocalMap("http-config")
                  .get("default");
    this.vertx
        .sharedData()
        .<String, Environment>getLocalMap("environment")
        .get("default")
        .getWorkspaces()
        .forEach(w -> w.getArtifacts()
                       .forEach(a -> a.getFocusingAgents()
                                      .forEach(ag -> this.registry.addCallbackIri(
                                        httpConfig.getArtifactUri(w.getName(), a.getName()),
                                        ag.getCallback()
                                      ))
                       )
        );

    final var cartagoMessagebox = new CartagoMessagebox(
      vertx.eventBus(),
      environmentConfig
    );

    final var ownMessagebox = new HttpNotificationDispatcherMessagebox(
        this.vertx.eventBus(),
        notificationConfig
    );
    ownMessagebox.init();
    ownMessagebox.receiveMessages(message -> {
      switch (message.body()) {
        case HttpNotificationDispatcherMessage.AddCallback(String requestIri, String callbackIri) -> {
          this.registry.addCallbackIri(requestIri, callbackIri);

          String[] segments = requestIri.split("/");
          String workspaceName = null;
          String artifactName = null;
          for (int i = 0; i < segments.length; i++) {
            if ("workspaces".equals(segments[i]) && i + 1 < segments.length) {
              workspaceName = segments[i + 1];
            }
            if ("artifacts".equals(segments[i]) && i + 1 < segments.length) {
              artifactName = segments[i + 1];
            }
          }
          if(artifactName == null) {
            return;
          }
          LOGGER.info("Sending focus message to: " + callbackIri);
          cartagoMessagebox.sendMessage(new CartagoMessage.Focus(
            "http://localhost:8080/agents/llm-agent",
            workspaceName,
            artifactName
          )).onComplete(ar -> {
            if (ar.succeeded()) {
              LOGGER.info("Focus message sent successfully");
            } else {
              LOGGER.error("Failed to send focus message: " + ar.cause().getMessage());
            }
          });
        }
        case HttpNotificationDispatcherMessage.RemoveCallback(
            String requestIri,
            String callbackIri
          ) -> this.registry.removeCallbackIri(requestIri, callbackIri);
        case HttpNotificationDispatcherMessage.EntityDeleted m -> {
          final var entityIri = m.requestIri();
          this.registry.getCallbackIris(entityIri).forEach(c ->
              this.createNotificationRequest(client, webSubHubUri, c, entityIri)
                  .send(this.reponseHandler(c))
          );
        }
        case HttpNotificationDispatcherMessage.ArtifactObsPropertyUpdated(
            String requestIri,
            String content
          ) -> this.handleNotificationSending(
            client,
            webSubHubUri,
            requestIri,
            Pattern.compile(
              "https?://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]"
              )
              .matcher(content)
              .replaceAll(r ->
                  content.charAt(r.start() - 1) == '"' && content.charAt(r.end()) == '"'
                  ? r.group()
                  : "\"" + r.group() + "\""
              )
          );
        case HttpNotificationDispatcherMessage.EntityCreated(String requestIri, String content) ->
          this.handleNotificationSending(client, webSubHubUri, requestIri, content);
        case HttpNotificationDispatcherMessage.EntityChanged(String requestIri, String content) ->
          this.handleNotificationSending(client, webSubHubUri, requestIri, content);
        case HttpNotificationDispatcherMessage.ActionFailed(String requestIri, String content) ->
          this.handleActionNotificationSending(
            client,
            webSubHubUri,
            requestIri,
            content,
            "actionFailed"
          );
        case HttpNotificationDispatcherMessage.ActionRequested(String requestIri, String content) ->
          this.handleActionNotificationSending(
            client,
            webSubHubUri,
            requestIri,
            content,
            "actionRequested"
          );
        case HttpNotificationDispatcherMessage.ActionSucceeded(String requestIri, String content) ->
          this.handleActionNotificationSending(
            client,
            webSubHubUri,
            requestIri,
            content,
            "actionSucceeded"
          );
      }
    });
  }

  private void handleActionNotificationSending(
      final WebClient client,
      final String webSubHubUri,
      final String requestIri,
      final String content,
      final String eventType
  ) {
    this.registry.getCallbackIris(requestIri).forEach(c ->
        this.createNotificationRequest(client, webSubHubUri, c, requestIri)
            .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(content.length()))
            .sendJsonObject(
              ((JsonObject) Json.decodeValue(content)).put("eventType", eventType),
              this.reponseHandler(c)
            )
    );
  }

  private void handleNotificationSending(
      final WebClient client,
      final String webSubHubUri,
      final String requestIri,
      final String content
  ) {
    this.registry.getCallbackIris(requestIri).forEach(c ->
        this.createNotificationRequest(client, webSubHubUri, c, requestIri)
            .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(content.length()))
            .sendBuffer(Buffer.buffer(content), this.reponseHandler(c))
    );
  }

  private HttpRequest<Buffer> createNotificationRequest(
      final WebClient client,
      final String webSubHubUri,
      final String callbackIri,
      final String entityIri
  ) {
    return client.postAbs(callbackIri)
                 .putHeader("Link", "<" + webSubHubUri + ">; rel=\"hub\"")
                 .putHeader("Link", "<" + entityIri + ">; rel=\"self\"");
  }

  private Handler<AsyncResult<HttpResponse<Buffer>>> reponseHandler(final String callbackIri) {
    return ar -> {
      final var response = ar.result();
      if (response == null) {
        LOGGER.info(
            "Failed to send notification to: "
            + callbackIri
            + ", operation failed: "
            + ar.cause().getMessage()
        );
      } else if (response.statusCode() == HttpStatus.SC_OK) {
        LOGGER.info(
            "Notification sent to: "
            + callbackIri
            + ", status code: "
            + response.statusCode()
        );
      } else {
        LOGGER.info(
            "Failed to send notification to: "
            + callbackIri
            + ", status code: "
            + response.statusCode()
        );
      }
    };
  }
}
