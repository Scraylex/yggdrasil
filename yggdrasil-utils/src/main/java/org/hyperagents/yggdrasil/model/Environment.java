package org.hyperagents.yggdrasil.model;

import io.vertx.core.shareddata.Shareable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Environment extends Shareable {
  List<Workspace> getWorkspaces();

  Set<KnownArtifact> getKnownArtifacts();
  Optional<String> getTextInfo();

}
