package org.hyperagents.yggdrasil.cartago.blocksworld_4op;

import cartago.*;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;
import org.hyperagents.yggdrasil.cartago.blocksworld_shared.ActionResult;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@ARTIFACT_INFO(outports = {
  @OUTPORT(name = "table"),
})
public class RobotArtifact extends HypermediaArtifact {

  Set<String> BLOCK_SET = Set.of("A", "B", "C");
  Position[] POSITION_SET = {Position.LEFT, Position.CENTER, Position.RIGHT};
  private static final String PREFIX = "http://example.org/";
  private static final String ROBOT_TYPE = PREFIX + "RobotArtifact";

  private static final String EMPTY = "EMPTY";

  private static final String HAND_EMPTY = "isHolding";
  private static final String HOLDING_BLOCK = "holdingBlock";
  private boolean isHolding = false;
  private String heldBlock = EMPTY;

  private static final String ROBOT_DESCRIPTION = """
    A robot that can move blocks around a table. The robot can move blocks A, B, and C to three different positions LEFT, CENTER, RIGHT.
    A block can only be moved by four operations pickup, putdown, stack and unstack.
    pickup operation picks up a block from a given position if there is only a single block and putdown operation puts the block down to a given position if the position is empty.
    The stack operation places a block on top of another block while the unstack operation removes a block from another block. They are invoked as follows
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
    ## Stack
    The block to stack on top is the first argument and the block to stack on is the second argument.
    Input: 'doAction %s/stack A B'
    Output: 'Block A stacked on B'
    Input: 'doAction %s/stack C B'
    Output: 'Block C stacked on B'
    ## Unstack
    The block to unstack and hold is the first argument and the block to unstack and make moveable is the second argument.
    Input: 'doAction %s/unstack A C'
    Output: 'Block A unstacked from C
    Input: 'doAction %s/unstack BLOCK_A BLOCK_B'
    Output: 'Block BLOCK_A unstacked from BLOCK_B'
    # Observations
    The robot can be observed to see if it is holding a block and which block it is holding.
    It supplies both 'isHolding(true)' and 'holdingBlock(A)' observations.
    # Invariant Conditions
    - The robot can only hold one block at a time.
    - The robot can only putdown a block if it is holding one and the position is 'EMPTY'.
    - the robot can only pickup a block if it is not holding one and that is the only block at the position.
    - the robot can only pick or place a block in the LEFT, CENTER or RIGHT position if it is the only block on the position.
    - Stacking a block on another block will make the block below unmovable until the block is unstacked and remove the block from the robot hand.
    - Unstacking a block from another block will make the block below moveable and place the block into the robot hand.
    - pickup action is only supported if there is only a single block in the position and no block is currently held
    - putdown action is only supported if there is no block in the position so the position is EMPTY and a block is currently held
    - stack is only supported when there is a block already in the position and a block is currently held
    - unstack is only supported when there are at least two blocks in the position and no block is currently held
    - stack and unstack require 2 blocks as arguements
    - pickup and putdown require a single position as arguement
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
    Table instance = Table.getInstance();
    if (instance.getColumns().get(pos).size() > 1) {
      moved.set(new ActionResult(false, "Cannot pick up a block from a position with more than one block"));
      return;
    }
    this.log("picking up block from" + position);
    String s = instance.pickUp(pos);
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
    Table instance = Table.getInstance();
    final var block = instance.getBlocks().get(heldBlock);
    if (!isHolding || block == null) {
      moved.set(new ActionResult(false, "No block to put down"));
    } else if (!instance.getColumns().get(pos).isEmpty()) {
      moved.set(new ActionResult(false, "Cannot put block down on a non EMPTY position "));
    } else {
      instance.putDown(pos, heldBlock);
      final var tempName = heldBlock;
      isHolding = false;
      heldBlock = EMPTY;
      updateObsProperty(HAND_EMPTY, isHolding);
      updateObsProperty(HOLDING_BLOCK, heldBlock);
      ArtifactId artifactId = this.lookupArtifact("table");
      execLinkedOp(artifactId, "checkTable", moved);
      moved.set(new ActionResult(true, "Block " + tempName + " putdown to " + position));
    }
  }

  @OPERATION
  public void stack(String blockA, String blockB, OpFeedbackParam<ActionResult> moved) throws OperationException {
    Table instance = Table.getInstance();
    Block block = instance.getBlocks().get(blockB);
    List<String> strings = instance.getColumns().get(block.getPosition());
    if (strings.isEmpty()) {
      moved.set(new ActionResult(false, "Cannot stack a block on an empty position"));
      return;
    }
    if (!isHolding) {
      moved.set(new ActionResult(false, "Cannot stack a block if not holding one"));
      return;
    }
    if(!heldBlock.equals(blockA)) {
      moved.set(new ActionResult(false, "Cannot stack a block that is not held"));
      return;
    }
    if (!block.isMoveable()) {
      moved.set(new ActionResult(false, "Cannot stack block " + blockA + " on not moveable block " + blockB));
      return;
    }
    this.log("stacking " + blockA + " on " + blockB);
    boolean s = instance.stack(blockA, blockB);
    isHolding = false;
    heldBlock = EMPTY;
    updateObsProperty(HAND_EMPTY, isHolding);
    updateObsProperty(HOLDING_BLOCK, heldBlock);
    ArtifactId artifactId = this.lookupArtifact("table");
    execLinkedOp(artifactId, "checkTable", moved);
    moved.set(new ActionResult(true, "block " + blockA + " stacked on " + blockB));
  }

  @OPERATION
  public void unstack(String blockA, String blockB, OpFeedbackParam<ActionResult> moved) throws OperationException {
    Table instance = Table.getInstance();
    Block block = instance.getBlocks().get(blockB);
    List<String> strings = instance.getColumns().get(block.getPosition());
    if (strings.isEmpty()) {
      moved.set(new ActionResult(false, "Cannot unstack a block at a empty position"));
      return;
    }
    if (strings.size() == 1) {
      moved.set(new ActionResult(false, "Cannot unstack a block without one ontop"));
      return;
    }
    if (isHolding) {
      moved.set(new ActionResult(false, "Cannot unstack a block while holding one"));
      return;
    }
    if (!instance.getBlocks().get(blockA).getPosition().equals(instance.getBlocks().get(blockB).getPosition())) {
      moved.set(new ActionResult(false, "blocks to unstack are not in the same position"));
      return;
    }

    this.log("unstacking " + blockA + " from " + blockB);
    boolean s = instance.unstack(blockA, blockB);
    isHolding = true;
    heldBlock = blockA;
    updateObsProperty(HAND_EMPTY, isHolding);
    updateObsProperty(HOLDING_BLOCK, heldBlock);
    ArtifactId artifactId = this.lookupArtifact("table");
    execLinkedOp(artifactId, "checkTable", moved);
    moved.set(new ActionResult(true, "block " + blockA + " unstacked from " + blockB));
  }

  @Override
  protected void registerInteractionAffordances() {
    this.registerActionAffordance(
      ROBOT_TYPE + "#pickup",
      "pickup",
      "/pickup"
      , new ArraySchema.Builder()
        .addItem(new StringSchema.Builder()
          .addEnum(Arrays.stream(POSITION_SET)
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
          .addEnum(Arrays.stream(POSITION_SET)
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet()))
          .build())
        .build()
    );
    this.registerFeedbackParameter("putdown");

    this.registerActionAffordance(
      ROBOT_TYPE + "#stack",
      "stack",
      "/stack"
      , new ArraySchema.Builder()
        .addItem(new StringSchema.Builder()
          .addEnum(BLOCK_SET)
          .build())
        .addItem(new StringSchema.Builder()
          .addEnum(BLOCK_SET)
          .build())
        .build()
    );
    this.registerFeedbackParameter("stack");

    this.registerActionAffordance(
      ROBOT_TYPE + "#unstack",
      "unstack",
      "/unstack"
      , new ArraySchema.Builder()
        .addItem(new StringSchema.Builder()
          .addEnum(BLOCK_SET)
          .build())
        .addItem(new StringSchema.Builder()
          .addEnum(BLOCK_SET)
          .build())
        .build()
    );
    this.registerFeedbackParameter("unstack");

    final var model = RdfModelUtils.createCommentModel(getArtifactUri(), ROBOT_DESCRIPTION.formatted(getArtifactUri(),
      getArtifactUri(),
      getArtifactUri(),
      getArtifactUri(),
      getArtifactUri(),
      getArtifactUri(),
      getArtifactUri(),
      getArtifactUri()));

    this.addMetadata(model);
  }
}
