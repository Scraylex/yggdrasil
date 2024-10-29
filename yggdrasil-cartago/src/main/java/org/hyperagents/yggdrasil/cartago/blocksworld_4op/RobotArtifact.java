package org.hyperagents.yggdrasil.cartago.blocksworld_4op;

import cartago.*;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;
import org.hyperagents.yggdrasil.cartago.blocksworld_shared.ActionResult;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@ARTIFACT_INFO(outports = {
  @OUTPORT(name = "table"),
})
public class RobotArtifact extends HypermediaArtifact {

  Set<String> BLOCK_SET = Set.of("A", "B", "C");
  private static final String PREFIX = "http://example.org/";
  private static final String ROBOT_TYPE = PREFIX + "RobotArtifact";

  private static final String EMPTY = "EMPTY";

  private static final String HAND_EMPTY = "isHolding";
  private static final String HOLDING_BLOCK = "holdingBlock";
  private boolean isHolding = false;
  private String heldBlock = EMPTY;

  private static final String ROBOT_DESCRIPTION = """
    A robot that can move blocks around a table. The robot can move blocks A, B, and C to three different positions LEFT, CENTER, RIGHT.
    A block can only be moved if there is no block above it otherwise the operation fails. This is done by two operations pickup and putdown.
    pickup operation picks up a block from a given position and putdown operation puts the block down to a given position. They are invoked as follows
    # Examples
    Useage examples of the possible operations supplied by this artifact.
    ## Pickup
    Input: 'doAction %s/pickup CENTER'
    Output: 'Block A picked up from CENTER'
    Input: 'doAction %s/pickup LEFT'
    Output: 'Block B picked up from LEFT'
    ## Putdown
    Input: 'doAction %s/putdown CENTER'
    Output: 'Block A putdown to CENTER'
    Input: 'doAction %s/putdown RIGHT'
    Output: 'Block B putdown to RIGHT'
    # Observations
    The robot can be observed to see if it is holding a block and which block it is holding.
    It supplies both 'isHolding(true)' and 'holdingBlock(A)' observations.
    # Invariants
    - The robot can only hold one block at a time.
    - The robot can only putdown a block if it is holding one.
    - the robot can only pickup a block if it is not holding one.
    - the robot can only pick or place a block in the LEFT, CENTER or RIGHT position.
    - Placing a block on a position will make the block below it unmovable until the block is picked up.
    """;

  public void init() {
    this.defineObsProperty(HAND_EMPTY, isHolding);
    this.defineObsProperty(HOLDING_BLOCK, heldBlock);
  }

  @OPERATION
  public void pickup(String position, OpFeedbackParam<ActionResult> moved) throws OperationException {
    final var pos = Position.fromString(position);
    if (pos == null) {
      moved.set(new ActionResult(false, "Invalid position " + position));
      return;
    }
    if (isHolding) {
      moved.set(new ActionResult(false, "Robot is already holding a block"));
      return;
    }
    this.log("picking up block from" + position);
    String s = Table.getInstance().pickUp(pos);
    if (s.equals(EMPTY)) {
      moved.set(new ActionResult(false, "No block to pick up from " + pos));
    } else {
      isHolding = true;
      heldBlock = s;
      updateObsProperty(HAND_EMPTY, isHolding);
      updateObsProperty(HOLDING_BLOCK, heldBlock);
      ArtifactId artifactId = this.lookupArtifact("table");
      execLinkedOp(artifactId, "checkTable", moved);
      moved.set(new ActionResult(true, "Block " + s + " picked up from " + pos));
    }
  }

  @OPERATION
  public void putdown(String position, OpFeedbackParam<ActionResult> moved) throws OperationException {
    final var pos = Position.fromString(position);
    if (pos == null) {
      moved.set(new ActionResult(false, "Invalid position " + position));
      return;
    }
    this.log("putting down block " + heldBlock + " on " + position);
    final var block = Table.getInstance().getBlocks().get(heldBlock);
    if (!isHolding || block == null) {
      moved.set(new ActionResult(false, "No block to put down"));
    } else {
      Table.getInstance().putDown(pos, heldBlock);
      isHolding = false;
      heldBlock = EMPTY;
      updateObsProperty(HAND_EMPTY, isHolding);
      updateObsProperty(HOLDING_BLOCK, heldBlock);
      ArtifactId artifactId = this.lookupArtifact("table");
      execLinkedOp(artifactId, "checkTable", moved);
      moved.set(new ActionResult(true, "Block " + heldBlock + " putdown to " + position));
    }
  }

//  @OPERATION
//  public void stack(String blockA, String blockB, OpFeedbackParam<ActionResult> moved) {
//
//  }
//
//  @OPERATION
//  public void unstack(String blockA, String blockB, OpFeedbackParam<ActionResult> moved) {
//
//  }

  @Override
  protected void registerInteractionAffordances() {
    this.registerActionAffordance(
      ROBOT_TYPE + "#pickup",
      "pickup",
      "/pickup"
      , new ArraySchema.Builder()
        .addItem(new StringSchema.Builder()
          .addEnum(Arrays.stream(Position.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet()))
          .build())
        .build()
    );
    this.registerFeedbackParameter("pickup");

    this.registerActionAffordance(
      ROBOT_TYPE + "#putdown",
      "putdown",
      "/putdown"
      , new ArraySchema.Builder()
        .addItem(new StringSchema.Builder()
          .addEnum(Arrays.stream(new Position[]{Position.LEFT, Position.CENTER, Position.RIGHT})
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet()))
          .build())
        .build()
    );
    this.registerFeedbackParameter("putdown");

//    this.registerActionAffordance(
//      ROBOT_TYPE + "#stack",
//      "stack",
//      "/stack"
//      , new ArraySchema.Builder()
//        .addItem(new StringSchema.Builder()
//          .addEnum(BLOCK_SET)
//          .build())
//        .addItem(new StringSchema.Builder()
//          .addEnum(BLOCK_SET)
//          .build())
//        .build()
//    );
//    this.registerFeedbackParameter("stack");
//
//    this.registerActionAffordance(
//      ROBOT_TYPE + "#unstack",
//      "unstack",
//      "/unstack"
//      , new ArraySchema.Builder()
//        .addItem(new StringSchema.Builder()
//          .addEnum(BLOCK_SET)
//          .build())
//        .addItem(new StringSchema.Builder()
//          .addEnum(BLOCK_SET)
//          .build())
//        .build()
//    );
//    this.registerFeedbackParameter("unstack");

    final var model = RdfModelUtils.createCommentModel(getArtifactUri(), ROBOT_DESCRIPTION.formatted(getArtifactUri(), getArtifactUri(), getArtifactUri(), getArtifactUri()));

    this.addMetadata(model);
  }
}
