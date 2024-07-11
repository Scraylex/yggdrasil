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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
      System.out.println("Received message: "+ message);
      // manipulation to deal with llm
      String cleanedMsg = message.replace("'", "");

      if (message.contains("goto")) {
        handleGoTo(socket, cleanedMsg);
      } else if (message.contains("doAction")) {
        handleDoAction(socket, cleanedMsg);
      } else if (message.contains("query")) {
        handleSparqlQuery(socket, cleanedMsg);
      } else {
        handleUnmappedGrammar(socket, cleanedMsg);
      }
    };
  }

  private void handleUnmappedGrammar(ServerWebSocket socket, String message) {
    System.out.println("Unmapped grammar: "+ message);
    socket.writeTextMessage("I'm sorry, I don't understand. Please use the following grammar: 'goto <entity>', 'doAction <action> <param1?> <param2?>', 'query <sparql query>'.");
  }

  private void handleSparqlQuery(ServerWebSocket socket, String message) {
    String query = message.split("query")[1].trim();
    rdfStoreMessagebox.sendMessage(new RdfStoreMessage.QueryKnowledgeGraph(query, List.of(), List.of(), "text/turtle"))
      .onComplete(messageAsyncResult -> {
        if (messageAsyncResult.succeeded()) {
//          final var body = messageAsyncResult.result().body();
//          try {
//            final var targetIri = RdfModelUtils.createIri(target);
//            final var model = RdfModelUtils.stringToModel(body, targetIri, RDFFormat.TURTLE);
//            System.out.println(("Model: {}", model);
//            final var responseString = RdfToNaturalLanguageConverter.modelResourceToNaturalLanguageString(model, targetIri);
//            System.out.println(("Response: {}", responseString);
//            socket.writeTextMessage(responseString);
//          } catch (IOException e) {
//            final var string = "Failed to serialize model.";
//            LOGGER.error(string, e);
//            socket.writeTextMessage(string);
//          }
          socket.writeTextMessage("Todo: implement query handling.");
        } else {
          socket.writeTextMessage("Query failed");
        }
      });

  }

  private void handleDoAction(ServerWebSocket socket, String message) {
    final var args = message.split(" ");
    final var targetAction = args[0];
    final var params = Arrays.stream(args)
      .skip(1)
      .toList();
    System.out.println("invoking action {} with params {}"+ targetAction + " " + String.join(", ", params));
    socket.writeTextMessage("Todo: implement action handling.");
//    cartagoMessagebox.sendMessage(new CartagoMessage.DoAction()) TODO


//    rdfStoreMessagebox.sendMessage(new RdfStoreMessage.(target))
//      .onComplete(messageAsyncResult -> {
//        if (messageAsyncResult.succeeded()) {
//          final var body = messageAsyncResult.result().body();
//          try {
//            final var targetIri = RdfModelUtils.createIri(target);
//            final var model = RdfModelUtils.stringToModel(body, targetIri, RDFFormat.TURTLE);
//            System.out.println(("Model: {}", model);
//            final var responseString = RdfToNaturalLanguageConverter.modelResourceToNaturalLanguageString(model, targetIri);
//            System.out.println(("Response: {}", responseString);
//            socket.writeTextMessage(responseString);
//          } catch (IOException e) {
//            final var string = "Failed to serialize model.";
//            LOGGER.error(string, e);
//            socket.writeTextMessage(string);
//          }
//        } else {
//          socket.writeTextMessage("Entity not found.");
//        }
//      });
  }

  private void handleGoTo(ServerWebSocket socket, String message) {
    final var target = message.split("goto")[1].trim();
    System.out.println("Navigating to " + target);
    rdfStoreMessagebox.sendMessage(new RdfStoreMessage.GetEntity(target))
      .onComplete(messageAsyncResult -> {
        if (messageAsyncResult.succeeded()) {
          final var body = messageAsyncResult.result().body();
          try {
            final var targetIri = RdfModelUtils.createIri(target);
            final var model = RdfModelUtils.stringToModel(body, targetIri, RDFFormat.TURTLE);
            System.out.println("Model: " + model);
            final var responseString = RdfToNaturalLanguageConverter.modelResourceToNaturalLanguageString(model, targetIri);
            System.out.println("Response: " + responseString);
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
