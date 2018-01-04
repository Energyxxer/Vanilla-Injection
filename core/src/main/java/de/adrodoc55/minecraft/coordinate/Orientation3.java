package de.adrodoc55.minecraft.coordinate;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.adrodoc55.minecraft.coordinate.Direction3.EAST;
import static de.adrodoc55.minecraft.coordinate.Direction3.SOUTH;
import static de.adrodoc55.minecraft.coordinate.Direction3.UP;

import javax.annotation.concurrent.Immutable;

/**
 * An instance of {@link Orientation3} represents one of the 48 ways you can allign a structure to a
 * grid in three dimensions via rotation and mirroring. An {@link Orientation3} consists of a
 * {@link #primary}, a {@link #secondary} and a {@link #tertiary} {@link Direction3} which are all
 * on different {@link Axis3 axes}.
 *
 * @author Adrodoc55
 */
@Immutable
public class Orientation3 {
  public static final Orientation3 XYZ = new Orientation3(EAST, UP, SOUTH);
  public static final Orientation3 XZY = new Orientation3(EAST, SOUTH, UP);
  public static final Orientation3 YXZ = new Orientation3(UP, EAST, SOUTH);
  public static final Orientation3 YZX = new Orientation3(UP, SOUTH, EAST);
  public static final Orientation3 ZXY = new Orientation3(SOUTH, EAST, UP);
  public static final Orientation3 ZYX = new Orientation3(SOUTH, UP, EAST);

  /**
   * The primary {@link Direction3} of {@code this} {@link Orientation3}.
   */
  private final Direction3 primary;
  /**
   * The secondary {@link Direction3} of {@code this} {@link Orientation3}.
   */
  private final Direction3 secondary;
  /**
   * The tertiary {@link Direction3} of {@code this} {@link Orientation3}.
   */
  private final Direction3 tertiary;

  public Orientation3(Direction3 primary, Direction3 secondary, Direction3 tertiary) {
    this.primary = checkNotNull(primary, "primary == null!");
    this.secondary = checkNotNull(secondary, "secondary == null!");
    this.tertiary = checkNotNull(tertiary, "tertiary == null!");
    if (primary.getAxis() == secondary.getAxis() //
        || secondary.getAxis() == tertiary.getAxis() //
        || tertiary.getAxis() == primary.getAxis()//
    ) {
      throw new IllegalArgumentException("All directions must be on different axis!");
    }
  }

  /**
   * @return the value of {@link #primary}
   */
  public Direction3 getPrimary() {
    return primary;
  }

  /**
   * @return the value of {@link #secondary}
   */
  public Direction3 getSecondary() {
    return secondary;
  }

  /**
   * @return the value of {@link #tertiary}
   */
  public Direction3 getTertiary() {
    return tertiary;
  }

  public Direction3 get(Axis3 axis) {
    checkNotNull(axis, "axis == null!");
    if (primary.getAxis() == axis)
      return primary;
    if (secondary.getAxis() == axis)
      return secondary;
    if (tertiary.getAxis() == axis)
      return tertiary;
    throw new InternalError("All directions must be on different axis!");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((primary == null) ? 0 : primary.hashCode());
    result = prime * result + ((secondary == null) ? 0 : secondary.hashCode());
    result = prime * result + ((tertiary == null) ? 0 : tertiary.hashCode());
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
    Orientation3 other = (Orientation3) obj;
    if (primary != other.primary)
      return false;
    if (secondary != other.secondary)
      return false;
    if (tertiary != other.tertiary)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Orientation3 [primary=" + primary + ", secondary=" + secondary + ", tertiary="
        + tertiary + "]";
  }
}
