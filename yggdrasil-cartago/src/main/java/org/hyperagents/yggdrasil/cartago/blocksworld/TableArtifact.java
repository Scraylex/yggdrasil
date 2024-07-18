package org.hyperagents.yggdrasil.cartago.blocksworld;

import cartago.OPERATION;
import cartago.OpFeedbackParam;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

@SuppressWarnings("unused")
public class TableArtifact extends HypermediaArtifact {
  private static final String PREFIX = "http://example.org/";
  private static final String TABLE_TYPE = PREFIX + "TableArtifact";

  private final static String OBS_PROP_TABLE = "Table";

  private final static String TABLE_DESCRIPTION = """
    A table with three blocks A, B, and C. The blocks can be moved to three different positions LEFT, CENTER, RIGHT.
    To move the blocks the adjacent robot has to be used. The table can be observed to see the current state of the blocks.
    example: doAction %s/checkTable
    """;

  public void init() {
    this.defineObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());

  }

  @OPERATION
  public void checkTable(final OpFeedbackParam<String> isSorted) {
    this.log("checking table");
    String currentState = Table.getInstance().getCurrentState();
    this.updateObsProperty(OBS_PROP_TABLE, currentState);
    isSorted.set(currentState);
    this.log(isSorted.get());
  }

  @Override
  protected void registerInteractionAffordances() {
    final var model = RdfModelUtils.createCommentModel(getArtifactUri(), TABLE_DESCRIPTION.formatted(getArtifactUri()));

    this.registerActionAffordance(
      TABLE_TYPE + "#checkTable",
      "checkTable",
      "/checkTable");
    this.registerFeedbackParameter("checkTable");
    this.addMetadata(model);
  }
}

