package org.hyperagents.yggdrasil.cartago.blocksworld;

import cartago.OPERATION;
import cartago.OpFeedbackParam;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class TableAggregateArtifact extends HypermediaArtifact {
  private static final String PREFIX = "http://example.org/";
  private static final String TABLE_TYPE = PREFIX + "TableArtifact";

  private final static String OBS_PROP_TABLE = "Table";
  private final static String OBS_PROP_CORRECT = "Correct";

  private final static String TABLE_DESCRIPTION = """
    A table with three blocks A, B, and C. The blocks can be moved to three different positions LEFT, CENTER, RIGHT.
    The blocks can only be moved if there is no block above them.
    Focus this artefact to observe the current configuration of the table and get updated after moving a block.
    To move the blocks the inbuilt robot has to be used. The table can be observed to see the current state of the blocks.
    You can either check the current configuration of table with checkTable or move a block with moveBlock.
    ------------------
    action %s/checkTable
    input: doAction %s/checkTable
    output: doAction succeeded with status code 200 and response: CENTER: B
            LEFT: A
            RIGHT: C
    ------------------
    action %s/moveBlock
    input: doAction %s/moveBlock A CENTER
    output: doAction succeeded with status code 200 and response: Block B moved to LEFT. Current state CENTER: \s
            LEFT: A,B
            RIGHT: C
    input: doAction %s/moveBlock C CENTER
    constraint: LEFT: C,B
    output: doAction succeeded with status code 200 and response: Block A could not be moved to CENTER
    """;

  public void init() {
    this.defineObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());
    this.defineObsProperty(OBS_PROP_CORRECT, Table.getInstance().isCorrectOrder());
  }

  @OPERATION
  public void checkTable(final OpFeedbackParam<String> isSorted) {
    this.log("checking table");
    String currentState = Table.getInstance().getCurrentState();
    isSorted.set(currentState);
    this.log(isSorted.get());
  }

  @OPERATION
  public void moveBlock(String blockName, String position, OpFeedbackParam<String> moved) {
    this.log("moving block " + blockName + " to " + position);
    boolean b = Table.getInstance().moveBlock(blockName, Position.valueOf(position));
    final String response;
    if (b) {
      response = "Block %s moved to %s. Current state %s".formatted(blockName, position, Table.getInstance().getCurrentState());
    } else {
      response = "Block %s could not be moved to %s".formatted(blockName, position);
      this.log(response);
      moved.set(response);
      return;
    }
    moved.set(response);
    this.log("moved");
    this.updateObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());

    if(Table.getInstance().isCorrectOrder() != this.getObsProperty(OBS_PROP_CORRECT).booleanValue()) {
      this.updateObsProperty(OBS_PROP_CORRECT, Table.getInstance().isCorrectOrder());
    }
  }


  @Override
  protected void registerInteractionAffordances() {
    this.registerActionAffordance(
      TABLE_TYPE + "#moveBlock",
      "moveBlock",
      "/moveBlock"
      , new ArraySchema.Builder()
        .addItem(new StringSchema.Builder()
          .addEnum(Set.of("A", "B", "C"))
          .build())
        .addItem(new StringSchema.Builder()
          .addEnum(Arrays.stream(Position.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet()))
          .build())
        .build()
    );
    this.registerFeedbackParameter("moveBlock");
    this.registerActionAffordance(
      TABLE_TYPE + "#checkTable",
      "checkTable",
      "/checkTable");
    this.registerFeedbackParameter("checkTable");

    final var model = RdfModelUtils.createCommentModel(getArtifactUri(), TABLE_DESCRIPTION.formatted(getArtifactUri(),
      getArtifactUri(),
      getArtifactUri(), getArtifactUri(),
      getArtifactUri()));

    this.addMetadata(model);
  }
}

