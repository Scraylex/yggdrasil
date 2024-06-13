package org.hyperagents.yggdrasil.representation;

import edu.umd.cs.findbugs.annotations.Nullable;

public enum ResourceRepresentation {
  PLAIN_TEXT("text/plain"),
  TURTLE("text/turtle"),
  ALL("*/*")
  ;

  private final String mediaType;

  ResourceRepresentation(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getMediaType() {
    return mediaType;
  }

  @Nullable
  public static ResourceRepresentation fromMediaType(String mediaType) {
    for (ResourceRepresentation representation : ResourceRepresentation.values()) {
      if (representation.getMediaType().equals(mediaType)) {
        return representation;
      }
    }
    return null;
  }
}
