package org.hyperagents.yggdrasil.cartago.blocksworld;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Table {

  // Singleton instance
  private static Table instance;

  // Map to hold the blocks
  private final Map<String, Block> blocks;
  private final Map<Position, List<String>> columns;

  private Table() {
    blocks = Map.ofEntries(
      Map.entry("A", new Block(Position.LEFT, 0, true)),
      Map.entry("B", new Block(Position.CENTER, 0, true)),
      Map.entry("C", new Block(Position.RIGHT, 0, true))
    );

    columns = Map.ofEntries(
      Map.entry(Position.LEFT, new LinkedList<>() {{
        add("A");
      }}),
      Map.entry(Position.CENTER, new LinkedList<>() {{
        add("B");
      }}),
      Map.entry(Position.RIGHT, new LinkedList<>() {{
        add("C");
      }})
    );
  }

  public String getCurrentState() {
    final var stringBuilder = new StringBuilder();
    columns.keySet()
      .forEach(position -> stringBuilder
        .append(position.name())
        .append(": ")
        .append(String.join(",", columns.get(position)))
        .append("\n"));
    return stringBuilder.toString();
  }

  public static Table getInstance() {
    if (instance == null) {
      instance = new Table();
    }
    return instance;
  }

  public boolean isCorrectOrder() {
    final var strings = columns.get(Position.CENTER);
    if (strings.size() == 3) {
      return strings.get(0).equals("A") && strings.get(1).equals("B") && strings.get(2).equals("C");
    }
    return false;
  }

  public boolean moveBlock(String blockName, Position newPosition) {
    final var block = blocks.get(blockName);
    final var oldPosition = block.position;

    if (block.isMoveable) {
      List<String> strings = columns.get(newPosition);
      // set block at new position & index
      int i = strings.size();
      block.position = newPosition;
      block.index = i;

      //manipulate columns
      strings.addLast(blockName);
      columns.get(oldPosition).removeLast();

      // set block below as not moveable
      if (i != 0) {
        String s = strings.get(i - 1);
        blocks.get(s).isMoveable = false;
      }
      return true;
    }
    return false;
  }

  private static final class Block {
    private Position position;
    private int index;
    private boolean isMoveable;

    Block(Position position, int index, boolean isMoveable) {
      this.position = position;
      this.index = index;
    }
  }
}
