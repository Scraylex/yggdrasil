package org.hyperagents.yggdrasil.text;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hyperagents.yggdrasil.eventbus.messageboxes.CartagoMessagebox;
import org.hyperagents.yggdrasil.eventbus.messageboxes.HttpNotificationDispatcherMessagebox;
import org.hyperagents.yggdrasil.eventbus.messageboxes.Messagebox;
import org.hyperagents.yggdrasil.eventbus.messageboxes.RdfStoreMessagebox;
import org.hyperagents.yggdrasil.eventbus.messages.CartagoMessage;
import org.hyperagents.yggdrasil.eventbus.messages.HttpNotificationDispatcherMessage;
import org.hyperagents.yggdrasil.eventbus.messages.RdfStoreMessage;
import org.hyperagents.yggdrasil.http.HttpEntityHandler;
import org.hyperagents.yggdrasil.nlp.RdfToNaturalLanguageConverter;
import org.hyperagents.yggdrasil.utils.EnvironmentConfig;
import org.hyperagents.yggdrasil.utils.HttpInterfaceConfig;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;
import org.hyperagents.yggdrasil.utils.WebSubConfig;

import java.io.IOException;

public class TextMessageHandler {

  private static final Logger LOGGER = LogManager.getLogger(HttpEntityHandler.class);
  private static final String WORKSPACE_ID_PARAM = "wkspid";
  private static final String AGENT_WEBID_HEADER = "X-Agent-WebID";

  private final Messagebox<CartagoMessage> cartagoMessagebox;
  private final Messagebox<RdfStoreMessage> rdfStoreMessagebox;
  private final Messagebox<HttpNotificationDispatcherMessage> notificationMessagebox;
  private final HttpInterfaceConfig httpConfig;
  private final WebSubConfig notificationConfig;

  public TextMessageHandler(Vertx vertx,
                            HttpInterfaceConfig httpConfig,
                            EnvironmentConfig environmentConfig,
                            WebSubConfig notificationConfig) {
    this.httpConfig = httpConfig;
    this.notificationConfig = notificationConfig;
    this.cartagoMessagebox = new CartagoMessagebox(
      vertx.eventBus(),
      environmentConfig
    );
    this.rdfStoreMessagebox = new RdfStoreMessagebox(vertx.eventBus());
    this.notificationMessagebox =
      new HttpNotificationDispatcherMessagebox(vertx.eventBus(), this.notificationConfig);
  }


  public Handler<String> handleTextMessage(ServerWebSocket socket) {
    return message -> {
      if (message.contains("go to")) {
        handleGoTo(socket, message);
      }
    };
  }

  private void handleGoTo(ServerWebSocket socket, String message) {
    final var target = message.split("go to")[1].trim();
    LOGGER.info("Navigating to {}", target);
    rdfStoreMessagebox.sendMessage(new RdfStoreMessage.GetEntity(target))
      .onComplete(messageAsyncResult -> {
        if (messageAsyncResult.succeeded()) {
          final var body = messageAsyncResult.result().body();
          try {
            final var targetIri = RdfModelUtils.createIri(target);
            final var model = RdfModelUtils.stringToModel(body, targetIri, RDFFormat.TURTLE);
            LOGGER.info("Model: {}", model);
            final var responseString = RdfToNaturalLanguageConverter.modelResourceToNaturalLanguageString(model, targetIri);
            LOGGER.info("Response: {}", responseString);
            socket.writeTextMessage(responseString);
          } catch (IOException e) {
            final var string = "Failed to serialize model.";
            LOGGER.error(string, e);
            socket.writeTextMessage(string);
          }
        } else {
          socket.writeTextMessage("Entity not found.");
        }
      });
  }
}
