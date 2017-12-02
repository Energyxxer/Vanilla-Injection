package de.adrodoc55.minecraft.coordinate;

/**
 * A three dimensional axis.
 *
 * @author Adrodoc55
 */
public enum Axis3 {
  X {
    @Override
    public Direction3 getDirection(boolean negative) {
      return negative ? Direction3.WEST : Direction3.EAST;
    }

    @Override
    public double of(Vec3D c) {
      return c.x;
    }

    @Override
    public int of(Vec3I c) {
      return c.x;
    }

    @Override
    Vec3D with(Vec3D c, double value) {
      double x = value;
      return new Vec3D(x, c.y, c.z);
    }

    @Override
    Vec3I with(Vec3I c, int value) {
      int x = value;
      return new Vec3I(x, c.y, c.z);
    }

    @Override
    Vec3D plus(Vec3D c, double scalar) {
      double x = c.x + scalar;
      return new Vec3D(x, c.y, c.z);
    }

    @Override
    Vec3I plus(Vec3I c, int scalar) {
      int x = c.x + scalar;
      return new Vec3I(x, c.y, c.z);
    }
  },
  Y {
    @Override
    public Direction3 getDirection(boolean negative) {
      return negative ? Direction3.DOWN : Direction3.UP;
    }

    @Override
    public double of(Vec3D c) {
      return c.y;
    }

    @Override
    public int of(Vec3I c) {
      return c.y;
    }

    @Override
    Vec3D with(Vec3D c, double value) {
      double y = value;
      return new Vec3D(c.x, y, c.z);
    }

    @Override
    Vec3I with(Vec3I c, int value) {
      int y = value;
      return new Vec3I(c.x, y, c.z);
    }

    @Override
    Vec3D plus(Vec3D c, double scalar) {
      double y = c.y + scalar;
      return new Vec3D(c.x, y, c.z);
    }

    @Override
    Vec3I plus(Vec3I c, int scalar) {
      int y = c.y + scalar;
      return new Vec3I(c.x, y, c.z);
    }
  },
  Z {
    @Override
    public Direction3 getDirection(boolean negative) {
      return negative ? Direction3.NORTH : Direction3.SOUTH;
    }

    @Override
    public double of(Vec3D c) {
      return c.z;
    }

    @Override
    public int of(Vec3I c) {
      return c.z;
    }

    @Override
    Vec3D with(Vec3D c, double value) {
      double z = value;
      return new Vec3D(c.x, c.y, z);
    }

    @Override
    Vec3I with(Vec3I c, int value) {
      int z = value;
      return new Vec3I(c.x, c.y, z);
    }

    @Override
    Vec3D plus(Vec3D c, double scalar) {
      double z = c.z + scalar;
      return new Vec3D(c.x, c.y, z);
    }

    @Override
    Vec3I plus(Vec3I c, int scalar) {
      int z = c.z + scalar;
      return new Vec3I(c.x, c.y, z);
    }
  };

  /**
   * Return the appropriate {@link Direction3} along {@code this} {@link Axis3}.
   *
   * @param negative whether to return the positive or negative {@link Direction3}
   * @return the appropriate {@link Direction3}
   */
  public abstract Direction3 getDirection(boolean negative);

  /**
   * Return the positive {@link Direction3} along {@code this} {@link Axis3}.
   *
   * @return the positive {@link Direction3}
   */
  public Direction3 getPositiveDirection() {
    return getDirection(false);
  }

  /**
   * Return the negative {@link Direction3} along {@code this} {@link Axis3}.
   *
   * @return the negative {@link Direction3}
   */
  public Direction3 getNegativeDirection() {
    return getDirection(true);
  }

  /**
   * Return the extend of the specified {@link Vec3D} along {@code this}{@link Axis3}.
   *
   * @param c the {@link Vec3D}
   * @return the extend of the {@link Vec3D}
   */
  public abstract double of(Vec3D c);

  /**
   * Return the extend of the specified {@link Vec3I} along {@code this}{@link Axis3}.
   *
   * @param c the {@link Vec3I}
   * @return the extend of the {@link Vec3I}
   */
  public abstract int of(Vec3I c);

  abstract Vec3D with(Vec3D c, double value);

  abstract Vec3I with(Vec3I c, int value);

  abstract Vec3D plus(Vec3D c, double scalar);

  abstract Vec3I plus(Vec3I c, int scalar);
}
