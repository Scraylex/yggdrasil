package org.hyperagents.yggdrasil.nlp;

public enum KnownNamespaces {
    HMAS("https://purl.org/hmas/"),
    RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    TD("https://www.w3.org/2019/wot/td#"),
  ;

    private final String namespace;

  KnownNamespaces(String namespace) {
    this.namespace = namespace;
  }

  public String getNamespace() {
    return namespace;
  }
}
