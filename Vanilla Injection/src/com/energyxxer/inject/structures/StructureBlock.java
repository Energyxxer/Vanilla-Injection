package com.energyxxer.inject.structures;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagString;

import de.adrodoc55.minecraft.coordinate.Vec3I;
import de.adrodoc55.minecraft.structure.Block;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * A Minecraft <a href="https://minecraft.gamepedia.com/Structure_Block">structure block</a> that
 * can be added to a {@link Structure}.
 *
 * @author Adrodoc55
 */
public class StructureBlock implements Block {
  public enum Mode {
    DATA, SAVE, LOAD, CORNER;
  }

  private final Vec3I coordinate;
  private final Mode mode;
  private final @Nullable String name;

  public StructureBlock(Vec3I coordinate, Mode mode, @Nullable String name) {
    this.coordinate = checkNotNull(coordinate, "coordinate == null!");
    this.mode = checkNotNull(mode, "mode == null!");
    this.name = name;
  }

  @Override
  public String getStringId() {
    return "minecraft:structure_block";
  }

  @Override
  public Map<String, String> getProperties() {
    return Collections.emptyMap();
  }

  @Override
  public Vec3I getCoordinate() {
    return coordinate;
  }

  @Override
  public TagCompound getNbt() {
    TagCompound nbt = new TagCompound("nbt");
    nbt.setTag(new TagString("mode", mode.name()));
    if (name != null) {
      nbt.setTag(new TagString("name", name));
    }
    return nbt;
  }
}
