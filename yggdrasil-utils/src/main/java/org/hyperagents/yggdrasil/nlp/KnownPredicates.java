package org.hyperagents.yggdrasil.nlp;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public enum KnownPredicates {
  HMAS_WORKSPACE(KnownNamespaces.HMAS, "Workspace"),
  HMAS_ARTIFACT(KnownNamespaces.HMAS, "Artifact"),
  HMAS_HOSTS(KnownNamespaces.HMAS, "hosts"),
  HMAS_CONTAINS(KnownNamespaces.HMAS, "contains"),
  HMAS_PLATFORM(KnownNamespaces.HMAS, "HypermediaMASPlatform"),
  RDF_TYPE(KnownNamespaces.RDF, "type"),
  TD_ACTION_AFFORDANCE(KnownNamespaces.TD, "ActionAffordance"),

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
