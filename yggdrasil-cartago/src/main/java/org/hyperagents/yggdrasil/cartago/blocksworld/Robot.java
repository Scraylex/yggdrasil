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
public class Robot extends HypermediaArtifact {

  private static final String PREFIX = "http://example.org/";
  private static final String ROBOT_TYPE = PREFIX + "Robot";

  private static final String ROBOT_DESCRIPTION = """
    A robot that can move blocks around a table. The robot can move blocks A, B, and C to three different positions LEFT, CENTER, RIGHT.
    A block can only be moved if there is no block above it otherwise the operation fails.
    example: doAction %s/moveBlock A CENTER
    example: doAction %s/moveBlock C CENTER
    """;

  public void init() {
  }

  @OPERATION
  public void moveBlock(String blockName, String position, OpFeedbackParam<String> moved) {
    this.log("moving block " + blockName + " to " + position);
    boolean b = Table.getInstance().moveBlock(blockName, Position.valueOf(position));
    final String response;
    if(b) {
      response = "Block %s moved to %s".formatted(blockName, position);
    } else {
      response = "Block %s could not be moved to %s".formatted(blockName, position);
      this.log(response);
      moved.set(response);
      return;
    }
    moved.set(response);
    this.log("moved");
  }

  @Override
  protected void registerInteractionAffordances() {
    this.registerActionAffordance(
      ROBOT_TYPE + "#moveBlock",
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

    final var model = RdfModelUtils.createCommentModel(getArtifactUri(), ROBOT_DESCRIPTION.formatted(getArtifactUri(), getArtifactUri()));

    this.addMetadata(model);
  }
}
