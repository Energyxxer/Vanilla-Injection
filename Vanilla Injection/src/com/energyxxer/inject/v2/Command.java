package com.energyxxer.inject.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.evilco.mc.nbt.tag.TagByte;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagString;

/**
 * The common information shared by {@link CommandBlock} and {@link CommandBlockMinecart}.
 *
 * @author Adrodoc55
 */
@Immutable
public class Command implements de.adrodoc55.minecraft.placement.Command {
  /**
   * The CustomName for this {@link Command}'s container (either the {@link CommandBlock} or
   * {@link CommandBlockMinecart}).
   */
  private final @Nullable String name;
  /**
   * A Minecraft <a href="https://minecraft.gamepedia.com/Commands">command</a>.
   */
  private final String command;
  private final boolean trackOutput;

  public Command(String command) {
    this(null, command);
  }

  public Command(@Nullable String name, String command) {
    this(name, command, false);
  }

  public Command(@Nullable String name, String command, boolean trackOutput) {
    this.name = name;
    this.command = checkNotNull(command, "command == null!");
    this.trackOutput = trackOutput;
  }

  /**
   * @return the value of {@link #name}
   */
  public @Nullable String getName() {
    return name;
  }

  /**
   * @return the value of {@link #command}
   */
  public String getCommand() {
    return command;
  }

  /**
   * @return the value of {@link #trackOutput}
   */
  public boolean isTrackOutput() {
    return trackOutput;
  }

  @Override
  public boolean isConditional() {
    return false;
  }

  public TagCompound toNbt() {
    TagCompound nbt = new TagCompound("nbt");
    nbt.setTag(new TagString("Command", command));
    if (name != null) {
      nbt.setTag(new TagString("CustomName", name));
    }
    nbt.setTag(new TagByte("TrackOutput", (byte) (trackOutput ? 1 : 0)));
    return nbt;
  }

  @Override
  public String toString() {
    return (name != null ? name : "@") + ": " + command;
  }
}
