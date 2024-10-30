package org.hyperagents.yggdrasil.cartago.blocksworld_2op;

public final class Block {
  private Position position;
  private int index;
  private boolean isMoveable;
  private boolean isOnTable;

  public Block(Position position, int index, boolean isMoveable, boolean isOnTable) {
    this.position = position;
    this.index = index;
    this.isMoveable = isMoveable;
    this.isOnTable = isOnTable;
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

  public boolean isOnTable() {
    return isOnTable;
  }

  public void setOnTable(boolean onTable) {
    isOnTable = onTable;
  }
}
