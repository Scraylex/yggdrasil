package org.hyperagents.yggdrasil.cartago.blocksworld;

import cartago.OPERATION;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Robot extends HypermediaArtifact {

  private static final String PREFIX = "http://example.org/";
  private static final String ROBOT_TYPE = PREFIX + "Robot";

  public void init() {}

  @OPERATION
  public void moveBlock(String blockName, String position) {
    this.log("moving block " + blockName + " to " + position);
    Table.getInstance().moveBlock(blockName, Position.valueOf(position));
    this.log("moved");
  }

  @Override
  protected void registerInteractionAffordances() {
    this.registerActionAffordance(
      ROBOT_TYPE + "#moveBlock",
      "moveBlock",
      "/moveBlock"
      , new ObjectSchema.Builder()
        .addRequiredProperties("blockName", "position")
        .addProperty("blockName", new StringSchema.Builder()
          .addEnum(Set.of("A", "B", "C"))
          .build())
        .addProperty("position", new StringSchema.Builder()
          .addEnum(Arrays.stream(Position.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet()))
          .build())
        .build()
    );
  }
}
