package org.hyperagents.yggdrasil.cartago.blocksworld_4op;

import org.hyperagents.yggdrasil.cartago.blocksworld_shared.ITable;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Table implements ITable {

  // Singleton instance
  private static Table instance;

  // Map to hold the blocks
  private final Map<String, Block> blocks;
  private final Map<Position, List<String>> columns;

  protected Table() {
    this.blocks = Map.ofEntries(
      Map.entry("A", new Block(Position.LEFT, 0, true, true)),
      Map.entry("B", new Block(Position.CENTER, 0, true, true)),
      Map.entry("C", new Block(Position.RIGHT, 0, true, true))
    );

    this.columns = Map.ofEntries(
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

  public Map<String, Block> getBlocks() {
    return blocks;
  }

  public Map<Position, List<String>> getColumns() {
    return columns;
  }

  public String getCurrentState() {
    final var stringBuilder = new StringBuilder();
    getColumns().keySet()
      .stream()
      .sorted(Comparator.comparing(Enum::name))
      .forEach(position -> stringBuilder
        .append("|")
        .append(position.name())
        .append(": ")
        .append(getColumns().get(position).isEmpty()
          ? "EMPTY" : String.join(",", getColumns().get(position)))
        .append("|"));
    return stringBuilder.toString();
  }

  public boolean isCorrectOrder() {
    final var strings = getColumns().get(Position.CENTER);
    if (strings.size() == 3) {
      return strings.get(0).equals("A") && strings.get(1).equals("B") && strings.get(2).equals("C");
    }
    return false;
  }

  public static Table getInstance() {
    if (instance == null) {
      instance = new Table();
    }
    return instance;
  }


  public String pickUp(Position position) {
    List<String> blocks = getColumns().get(position);
    if (blocks.size() > 1) {
      throw new IllegalStateException("Cannot pick up a block from a position with more than one block");
    } else {
      String s = blocks.removeLast();
      Block block = getBlocks().get(s);
      block.setOnTable(false);
      block.setMoveable(false);
      block.setPosition(Position.EMPTY);
      block.setIndex(-1);
      return s;
    }
  }

  public String putDown(Position position, String block) {
    List<String> blocks = getColumns().get(position);
    if(!blocks.isEmpty()) {
      throw new IllegalStateException("Cannot put block down on a non-empty position");
    }
    blocks.add(block);
    Block b = getBlocks().get(block);
    b.setMoveable(true);
    b.setOnTable(true);
    b.setPosition(position);
    b.setIndex(0);
    return block;
  }

  public boolean stack(String blockA, String blockB) {
    Block b = getBlocks().get(blockB);
    Position position = b.getPosition();
    getColumns().get(position).add(blockA);
    b.setMoveable(false);

    Block a = getBlocks().get(blockA);
    a.setMoveable(true);
    a.setIndex(b.getIndex() + 1);
    a.setPosition(position);
    a.setOnTable(true);
    return true;
  }

  public boolean unstack(String blockA, String blockB) {
    Block b = getBlocks().get(blockB);
    b.setMoveable(true);
    Position position = b.getPosition();

    Block a = getBlocks().get(blockA);
    getColumns().get(position).remove(blockA);
    a.setMoveable(false);
    a.setIndex(-1);
    a.setPosition(Position.EMPTY);
    a.setOnTable(false);
    return true;
  }
}
