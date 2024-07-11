package org.hyperagents.yggdrasil.nlp;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public interface RdfPredicateHandler {
  String handleStatement(final Model model,
                         final Resource subject,
                         final int indentLevel,
                         StringBuilder stringBuilder);
}
