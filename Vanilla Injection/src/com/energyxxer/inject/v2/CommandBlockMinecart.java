package com.energyxxer.inject.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import com.energyxxer.inject.structures.CommandBlock;
import com.evilco.mc.nbt.tag.ITag;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagString;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.structure.Entity;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * Class containing information about a command block minecart. Not to be confused with a
 * <code>CommandBlock</code>.
 *
 * @see CommandBlock
 */
public class CommandBlockMinecart implements Entity {
  private final Command command;
  /**
   * The relative position of this {@link CommandBlockMinecart} within a {@link Structure}.
   */
  private final Coordinate3D coordinate;

  public CommandBlockMinecart(Command command, Coordinate3D coordinate) {
    this.command = checkNotNull(command, "command == null!");
    this.coordinate = checkNotNull(coordinate, "coordinate == null!");
  }

  /**
   * @return the value of {@link #command}
   */
  public Command getCommand() {
    return command;
  }

  /**
   * @return the value of {@link #coordinate}
   */
  @Override
  public Coordinate3D getCoordinate() {
    return coordinate;
  }

  @Override
  public TagCompound getNbt() {
    TagCompound nbt = new TagCompound("nbt");
    nbt.setTag(new TagString("id", "minecraft:commandblock_minecart"));
    TagCompound commandNbt = command.toNbt();
    for (ITag tag : commandNbt.getTags().values()) {
      nbt.setTag(tag);
    }
    return nbt;
  }

  @Override
  public String toString() {
    return "CommandBlockMinecart [command=" + command + ", coordinate=" + coordinate + "]";
  }
}
