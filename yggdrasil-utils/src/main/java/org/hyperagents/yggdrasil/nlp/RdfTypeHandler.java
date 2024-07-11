package org.hyperagents.yggdrasil.nlp;

import org.eclipse.rdf4j.model.*;

public interface RdfTypeHandler {
  String handleStatement(final Model model,
                         final Resource subject,
                         final int indentLevel);
}
