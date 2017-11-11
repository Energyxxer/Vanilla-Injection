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
 * A three dimensional {@code double} coordinate.
 *
 * @author Adrodoc55
 */
@Immutable
public class Coordinate3D implements Cloneable {
  public static final Coordinate3D SELF = new Coordinate3D(0, 0, 0);
  public static final Coordinate3D EAST = new Coordinate3D(1, 0, 0);
  public static final Coordinate3D WEST = new Coordinate3D(-1, 0, 0);
  public static final Coordinate3D UP = new Coordinate3D(0, 1, 0);
  public static final Coordinate3D DOWN = new Coordinate3D(0, -1, 0);
  public static final Coordinate3D SOUTH = new Coordinate3D(0, 0, 1);
  public static final Coordinate3D NORTH = new Coordinate3D(0, 0, -1);
  private static final Collection<Coordinate3D> DIRECTIONS = new ArrayList<Coordinate3D>(6);

  static {
    DIRECTIONS.add(EAST);
    DIRECTIONS.add(WEST);
    DIRECTIONS.add(UP);
    DIRECTIONS.add(DOWN);
    DIRECTIONS.add(SOUTH);
    DIRECTIONS.add(NORTH);
  }

  public static Optional<Coordinate3D> min(Collection<Coordinate3D> coordinates) {
    return coordinates.stream().reduce(Coordinate3D::min);
  }

  public static Optional<Coordinate3D> max(Collection<Coordinate3D> coordinates) {
    return coordinates.stream().reduce(Coordinate3D::max);
  }

  public static Coordinate3D min(Coordinate3D a, Coordinate3D b) {
    return getBinaryOperator(Math::min).apply(a, b);
  }

  public static Coordinate3D max(Coordinate3D a, Coordinate3D b) {
    return getBinaryOperator(Math::max).apply(a, b);
  }

  private static BinaryOperator<Coordinate3D> getBinaryOperator(DoubleBinaryOperator op) {
    return (a, b) -> {
      double x = op.applyAsDouble(a.x, b.x);
      double y = op.applyAsDouble(a.y, b.y);
      double z = op.applyAsDouble(a.z, b.z);
      return new Coordinate3D(x, y, z);
    };
  }

  public final double x;
  public final double y;
  public final double z;

  public Coordinate3D() {
    this(0);
  }

  public Coordinate3D(double side) {
    this(side, side, side);
  }

  public Coordinate3D(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Coordinate3D(Coordinate3D other) {
    this(other.x, other.y, other.z);
  }

  public Coordinate3D copy() {
    return new Coordinate3D(this);
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException ex) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError(ex);
    }
  }

  public Coordinate3I ceil() {
    return round(RoundingMode.CEILING);
  }

  public Coordinate3I floor() {
    return round(RoundingMode.FLOOR);
  }

  public Coordinate3I round(RoundingMode mode) {
    int x = roundToInt(this.x, mode);
    int y = roundToInt(this.y, mode);
    int z = roundToInt(this.z, mode);
    return new Coordinate3I(x, y, z);
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

  public Coordinate3D plus(Coordinate3D other) {
    double x = this.x + other.x;
    double y = this.y + other.y;
    double z = this.z + other.z;
    return new Coordinate3D(x, y, z);
  }

  public Coordinate3D minus(Coordinate3D other) {
    double x = this.x - other.x;
    double y = this.y - other.y;
    double z = this.z - other.z;
    return new Coordinate3D(x, y, z);
  }

  public double get(Axis3 axis) {
    return axis.of(this);
  }

  public double get(Direction3 d) {
    double value = get(d.getAxis());
    if (d.isNegative()) {
      return -value;
    } else {
      return value;
    }
  }

  public Coordinate3D plus(double scalar, Direction3 direction) {
    scalar = direction.isNegative() ? -scalar : scalar;
    return plus(scalar, direction.getAxis());
  }

  public Coordinate3D plus(double scalar, Axis3 axis) {
    return axis.plus(this, scalar);
  }

  public Coordinate3D minus(double scalar, Direction3 direction) {
    scalar = direction.isNegative() ? -scalar : scalar;
    return minus(scalar, direction.getAxis());
  }

  public Coordinate3D minus(double scalar, Axis3 axis) {
    return plus(-scalar, axis);
  }

  /**
   * Simple scalar multiplication.
   *
   * @param scalar to multiply this with
   * @return a copy of this coordinate that is multiplied with the scalar
   */
  public Coordinate3D mult(double scalar) {
    double x = this.x * scalar;
    double y = this.y * scalar;
    double z = this.z * scalar;
    return new Coordinate3D(x, y, z);
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

  public Collection<Coordinate3D> getAdjacent() {
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
    Coordinate3D other = (Coordinate3D) obj;
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
    return "Coordinate3D [x=" + x + ", y=" + y + ", z=" + z + "]";
  }
}
