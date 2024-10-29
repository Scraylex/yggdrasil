package org.hyperagents.yggdrasil.cartago.blocksworld_1op;

import cartago.OPERATION;
import cartago.OpFeedbackParam;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;
import org.hyperagents.yggdrasil.cartago.blocksworld_shared.ActionResult;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class TableAggregateArtifact extends HypermediaArtifact {
  private static final String PREFIX = "http://example.org/";
  private static final String TABLE_TYPE = PREFIX + "TableArtifact";

  private final static String OBS_PROP_TABLE = "TableState";
  private final static String OBS_PROP_CORRECT = "TableCorrect";

  private final static String TABLE_DESCRIPTION = """
    A table with three blocks A, B, and C. The blocks can be moved to three different positions LEFT, CENTER, RIGHT.
    Focus this artefact to observe the current configuration of the table and get updated after moving a block.
    You can either check the current configuration of table with checkTable or move a block with moveBlock.
    ------------------
    Invariant:
    - The blocks can only be moved if there is no block above them.
    - The order of blocks is read left to right with the left most being the lowest and right most being on top on the given position.
    ------------------
    Usage Examples:
    ------------------
    Example 1:
    invoked action %s/checkTable
    input: doAction %s/checkTable
    output: '|CENTER: B||LEFT: A||RIGHT: C|
    ------------------
    Example 2:
    invoked action %s/moveBlock
    input: 'doAction %s/moveBlock A CENTER'
    output: 'Block B moved to LEFT.'
    ------------------
    Example 3:
    invoked action %s/moveBlock
    input: 'doAction %s/moveBlock C CENTER'
    output: 'Constraint not satisfied. Block A could not be moved to CENTER because there is block C above it.'
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

  @OPERATION
  public void moveBlock(String blockName, String position, OpFeedbackParam<ActionResult> moved) {
    this.log("moving block " + blockName + " to " + position);
    final var newPosition = Position.valueOf(position);
    final var b = Table.getInstance().moveBlock(blockName, newPosition);
    final String response;
    if (!b) {
      final var strings = Table.getInstance().getColumns().get(newPosition);
      for (int i=0; i < strings.size(); i++) {
        if (strings.get(i).equals(blockName)) {
            response = "Constraint not satisfied. Block %s could not be moved to %s because there is block %s above it.".formatted(blockName, position, strings.get(i + 1));
            this.log(response);
            this.updateObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());
            moved.set(new ActionResult(false, response));
            return;
        }
      }
      response = "Constraint not satisfied. Block %s is not at position %s.".formatted(blockName, position);
      this.log(response);
      this.updateObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());
      moved.set(new ActionResult(false, response));
      return;
    }
    response = "Block %s moved to %s".formatted(blockName, position);
    this.log(response);
    moved.set(new ActionResult(true, response));

    this.updateObsProperty(OBS_PROP_TABLE, Table.getInstance().getCurrentState());

    if (Table.getInstance().isCorrectOrder() != this.getObsProperty(OBS_PROP_CORRECT).booleanValue()) {
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
      getArtifactUri(),
      getArtifactUri(),
      getArtifactUri(),
      getArtifactUri()));

    this.addMetadata(model);
  }
}

