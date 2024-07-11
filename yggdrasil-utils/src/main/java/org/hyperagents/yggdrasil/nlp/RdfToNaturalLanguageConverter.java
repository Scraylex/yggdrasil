package org.hyperagents.yggdrasil.nlp;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

import java.util.ArrayList;

public class RdfToNaturalLanguageConverter {

  public static String modelResourceToNaturalLanguageString(final Model model, final IRI targetIri) {
    final var subjectRepresentation = new ArrayList<String>();
    model.filter(targetIri, KnownPredicates.TYPE.getIRI(), null)
      .objects()
      .stream()
      .filter(Value::isIRI)
      .map(Value::stringValue)
      .map(RdfModelUtils::createIri)
      .forEach(iri -> {
        RdfPredicateHandler handler = RdfPredicateHandlerRegistry.getHandler(iri);
        if (handler != null) {
          subjectRepresentation.add(handler.handleStatement(model, targetIri, 0));
        }
      });
    return String.join("\n", subjectRepresentation);
  }
}

