package org.hyperagents.yggdrasil.cartago.blocksworld_1op;

public final class Block {
  private Position position;
  private int index;
  private boolean isMoveable;

  public Block(Position position, int index, boolean isMoveable) {
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
