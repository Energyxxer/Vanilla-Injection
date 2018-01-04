package com.energyxxer.inject.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagList;
import com.evilco.mc.nbt.tag.TagString;
import com.google.common.collect.Lists;

import de.adrodoc55.minecraft.coordinate.Vec3D;
import de.adrodoc55.minecraft.structure.Entity;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * A Minecraft <a href="https://minecraft.gamepedia.com/Minecart_with_Command_Block">command block
 * minecart</a> that can be added to a {@link Structure}.
 *
 * @author Adrodoc55
 */
public class CommandBlockMinecart implements Entity {
  private final Command command;
  /**
   * The relative position of this {@link CommandBlockMinecart} within a {@link Structure}.
   */
  private final Vec3D coordinate;
  private final List<String> tags = new ArrayList<>();

  public CommandBlockMinecart(Command command, Vec3D coordinate) {
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
  public Vec3D getCoordinate() {
    return coordinate;
  }

  /**
   * @return the value of {@link #tags}
   */
  public List<String> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public void addTag(String tag) {
    tags.add(checkNotNull(tag, "tag == null!"));
  }

  public boolean removeTag(String tag) {
    return tags.remove(checkNotNull(tag, "tag == null!"));
  }

  @Override
  public TagCompound getNbt() {
    TagCompound nbt = command.toNbt();
    nbt.setTag(new TagString("id", "minecraft:commandblock_minecart"));
    nbt.setTag(new TagList("Tags", Lists.transform(tags, tag -> new TagString("", tag))));
    return nbt;
  }

  @Override
  public String toString() {
    return "CommandBlockMinecart [command=" + command + ", coordinate=" + coordinate + "]";
  }
}
