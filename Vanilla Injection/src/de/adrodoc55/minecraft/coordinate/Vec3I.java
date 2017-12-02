package de.adrodoc55.minecraft.coordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Collections2;

/**
 * A three dimensional {@code int} vector, can be used as a coordinate or size.
 *
 * @author Adrodoc55
 */
@Immutable
public class Vec3I implements Cloneable {
  public static final Vec3I SELF = new Vec3I(0, 0, 0);
  public static final Vec3I EAST = new Vec3I(1, 0, 0);
  public static final Vec3I WEST = new Vec3I(-1, 0, 0);
  public static final Vec3I UP = new Vec3I(0, 1, 0);
  public static final Vec3I DOWN = new Vec3I(0, -1, 0);
  public static final Vec3I SOUTH = new Vec3I(0, 0, 1);
  public static final Vec3I NORTH = new Vec3I(0, 0, -1);
  private static final Collection<Vec3I> DIRECTIONS = new ArrayList<Vec3I>(6);

  static {
    DIRECTIONS.add(EAST);
    DIRECTIONS.add(WEST);
    DIRECTIONS.add(UP);
    DIRECTIONS.add(DOWN);
    DIRECTIONS.add(SOUTH);
    DIRECTIONS.add(NORTH);
  }

  public static Optional<Vec3I> min(Collection<Vec3I> elements) {
    return elements.stream().reduce(Vec3I::min);
  }

  public static Optional<Vec3I> max(Collection<Vec3I> elements) {
    return elements.stream().reduce(Vec3I::max);
  }

  public static Vec3I min(Vec3I a, Vec3I b) {
    return getBinaryOperator(Math::min).apply(a, b);
  }

  public static Vec3I max(Vec3I a, Vec3I b) {
    return getBinaryOperator(Math::max).apply(a, b);
  }

  private static BinaryOperator<Vec3I> getBinaryOperator(IntBinaryOperator op) {
    return (a, b) -> {
      int x = op.applyAsInt(a.x, b.x);
      int y = op.applyAsInt(a.y, b.y);
      int z = op.applyAsInt(a.z, b.z);
      return new Vec3I(x, y, z);
    };
  }

  public final int x;
  public final int y;
  public final int z;

  public Vec3I() {
    this(0);
  }

  public Vec3I(int side) {
    this(side, side, side);
  }

  public Vec3I(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3I(Vec3I other) {
    this(other.x, other.y, other.z);
  }

  public Vec3I copy() {
    return new Vec3I(this);
  }

  @Override
  public Vec3I clone() {
    try {
      return (Vec3I) super.clone();
    } catch (CloneNotSupportedException ex) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError(ex);
    }
  }

  public Vec3D to3D() {
    return new Vec3D(x, y, z);
  }

  /**
   * Returns the value of {@link #x}.
   *
   * @return the value of {@link #x}
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the value of {@link #y}.
   *
   * @return the value of {@link #y}
   */
  public int getY() {
    return y;
  }

  /**
   * Returns the value of {@link #z}.
   *
   * @return the value of {@link #z}
   */
  public int getZ() {
    return z;
  }

  public Vec3I plus(int x, int y, int z) {
    return new Vec3I(this.x + x, this.y + y, this.z + z);
  }

  public Vec3I plus(Vec3I other) {
    return plus(other.x, other.y, other.z);
  }

  public Vec3I minus(int x, int y, int z) {
    return new Vec3I(this.x - x, this.y - y, this.z - z);
  }

  public Vec3I minus(Vec3I other) {
    return minus(other.x, other.y, other.z);
  }

  public int get(Axis3 axis) {
    return axis.of(this);
  }

  public int get(Direction3 d) {
    int value = get(d.getAxis());
    if (d.isNegative()) {
      return -value;
    } else {
      return value;
    }
  }

  public Vec3I plus(int scalar, Direction3 direction) {
    scalar = direction.isNegative() ? -scalar : scalar;
    return plus(scalar, direction.getAxis());
  }

  public Vec3I plus(int scalar, Axis3 axis) {
    return axis.plus(this, scalar);
  }

  public Vec3I minus(int scalar, Direction3 direction) {
    scalar = direction.isNegative() ? -scalar : scalar;
    return minus(scalar, direction.getAxis());
  }

  public Vec3I minus(int scalar, Axis3 axis) {
    return plus(-scalar, axis);
  }

  /**
   * Simple scalar multiplication.
   *
   * @param scalar to multiply this with
   * @return a copy of {@code this} {@link Vec3I} that is multiplied with the scalar
   */
  public Vec3I mult(int scalar) {
    int x = this.x * scalar;
    int y = this.y * scalar;
    int z = this.z * scalar;
    return new Vec3I(x, y, z);
  }

  public String toAbsoluteString() {
    return x + " " + y + " " + z;
  }

  public String toRelativeString() {
    return "~" + toStringNoZero(x) + " ~" + toStringNoZero(y) + " ~" + toStringNoZero(z);
  }

  private String toStringNoZero(int d) {
    if (d == 0) {
      return "";
    }
    return String.valueOf(d);
  }

  public Collection<Vec3I> getAdjacent() {
    return Collections2.transform(DIRECTIONS, this::plus);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(z);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Vec3I other = (Vec3I) obj;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
      return false;
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
      return false;
    if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Vec3I [x=" + x + ", y=" + y + ", z=" + z + "]";
  }
}
