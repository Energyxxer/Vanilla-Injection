package de.adrodoc55.minecraft.coordinate;

import static com.google.common.math.DoubleMath.roundToInt;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Collections2;

/**
 * A three dimensional {@code double} vector, can be used as a coordinate or size.
 *
 * @author Adrodoc55
 */
@Immutable
public class Vec3D implements Cloneable {
  public static final Vec3D SELF = new Vec3D(0, 0, 0);
  public static final Vec3D EAST = new Vec3D(1, 0, 0);
  public static final Vec3D WEST = new Vec3D(-1, 0, 0);
  public static final Vec3D UP = new Vec3D(0, 1, 0);
  public static final Vec3D DOWN = new Vec3D(0, -1, 0);
  public static final Vec3D SOUTH = new Vec3D(0, 0, 1);
  public static final Vec3D NORTH = new Vec3D(0, 0, -1);
  private static final Collection<Vec3D> DIRECTIONS = new ArrayList<Vec3D>(6);

  static {
    DIRECTIONS.add(EAST);
    DIRECTIONS.add(WEST);
    DIRECTIONS.add(UP);
    DIRECTIONS.add(DOWN);
    DIRECTIONS.add(SOUTH);
    DIRECTIONS.add(NORTH);
  }

  public static Optional<Vec3D> min(Collection<Vec3D> elements) {
    return elements.stream().reduce(Vec3D::min);
  }

  public static Optional<Vec3D> max(Collection<Vec3D> elements) {
    return elements.stream().reduce(Vec3D::max);
  }

  public static Vec3D min(Vec3D a, Vec3D b) {
    return getBinaryOperator(Math::min).apply(a, b);
  }

  public static Vec3D max(Vec3D a, Vec3D b) {
    return getBinaryOperator(Math::max).apply(a, b);
  }

  private static BinaryOperator<Vec3D> getBinaryOperator(DoubleBinaryOperator op) {
    return (a, b) -> {
      double x = op.applyAsDouble(a.x, b.x);
      double y = op.applyAsDouble(a.y, b.y);
      double z = op.applyAsDouble(a.z, b.z);
      return new Vec3D(x, y, z);
    };
  }

  public final double x;
  public final double y;
  public final double z;

  public Vec3D() {
    this(0);
  }

  public Vec3D(double side) {
    this(side, side, side);
  }

  public Vec3D(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3D(Vec3D other) {
    this(other.x, other.y, other.z);
  }

  public Vec3D copy() {
    return new Vec3D(this);
  }

  @Override
  public Vec3D clone() {
    try {
      return (Vec3D) super.clone();
    } catch (CloneNotSupportedException ex) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError(ex);
    }
  }

  public Vec3I ceil() {
    return round(RoundingMode.CEILING);
  }

  public Vec3I floor() {
    return round(RoundingMode.FLOOR);
  }

  public Vec3I round(RoundingMode mode) {
    int x = roundToInt(this.x, mode);
    int y = roundToInt(this.y, mode);
    int z = roundToInt(this.z, mode);
    return new Vec3I(x, y, z);
  }

  /**
   * Returns the value of {@link #x}.
   *
   * @return the value of {@link #x}
   */
  public double getX() {
    return x;
  }

  /**
   * Returns the value of {@link #y}.
   *
   * @return the value of {@link #y}
   */
  public double getY() {
    return y;
  }

  /**
   * Returns the value of {@link #z}.
   *
   * @return the value of {@link #z}
   */
  public double getZ() {
    return z;
  }

  public double get(Axis3 axis) {
    return axis.of(this);
  }

  public double get(Direction3 direction) {
    double value = get(direction.getAxis());
    if (direction.isNegative()) {
      value = -value;
    }
    return value;
  }

  public Vec3D withX(double x) {
    return new Vec3D(x, y, z);
  }

  public Vec3D withY(double y) {
    return new Vec3D(x, y, z);
  }

  public Vec3D withZ(double z) {
    return new Vec3D(x, y, z);
  }

  public Vec3D with(Axis3 axis, double value) {
    return axis.with(this, value);
  }

  public Vec3D with(Direction3 direction, double value) {
    Axis3 axis = direction.getAxis();
    if (direction.isNegative()) {
      value = -value;
    }
    return axis.with(this, value);
  }

  public Vec3D plus(double x, double y, double z) {
    return new Vec3D(this.x + x, this.y + y, this.z + z);
  }

  public Vec3D plus(Vec3D other) {
    return plus(other.x, other.y, other.z);
  }

  public Vec3D minus(double x, double y, double z) {
    return new Vec3D(this.x - x, this.y - y, this.z - z);
  }

  public Vec3D minus(Vec3D other) {
    return minus(other.x, other.y, other.z);
  }

  public Vec3D plus(double scalar, Direction3 direction) {
    scalar = direction.isNegative() ? -scalar : scalar;
    return plus(scalar, direction.getAxis());
  }

  public Vec3D plus(double scalar, Axis3 axis) {
    return axis.plus(this, scalar);
  }

  public Vec3D minus(double scalar, Direction3 direction) {
    scalar = direction.isNegative() ? -scalar : scalar;
    return minus(scalar, direction.getAxis());
  }

  public Vec3D minus(double scalar, Axis3 axis) {
    return plus(-scalar, axis);
  }

  /**
   * Simple scalar multiplication.
   *
   * @param scalar to multiply {@code this} with
   * @return a copy of {@code this} {@link Vec3D} that is multiplied with the scalar
   */
  public Vec3D mult(double scalar) {
    double x = this.x * scalar;
    double y = this.y * scalar;
    double z = this.z * scalar;
    return new Vec3D(x, y, z);
  }

  public String toAbsoluteString() {
    return x + " " + y + " " + z;
  }

  public String toRelativeString() {
    return "~" + toStringNoZero(x) + " ~" + toStringNoZero(y) + " ~" + toStringNoZero(z);
  }

  private String toStringNoZero(double d) {
    if (d == 0) {
      return "";
    }
    return String.valueOf(d);
  }

  public Collection<Vec3D> getAdjacent() {
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
    Vec3D other = (Vec3D) obj;
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
    return "Vec3D [x=" + x + ", y=" + y + ", z=" + z + "]";
  }
}
