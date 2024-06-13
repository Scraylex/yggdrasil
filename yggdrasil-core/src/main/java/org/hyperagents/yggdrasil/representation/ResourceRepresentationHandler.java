package org.hyperagents.yggdrasil.representation;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceRepresentationHandler {

  public String handleRepresentation(final List<String> acceptHeaders,
                                      final Model result) throws IOException {
    final var representations = acceptHeaders.stream()
      .map(ResourceRepresentation::fromMediaType)
      .collect(Collectors.toSet());
    if (representations.contains(ResourceRepresentation.PLAIN_TEXT)) {
      return RdfModelUtils.modelToNaturalLanguageString(result);
    }
    return RdfModelUtils.modelToString(result, RDFFormat.TURTLE);
  }
}
