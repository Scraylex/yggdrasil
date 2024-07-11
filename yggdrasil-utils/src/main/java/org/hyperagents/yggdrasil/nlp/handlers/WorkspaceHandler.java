package org.hyperagents.yggdrasil.nlp.handlers;

import org.eclipse.rdf4j.model.*;
import org.hyperagents.yggdrasil.nlp.RdfPredicateHandler;

public class WorkspaceHandler implements RdfPredicateHandler {
  @Override
  public String handleStatement(final Model model,
                                final Resource subject,
                                final int indentLevel) {
    return String.format("%s %s %s", subject.stringValue(), "todo", "todo");
  }
}
