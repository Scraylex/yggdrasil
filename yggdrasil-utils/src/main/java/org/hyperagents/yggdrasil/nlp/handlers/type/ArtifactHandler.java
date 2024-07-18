package org.hyperagents.yggdrasil.nlp.handlers.type;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.hyperagents.yggdrasil.nlp.IdentUtil;
import org.hyperagents.yggdrasil.nlp.KnownPredicates;
import org.hyperagents.yggdrasil.nlp.RdfTypeHandler;
import org.hyperagents.yggdrasil.nlp.handlers.predicate.CommentHandler;
import org.hyperagents.yggdrasil.nlp.handlers.predicate.HasActionAffordanceHandler;

import java.util.stream.Collectors;

public class ArtifactHandler implements RdfTypeHandler {
  @Override
  public String handleStatement(final Model model,
                                final Resource subject,
                                final int indentLevel) {
    final var stringBuilder = new StringBuilder();
    final var formatted = "%s is a Cartago artifact".formatted(subject.stringValue());
    stringBuilder.append(IdentUtil.identString(indentLevel, formatted));
    stringBuilder.append("\n");
//    handleHasActionAffordances(model, subject, indentLevel, stringBuilder);
//    stringBuilder.append("\n");
    handleComment(model, subject, indentLevel, stringBuilder);
    return stringBuilder.toString();
  }

  private void handleHasActionAffordances(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
    final var string = new HasActionAffordanceHandler().handleStatement(model, subject, indentLevel);
    stringBuilder.append(string);
  }

  private static void handleComment(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
    final var string = new CommentHandler().handleStatement(model, subject, indentLevel);
    stringBuilder.append(string);
  }
}
