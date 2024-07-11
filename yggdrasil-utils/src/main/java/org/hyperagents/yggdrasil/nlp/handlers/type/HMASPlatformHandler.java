package org.hyperagents.yggdrasil.nlp.handlers.type;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.hyperagents.yggdrasil.nlp.IdentUtil;
import org.hyperagents.yggdrasil.nlp.KnownPredicates;
import org.hyperagents.yggdrasil.nlp.RdfTypeHandler;

import java.util.stream.Collectors;

public class HMASPlatformHandler implements RdfTypeHandler {
  @Override
  public String handleStatement(final Model model,
                                final Resource subject,
                                final int indentLevel) {
    final var stringBuilder = new StringBuilder();
    final var formatted = "%s is a Hypermedia Multi Agent System platform".formatted(subject.stringValue());
    stringBuilder.append(IdentUtil.identString(indentLevel, formatted));
    stringBuilder.append("\n");
//    handleWorkspacesInHMASPlatform(model, subject, indentLevel, stringBuilder);
    handleHostsWorkspaces(model, subject, indentLevel, stringBuilder);
    return stringBuilder.toString();
  }

  private static void handleHostsWorkspaces(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
    final var list = model.filter(subject, KnownPredicates.HMAS_HOSTS.getIRI(), null).stream().toList();
    if (!list.isEmpty()) {
      final var collect = list.stream()
        .map(statement -> statement.getObject().stringValue())
        .collect(Collectors.joining(","));
      final var str = "Hosts the following workspaces: %s".formatted(collect);
      stringBuilder.append(IdentUtil.identString(indentLevel, str));
    }
  }

//  private static void handleWorkspacesInHMASPlatform(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
//    final var list = model.filter(subject, KnownPredicates.HMAS_WORKSPACE.getIRI(), null).stream().toList();
//    if (!list.isEmpty()) {
//      final var str = "\nHosts the following workspaces:\n";
//      stringBuilder.append(IdentUtil.identString(indentLevel, str));
//      list.stream()
//        .map(statement -> RdfTypeHandlerRegistry.WORKSPACE_HANDLER.handleStatement(model, statement.getSubject(), indentLevel + 1))
//        .forEach(stringBuilder::append);
//    } else {
//      final var s = "\nDoes not contain any workspaces.";
//      stringBuilder.append(IdentUtil.identString(indentLevel, s));
//    }
//  }
}
