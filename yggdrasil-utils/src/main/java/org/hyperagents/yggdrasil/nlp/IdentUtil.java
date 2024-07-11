package org.hyperagents.yggdrasil.nlp;

public class IdentUtil {

  public static String identString(final int indentLevel, String message) {
    return "%s%s".formatted("  ".repeat(indentLevel), message);
  }
}
