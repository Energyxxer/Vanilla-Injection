package com.energyxxer.inject.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import com.evilco.mc.nbt.tag.TagByte;
import com.evilco.mc.nbt.tag.TagCompound;

import de.adrodoc55.minecraft.coordinate.Coordinate3I;
import de.adrodoc55.minecraft.coordinate.Direction3;
import de.adrodoc55.minecraft.structure.Block;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * A Minecraft <a href="https://minecraft.gamepedia.com/Command_Block">command block</a> that can be
 * added to a {@link Structure}.
 *
 * @author Adrodoc55
 */
@Immutable
public class CommandBlock implements Block {
  /**
   * Enum containing all types of command blocks and their block IDs.
   */
  public enum Type {
    IMPULSE("minecraft:command_block"), //
    CHAIN("minecraft:chain_command_block"), //
    REPEAT("minecraft:repeating_command_block"),//
    ;
    /**
     * The Minecraft ID of the command block type.
     */
    private final String stringId;

    private Type(String stringId) {
      this.stringId = checkNotNull(stringId, "stringId == null!");
    }

    /**
     * @return the value of {@link #stringId}
     */
    public String getStringId() {
      return stringId;
    }
  }

  private final Command command;
  private final Coordinate3I coordinate;
  private final Direction3 direction;
  private final Type type;
  private final boolean conditional;
  private final boolean auto;

  public CommandBlock(String command, Coordinate3I coordinate, Direction3 direction, Type type,
      boolean conditional, boolean auto) {
    this(new Command(command), coordinate, direction, type, conditional, auto);
  }

  public CommandBlock(Command command, Coordinate3I coordinate, Direction3 direction, Type type,
      boolean conditional, boolean auto) {
    this.command = checkNotNull(command, "command == null!");
    this.coordinate = checkNotNull(coordinate, "coordinate == null!");
    this.direction = checkNotNull(direction, "direction == null!");
    this.type = checkNotNull(type, "type == null!");
    this.conditional = conditional;
    this.auto = auto;
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
  public Coordinate3I getCoordinate() {
    return coordinate;
  }

  /**
   * @return the value of {@link #direction}
   */
  public Direction3 getDirection() {
    return direction;
  }

  /**
   * @return the value of {@link #type}
   */
  public Type getType() {
    return type;
  }

  /**
   * @return the value of {@link #conditional}
   */
  public boolean isConditional() {
    return conditional;
  }

  /**
   * @return the value of {@link #auto}
   */
  public boolean isAuto() {
    return auto;
  }

  @Override
  public String getStringId() {
    return type.getStringId();
  }

  @Override
  public Map<String, String> getProperties() {
    HashMap<String, String> properties = new HashMap<>();
    properties.put("facing", String.valueOf(direction));
    properties.put("conditional", String.valueOf(conditional));
    return properties;
  }

  @Override
  public TagCompound getNbt() {
    TagCompound nbt = command.toNbt();
    nbt.setTag(new TagByte("auto", auto ? 1 : (byte) 0));
    return nbt;
  }

  @Override
  public String toString() {
    return "CommandBlock [command=" + command + ", coordinate=" + coordinate + ", direction="
        + direction + ", type=" + type + ", conditional=" + conditional + ", auto=" + auto + "]";
  }
}
