package org.hyperagents.yggdrasil.representation;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hyperagents.yggdrasil.nlp.RdfToNaturalLanguageConverter;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceRepresentationHandler {

  public String handleRepresentation(final List<String> acceptHeaders,
                                     final Model result, IRI requestIri) throws IOException {
    final var representations = acceptHeaders.stream()
      .map(ResourceRepresentation::fromMediaType)
      .collect(Collectors.toSet());
    if (representations.contains(ResourceRepresentation.PLAIN_TEXT)) {
      return RdfToNaturalLanguageConverter.modelResourceToNaturalLanguageString(result, requestIri);
    }
    return RdfModelUtils.modelToString(result, RDFFormat.TURTLE);
  }
}
