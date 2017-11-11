package com.energyxxer.inject.structures;

import java.util.ArrayList;
import java.util.List;

import com.energyxxer.inject.utils.Vector3D;
import com.evilco.mc.nbt.tag.ITag;
import com.evilco.mc.nbt.tag.TagByte;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagList;
import com.evilco.mc.nbt.tag.TagString;

import de.adrodoc55.minecraft.coordinate.Vec3D;
import de.adrodoc55.minecraft.structure.Entity;

/**
 * Class containing information about a command block minecart. Not to be confused with a <code>CommandBlock</code>.
 *
 * @see CommandBlock
 */
public class CommandBlockMinecart implements Entity {
    /**
     * The command in this minecart.
     * */
    private String command;
    /**
     * This minecart's custom name.
     * */
    private String name;
    /**
     * The position relative to the structure's origin where this minecart should be.
     * */
    private Vector3D pos;
    /**
     * A list of tags to add to the minecart's "Tags" list.
     * */
    private ArrayList<String> tags = new ArrayList<>();

    /**
     * Creates a command block minecart with the given command and position. Its name defaults to "@"
     *
     * @param command This minecart's command.
     * @param pos This minecart's position relative to the structure's origin.
     * */
    public CommandBlockMinecart(String command, Vector3D pos) {
        this(command, "@", pos);
    }

    /**
     * Creates a command block minecart with the given command, name and position.
     *
     * @param command This minecart's command.
     * @param name This minecart's name.
     * @param pos This minecart's position relative to the structure's origin.
     * */
    public CommandBlockMinecart(String command, String name, Vector3D pos) {
        this.command = command;
        if(name == null ) this.name = "@";
        else this.name = name;
        this.pos = pos;
    }

    /**
     * Gets the command in this minecart.
     *
     * @return The command.
     * */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the command in this minecart.
     *
     * @param command The new command in this minecart.
     * */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Gets the custom name of this minecart.
     *
     * @return The command block minecart's name.
     * */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this minecart.
     *
     * @param name The new name of this minecart.
     * */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the position of this minecart in 3D space.
     *
     * @return The position of this minecart.
     * */
    public Vector3D getPos() {
        return pos;
    }

    /**
     * Sets the position of this minecart.
     *
     * @param pos The new position for this minecart.
     * */
    public void setPos(Vector3D pos) {
        this.pos = pos;
    }

    /**
     * Gets a list of scoreboard tags for this minecart.
     *
     * @return The list of tags.
     * */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Adds a scoreboard tag to the minecart.
     *
     * @param tag The scoreboard tag to add.
     * */
    public void addTag(String tag) {
        tags.add(tag);
    }


    @Override
    public Vec3D getCoordinate() {
      return new Vec3D(pos.x + 0.5, pos.y + 0.0625, pos.z + 0.5);
    }

    @Override
    public TagCompound getNbt() {
      TagCompound nbt = new TagCompound("nbt");
      nbt.setTag(new TagString("Command", command));
      nbt.setTag(new TagString("CustomName", name));
      nbt.setTag(new TagString("id", "minecraft:commandblock_minecart"));
      nbt.setTag(new TagByte("TrackOutput", (byte) 1));
      ArrayList<ITag> tags = new ArrayList<>();
      for(String tag : this.tags) {
          tags.add(new TagString("", tag));
      }
      nbt.setTag(new TagList("Tags", tags));
      return nbt;
    }

    @Override
    public String toString() {
        return name + ": " + command;
    }
}
