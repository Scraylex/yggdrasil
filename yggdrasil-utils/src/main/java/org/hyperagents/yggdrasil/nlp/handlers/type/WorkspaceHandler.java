package org.hyperagents.yggdrasil.nlp.handlers.type;

import org.eclipse.rdf4j.model.*;
import org.hyperagents.yggdrasil.nlp.IdentUtil;
import org.hyperagents.yggdrasil.nlp.KnownPredicates;
import org.hyperagents.yggdrasil.nlp.RdfTypeHandler;

import java.util.stream.Collectors;

public class WorkspaceHandler implements RdfTypeHandler {
  @Override
  public String handleStatement(final Model model,
                                final Resource subject,
                                final int indentLevel) {
    final var stringBuilder = new StringBuilder();
    final var formatted = "%s is a Cartago workspace".formatted(subject.stringValue());
    stringBuilder.append(IdentUtil.identString(indentLevel, formatted));
    stringBuilder.append("\n");
//    handleWorkspacesInHMASPlatform(model, subject, indentLevel, stringBuilder);
    handleHostsWorkspaces(model, subject, indentLevel, stringBuilder);
    return stringBuilder.toString();
  }

  private static void handleHostsWorkspaces(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
    final var list = model.filter(subject, KnownPredicates.HMAS_CONTAINS.getIRI(), null).stream().toList();
    if (!list.isEmpty()) {
      final var collect = list.stream()
        .map(statement -> statement.getObject().stringValue())
        .collect(Collectors.joining(", "));
      final var str = "Contains the following artifacts: %s".formatted(collect);
      stringBuilder.append(IdentUtil.identString(indentLevel, str));
    }
  }
}
