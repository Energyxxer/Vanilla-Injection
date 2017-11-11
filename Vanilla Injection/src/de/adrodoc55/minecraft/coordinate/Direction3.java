package de.adrodoc55.minecraft.coordinate;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @author Adrodoc55
 */
public enum Direction3 {
  EAST(Coordinate3I.EAST, false, Axis3.X), //
  WEST(Coordinate3I.WEST, true, Axis3.X), //
  UP(Coordinate3I.UP, false, Axis3.Y), //
  DOWN(Coordinate3I.DOWN, true, Axis3.Y), //
  SOUTH(Coordinate3I.SOUTH, false, Axis3.Z), //
  NORTH(Coordinate3I.NORTH, true, Axis3.Z), //
  ;
  private static final ImmutableList<Direction3> VALUES = ImmutableList.copyOf(values());

  public static ImmutableList<Direction3> getValues() {
    return VALUES;
  }

  private static final ImmutableMap<Coordinate3D, Direction3> INDEX_3D =
      Maps.uniqueIndex(VALUES, Direction3::toCoordinate3D);

  public static Direction3 valueOf(Coordinate3D coordinate) {
    checkNotNull(coordinate, "coordinate == null!");
    Direction3 result = INDEX_3D.get(coordinate);
    if (result != null) {
      return result;
    }
    throw new IllegalArgumentException("No enum constant for coordinate " + coordinate);
  }

  private static final ImmutableMap<Coordinate3I, Direction3> INDEX_3I =
      Maps.uniqueIndex(VALUES, Direction3::toCoordinate3I);

  public static Direction3 valueOf(Coordinate3I coordinate) {
    checkNotNull(coordinate, "coordinate == null!");
    Direction3 result = INDEX_3I.get(coordinate);
    if (result != null) {
      return result;
    }
    throw new IllegalArgumentException("No enum constant for coordinate " + coordinate);
  }

  public static Direction3 valueOf(Axis3 axis, boolean negative) {
    return axis.getDirection(negative);
  }

  private final Coordinate3I relative;
  private final boolean negative;
  private final Axis3 axis;

  private Direction3(Coordinate3I relative, boolean negative, Axis3 axis) {
    this.relative = relative;
    this.negative = negative;
    this.axis = axis;
  }

  public Coordinate3I toCoordinate3I() {
    return relative;
  }

  public Coordinate3D toCoordinate3D() {
    return relative.to3D();
  }

  public boolean isPositive() {
    return !negative;
  }

  public boolean isNegative() {
    return negative;
  }

  public Axis3 getAxis() {
    return axis;
  }

  @Override
  public String toString() {
    return UPPER_UNDERSCORE.to(LOWER_CAMEL, super.toString());
  }
}
