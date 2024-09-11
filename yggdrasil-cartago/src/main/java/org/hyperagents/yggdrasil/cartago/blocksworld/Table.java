package org.hyperagents.yggdrasil.cartago.blocksworld;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Table {

  // Singleton instance
  private static Table instance;

  // Map to hold the blocks
  private final Map<String, Block> blocks;
  private final Map<Position, List<String>> columns;

  private Table() {
    this.blocks = Map.ofEntries(
      Map.entry("A", new Block(Position.LEFT, 0, true)),
      Map.entry("B", new Block(Position.CENTER, 0, true)),
      Map.entry("C", new Block(Position.RIGHT, 0, true))
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

  public String getCurrentState() {
    final var stringBuilder = new StringBuilder();
    Table.getInstance().getColumns().keySet()
      .stream()
      .sorted(Comparator.comparing(Enum::name))
      .forEach(position -> stringBuilder
        .append("|")
        .append(position.name())
        .append(": ")
        .append(Table.getInstance().getColumns().get(position).isEmpty()
          ? "EMPTY" : String.join(",", Table.getInstance().getColumns().get(position)))
        .append("|"));
    return stringBuilder.toString();
  }

  public static Table getInstance() {
    if (instance == null) {
      instance = new Table();
    }
    return instance;
  }

  public Map<String, Block> getBlocks() {
    return blocks;
  }

  public Map<Position, List<String>> getColumns() {
    return columns;
  }

  public boolean isCorrectOrder() {
    final var strings = Table.getInstance().getColumns().get(Position.CENTER);
    if (strings.size() == 3) {
      return strings.get(0).equals("A") && strings.get(1).equals("B") && strings.get(2).equals("C");
    }
    return false;
  }

  public boolean moveBlock(String blockName, Position newPosition) {
    final var block = Table.getInstance().getBlocks().get(blockName);
    final var oldPosition = block.getPosition();

    if (block.isMoveable()) {
      List<String> strings = Table.getInstance().getColumns().get(newPosition);
      // set block at new position & index
      int i = strings.size();
      block.setPosition(newPosition);
      block.setIndex(i);
      strings.addLast(blockName);

      //handle old column and blocks
      Table.getInstance().getColumns().get(oldPosition).removeLast();
      if(!Table.getInstance().getColumns().get(oldPosition).isEmpty()) {
        String oldColumnLastBlock = Table.getInstance().getColumns().get(oldPosition).getLast();
        Table.getInstance().getBlocks().get(oldColumnLastBlock).setMoveable(true);
      }

      // handle new column and block
      if (i != 0) {
        String s = strings.get(i - 1);
        Table.getInstance().getBlocks().get(s).setMoveable(false);
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
      this.isMoveable = isMoveable;
    }

    public Position getPosition() {
      return position;
    }

    public void setPosition(Position position) {
      this.position = position;
    }

    public int getIndex() {
      return index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public boolean isMoveable() {
      return isMoveable;
    }

    public void setMoveable(boolean moveable) {
      isMoveable = moveable;
    }
  }
}
