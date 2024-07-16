package org.hyperagents.yggdrasil.cartago.blocksworld;

import cartago.OPERATION;
import cartago.OpFeedbackParam;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;

@SuppressWarnings("unused")
public class TableArtifact extends HypermediaArtifact {
  private static final String PREFIX = "http://example.org/";
  private static final String TABLE_TYPE = PREFIX + "TableArtifact";

  private final static String OBS_PROP_TABLE = "Table";

  public void init() {
    this.defineObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());
  }

  @OPERATION
  public void checkTable(final OpFeedbackParam<String> isSorted) {
    this.log("checking table");
    final var obsProperty = this.getObsProperty(OBS_PROP_TABLE).stringValue();
    isSorted.set(obsProperty);
    this.log("table result: " + isSorted.get());
  }

  @Override
  protected void registerInteractionAffordances() {
    this.registerActionAffordance(
      TABLE_TYPE + "#checkTable",
      "checkTable",
      "/checkTable");
    this.registerFeedbackParameter("checkTable");
  }
}

