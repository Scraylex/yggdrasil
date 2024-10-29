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
    if (blocks.isEmpty()) {
      return "EMPTY";
    } else {
      String s = blocks.removeLast();
      Block block = getBlocks().get(s);
      block.setOnTable(false);
      block.setMoveable(false);
      block.setPosition(Position.EMPTY);
      block.setIndex(-1);
      if (!blocks.isEmpty()) {
        String string = blocks.getLast();
        Block block1 = getBlocks().get(string);
        block1.setMoveable(true);
      }
      return s;
    }
  }

  public String putDown(Position position, String block) {
    List<String> blocks = getColumns().get(position);
    final var idx = blocks.size();
    blocks.add(block);
    Block b = getBlocks().get(block);
    b.setMoveable(true);
    b.setOnTable(true);
    b.setPosition(position);
    if (idx > 0) {
      b.setIndex(blocks.size() - 1);
      //handle block below
      String string = blocks.get(idx - 1);
      Block block1 = getBlocks().get(string);
      block1.setMoveable(false);
    } else {
      b.setIndex(0);
    }
    return "";
  }
}
