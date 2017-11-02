package de.adrodoc55.minecraft.coordinate;

/**
 * @author Adrodoc55
 */
public enum Axis3 {
  X {
    @Override
    public Direction3 getDirection(boolean negative) {
      return negative ? Direction3.WEST : Direction3.EAST;
    }

    @Override
    public double of(Coordinate3D coordinate) {
      return coordinate.x;
    }

    @Override
    public int of(Coordinate3I coordinate) {
      return coordinate.x;
    }

    @Override
    Coordinate3D plus(Coordinate3D c, double scalar) {
      double x = c.x + scalar;
      return new Coordinate3D(x, c.y, c.z);
    }

    @Override
    Coordinate3I plus(Coordinate3I c, int scalar) {
      int x = c.x + scalar;
      return new Coordinate3I(x, c.y, c.z);
    }
  },
  Y {
    @Override
    public Direction3 getDirection(boolean negative) {
      return negative ? Direction3.DOWN : Direction3.UP;
    }

    @Override
    public double of(Coordinate3D c) {
      return c.y;
    }

    @Override
    public int of(Coordinate3I c) {
      return c.y;
    }

    @Override
    Coordinate3D plus(Coordinate3D c, double scalar) {
      double y = c.y + scalar;
      return new Coordinate3D(c.x, y, c.z);
    }

    @Override
    Coordinate3I plus(Coordinate3I c, int scalar) {
      int y = c.y + scalar;
      return new Coordinate3I(c.x, y, c.z);
    }
  },
  Z {
    @Override
    public Direction3 getDirection(boolean negative) {
      return negative ? Direction3.NORTH : Direction3.SOUTH;
    }

    @Override
    public double of(Coordinate3D c) {
      return c.z;
    }

    @Override
    public int of(Coordinate3I c) {
      return c.z;
    }

    @Override
    Coordinate3D plus(Coordinate3D c, double scalar) {
      double z = c.z + scalar;
      return new Coordinate3D(c.x, c.y, z);
    }

    @Override
    Coordinate3I plus(Coordinate3I c, int scalar) {
      int z = c.z + scalar;
      return new Coordinate3I(c.x, c.y, z);
    }
  };

  public abstract Direction3 getDirection(boolean negative);

  public abstract double of(Coordinate3D c);

  public abstract int of(Coordinate3I coordinate3i);

  abstract Coordinate3D plus(Coordinate3D c, double scalar);

  abstract Coordinate3I plus(Coordinate3I c, int scalar);
}
