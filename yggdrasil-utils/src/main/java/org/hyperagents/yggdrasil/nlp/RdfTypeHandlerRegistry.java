package org.hyperagents.yggdrasil.nlp;

import org.eclipse.rdf4j.model.IRI;
import org.hyperagents.yggdrasil.nlp.handlers.type.ArtifactHandler;
import org.hyperagents.yggdrasil.nlp.handlers.type.HMASPlatformHandler;
import org.hyperagents.yggdrasil.nlp.handlers.type.WorkspaceHandler;

import java.util.HashMap;
import java.util.Map;

public class RdfTypeHandlerRegistry {

  private static final Map<IRI, RdfTypeHandler> handlers = new HashMap<>();

  public static RdfTypeHandler HMAS_WORKSPACE_HANDLER = new WorkspaceHandler();
  public static RdfTypeHandler HMAS_PLATFORM_HANDLER = new HMASPlatformHandler();
  public static RdfTypeHandler HMAS_ARTIFACT_HANDLER = new ArtifactHandler();


  static {
    handlers.put(KnownPredicates.HMAS_WORKSPACE.getIRI(), HMAS_WORKSPACE_HANDLER);
    handlers.put(KnownPredicates.HMAS_PLATFORM.getIRI(), HMAS_PLATFORM_HANDLER);
    handlers.put(KnownPredicates.HMAS_ARTIFACT.getIRI(), HMAS_ARTIFACT_HANDLER);
  }

  public static RdfTypeHandler getHandler(IRI predicateUri) {
    return handlers.get(predicateUri);
  }
}

