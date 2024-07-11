package org.hyperagents.yggdrasil.nlp.handlers.type;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.hyperagents.yggdrasil.nlp.IdentUtil;
import org.hyperagents.yggdrasil.nlp.KnownPredicates;
import org.hyperagents.yggdrasil.nlp.RdfTypeHandler;

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
//    handleWorkspacesInHMASPlatform(model, subject, indentLevel, stringBuilder);
    handleHasActionAffordances(model, subject, indentLevel, stringBuilder);
    handleHasSecurityConfiguration(model, subject, indentLevel, stringBuilder);
    handleIsContainedIn(model, subject, indentLevel, stringBuilder);
    return stringBuilder.toString();
  }

  private void handleIsContainedIn(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
  }

  private void handleHasSecurityConfiguration(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {

  }

  private void handleHasActionAffordances(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
//    final var filteredModel = model.filter(subject, KnownPredicates.TD_ACTION_AFFORDANCE.getIRI(), null);
//    final var collect = list.stream()
//      .map(statement -> statement.getObject().stringValue())
//      .collect(Collectors.joining(","));
//    final var str = "Contains the following artifacts: %s".formatted(collect);
//    stringBuilder.append(IdentUtil.identString(indentLevel, str));
  }
}
