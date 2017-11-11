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
  EAST(Vec3I.EAST, false, Axis3.X), //
  WEST(Vec3I.WEST, true, Axis3.X), //
  UP(Vec3I.UP, false, Axis3.Y), //
  DOWN(Vec3I.DOWN, true, Axis3.Y), //
  SOUTH(Vec3I.SOUTH, false, Axis3.Z), //
  NORTH(Vec3I.NORTH, true, Axis3.Z), //
  ;
  private static final ImmutableList<Direction3> VALUES = ImmutableList.copyOf(values());

  public static ImmutableList<Direction3> getValues() {
    return VALUES;
  }

  private static final ImmutableMap<Vec3D, Direction3> INDEX_3D =
      Maps.uniqueIndex(VALUES, Direction3::toVec3D);

  public static Direction3 valueOf(Vec3D vec) {
    checkNotNull(vec, "vec == null!");
    Direction3 result = INDEX_3D.get(vec);
    if (result != null) {
      return result;
    }
    throw new IllegalArgumentException("No enum constant for " + vec);
  }

  private static final ImmutableMap<Vec3I, Direction3> INDEX_3I =
      Maps.uniqueIndex(VALUES, Direction3::toVec3I);

  public static Direction3 valueOf(Vec3I vec) {
    checkNotNull(vec, "vec == null!");
    Direction3 result = INDEX_3I.get(vec);
    if (result != null) {
      return result;
    }
    throw new IllegalArgumentException("No enum constant for " + vec);
  }

  public static Direction3 valueOf(Axis3 axis, boolean negative) {
    return axis.getDirection(negative);
  }

  private final Vec3I relative;
  private final boolean negative;
  private final Axis3 axis;

  private Direction3(Vec3I relative, boolean negative, Axis3 axis) {
    this.relative = checkNotNull(relative, "relative == null!");
    this.negative = negative;
    this.axis = axis;
  }

  public Vec3I toVec3I() {
    return relative;
  }

  public Vec3D toVec3D() {
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
