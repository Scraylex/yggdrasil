package org.hyperagents.yggdrasil.cartago.blocksworld_2op;

import cartago.OPERATION;
import cartago.OpFeedbackParam;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;
import org.hyperagents.yggdrasil.cartago.blocksworld_shared.ActionResult;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

@SuppressWarnings("unused")
public class TableArtifact extends HypermediaArtifact {
  private static final String PREFIX = "http://example.org/";
  private static final String TABLE_TYPE = PREFIX + "TableArtifact";

  private final static String OBS_PROP_TABLE = "TableState";
  private final static String OBS_PROP_CORRECT = "TableCorrect";

  private final static String TABLE_DESCRIPTION = """
    # General
    A table with three blocks A, B, and C. The blocks can be moved to three different positions LEFT, CENTER, RIGHT.
    Focus this artefact to observe the current configuration of the table and get updated after moving a block.
    You can check the current configuration of table with checkTable.
    # Examples
    invoked action %s/checkTable
    input: doAction %s/checkTable
    output: '|CENTER: B||LEFT: A||RIGHT: C|
    """;

  public void init() {
    this.defineObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());
    this.defineObsProperty(OBS_PROP_CORRECT, Table.getInstance().isCorrectOrder());
  }

  @OPERATION
  public void checkTable(final OpFeedbackParam<ActionResult> isSorted) {
    this.log("checking table");
    final var currentState = Table.getInstance().getCurrentState();
    isSorted.set(new ActionResult(true, currentState));
    this.updateObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());
    this.updateObsProperty(OBS_PROP_CORRECT, Table.getInstance().isCorrectOrder());
    this.log(currentState);
  }

  @Override
  protected void registerInteractionAffordances() {
    final var model = RdfModelUtils.createCommentModel(getArtifactUri(), TABLE_DESCRIPTION.formatted(getArtifactUri(), getArtifactUri()));

    this.registerActionAffordance(
      TABLE_TYPE + "#checkTable",
      "checkTable",
      "/checkTable");
    this.registerFeedbackParameter("checkTable");
    this.addMetadata(model);
  }
}

