package org.hyperagents.yggdrasil.eventbus.messageboxes;

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.function.Consumer;

public interface Messagebox<M> {

  void init();

  Future<Message<String>> sendMessage(M message);

  /**
   * This allows to set headers on messages which can in turn be processed by the receiver verticle.
   * This can be used for content negotation and similar mechanisms
   * @param message
   * @param options
   * @return
   */
  default Future<Message<String>> sendMessage(M message, DeliveryOptions options) {
    return this.sendMessage(message);
  }

  void receiveMessages(Consumer<Message<M>> messageHandler);
}
