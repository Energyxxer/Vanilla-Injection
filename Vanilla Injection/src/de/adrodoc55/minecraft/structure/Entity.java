package de.adrodoc55.minecraft.structure;

import com.evilco.mc.nbt.tag.TagCompound;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;

/**
 * A Minecraft <a href="https://minecraft.gamepedia.com/Entity">entity</a> that can be added to a
 * {@link Structure}.
 *
 * @author Adrodoc55
 */
public interface Entity {
  /**
   * The relative position of this {@link Entity} within a {@link Structure}.
   *
   * @return relative position
   */
  Coordinate3D getCoordinate();

  /**
   * The specific <a href="https://minecraft-de.gamepedia.com/NBT-Format">NBT</a> of this
   * {@link Entity}. The {@link TagCompound} must be named "nbt".
   *
   * @return the specific NBT
   */
  TagCompound getNbt();
}
