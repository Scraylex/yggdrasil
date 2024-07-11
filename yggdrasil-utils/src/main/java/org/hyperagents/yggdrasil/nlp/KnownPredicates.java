package org.hyperagents.yggdrasil.nlp;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public enum KnownPredicates {
  WORKSPACE(KnownNamespaces.HMAS, "Workspace"),
  HOSTS(KnownNamespaces.HMAS, "hosts"),
  HMASPLATFORM(KnownNamespaces.HMAS, "HypermediaMASPlatform"),
  TYPE(KnownNamespaces.RDF, "type"),

  ;

  private final String predicate;
  private final KnownNamespaces namespace;

  KnownPredicates(KnownNamespaces namespace, String predicate) {
    this.namespace = namespace;
    this.predicate = predicate;
  }

  public String getPredicate() {
    return namespace.getNamespace() + predicate;
  }

  public IRI getIRI() {
    return SimpleValueFactory.getInstance().createIRI(getPredicate());
  }
}
