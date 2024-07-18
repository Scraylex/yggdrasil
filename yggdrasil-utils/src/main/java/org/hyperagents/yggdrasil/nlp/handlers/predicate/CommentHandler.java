package org.hyperagents.yggdrasil.nlp.handlers.predicate;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.hyperagents.yggdrasil.nlp.IdentUtil;
import org.hyperagents.yggdrasil.nlp.RdfTypeHandler;

public class CommentHandler implements RdfTypeHandler {
  @Override
  public String handleStatement(final Model model,
                                final Resource subject,
                                final int indentLevel) {
    final var stringBuilder = new StringBuilder();
    Model filtered = model.filter(subject, SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2000/01/rdf-schema#comment"), null);
    filtered.forEach(statement -> {
      stringBuilder.append(IdentUtil.identString(indentLevel, statement.getObject().stringValue()));
      stringBuilder.append("\n");
    });
    return stringBuilder.toString();
  }
}
