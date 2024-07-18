package org.hyperagents.yggdrasil.nlp.handlers.predicate;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.hyperagents.yggdrasil.nlp.IdentUtil;
import org.hyperagents.yggdrasil.nlp.KnownPredicates;
import org.hyperagents.yggdrasil.nlp.RdfTypeHandler;
import org.hyperagents.yggdrasil.nlp.handlers.type.ActionAffordanceHandler;

public class HasActionAffordanceHandler implements RdfTypeHandler {
  @Override
  public String handleStatement(final Model model,
                                final Resource subject,
                                final int indentLevel) {
    final var stringBuilder = new StringBuilder();
    Model filtered = model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/td#hasActionAffordance"), null);
    filtered.forEach(statement -> stringBuilder.append(new ActionAffordanceHandler().handleStatement(model, (Resource) statement.getObject(), indentLevel)));
    return stringBuilder.toString();
  }
}
