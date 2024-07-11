package org.hyperagents.yggdrasil.nlp.handlers;

import org.eclipse.rdf4j.model.*;
import org.hyperagents.yggdrasil.nlp.RdfPredicateHandler;

import java.util.List;
import java.util.stream.StreamSupport;

public class ActionAffordanceHandler implements RdfPredicateHandler {
  @Override
  public String handleStatement(final Model model,
                                final Resource subject,
                                final int indentLevel) {
    final var typeList = model.filter(subject, null, null).stream().map(Statement::getObject)
      .map(Value::stringValue)
      .toList();
    return String.format("%s %s %s", subject.stringValue(),
        typeList.size() > 1 ? "is of types" : "is of type",
        String.join(", ", typeList));
    }
  }
