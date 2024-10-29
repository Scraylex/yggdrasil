package org.hyperagents.yggdrasil.cartago.blocksworld_4op;

public enum Position {
    LEFT,
    RIGHT,
    CENTER,
    EMPTY
    ;

  public static Position fromString(String position) {
    return switch (position) {
      case "LEFT" -> LEFT;
      case "RIGHT" -> RIGHT;
      case "CENTER" -> CENTER;
      default -> null;
    };
  }
}
