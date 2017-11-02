package de.adrodoc55.minecraft.structure;

import javax.annotation.Nullable;

import com.evilco.mc.nbt.tag.TagCompound;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;

/**
 * @author Adrodoc55
 */
public interface Entity {
  Coordinate3D getCoordinate();

  @Nullable
  TagCompound getNbt();
}
