package org.hyperagents.yggdrasil.nlp;

import org.eclipse.rdf4j.model.IRI;
import org.hyperagents.yggdrasil.nlp.handlers.HMASPlatformHandler;
import org.hyperagents.yggdrasil.nlp.handlers.WorkspaceHandler;

import java.util.HashMap;
import java.util.Map;

public class RdfPredicateHandlerRegistry {

  private static final Map<IRI, RdfPredicateHandler> handlers = new HashMap<>();

public static RdfPredicateHandler WORKSPACE_HANDLER = new WorkspaceHandler();
public static RdfPredicateHandler HMASPLATFORM_HANDLER = new HMASPlatformHandler();


  static {
    handlers.put(KnownPredicates.WORKSPACE.getIRI(), WORKSPACE_HANDLER);
    handlers.put(KnownPredicates.HMASPLATFORM.getIRI(), HMASPLATFORM_HANDLER);
//    handlers.put(valueFactory.createIRI("https://www.w3.org/2019/wot/td#hasActionAffordance"), new ActionAffordanceHandler());
  }

  public static RdfPredicateHandler getHandler(IRI predicateUri) {
    return handlers.get(predicateUri);
  }
}

