package de.adrodoc55.minecraft.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import com.evilco.mc.nbt.tag.TagCompound;

import de.adrodoc55.minecraft.coordinate.Vec3I;

/**
 * @author Adrodoc55
 */
public class SimpleBlock extends SimpleBlockState implements Block {
  private Vec3I coordinate;

  public SimpleBlock(String stringId, Vec3I coordinate) {
    super(stringId);
    this.coordinate = checkNotNull(coordinate, "coordinate == null!");
  }

  public SimpleBlock(BlockState state, Vec3I coordinate) {
    super(state);
    this.coordinate = checkNotNull(coordinate, "coordinate == null!");
  }

  public SimpleBlock(String stringId, TagCompound nbt, Vec3I coordinate) {
    super(stringId, nbt);
    this.coordinate = checkNotNull(coordinate, "coordinate == null!");
  }

  /**
   * @return the value of {@link #coordinate}
   */
  @Override
  public Vec3I getCoordinate() {
    return coordinate;
  }

  /**
   * @param coordinate the new value for {@link #coordinate}
   */
  public void setCoordinate(Vec3I coordinate) {
    this.coordinate = coordinate;
  }

  @Override
  public String toString() {
    return "SimpleBlock [coordinate=" + coordinate + ", stringId=" + stringId + ", properties="
        + properties + ", nbt=" + nbt + "]";
  }
}
