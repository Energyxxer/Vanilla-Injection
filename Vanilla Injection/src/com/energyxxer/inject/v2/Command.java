package com.energyxxer.inject.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.evilco.mc.nbt.tag.TagByte;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagString;

public class Command {
  /**
   * The CustomName for this command's invoker.
   */
  private final @Nullable String name;
  private final String command;
  private final boolean trackOutput;

  public Command(String command) {
    this(null, command);
  }

  public Command(String name, @Nullable String command) {
    this(name, command, false);
  }

  public Command(String name, @Nullable String command, boolean trackOutput) {
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

  public TagCompound toNbt() {
    TagCompound nbt = new TagCompound("");
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
