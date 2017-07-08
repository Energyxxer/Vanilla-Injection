package com.energyxxer.inject.structures;

import com.energyxxer.inject.jnbt.ByteTag;
import com.energyxxer.inject.jnbt.ListTag;
import com.energyxxer.inject.jnbt.StringTag;
import com.energyxxer.inject.jnbt.Tag;
import com.energyxxer.inject.structures.builder.StructureBuilder;
import com.energyxxer.inject.structures.builder.StructureEntityEntry;
import com.energyxxer.inject.utils.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing information about a command block minecart. Not to be confused with a <code>CommandBlock</code>.
 *
 * @see CommandBlock
 */
public class CommandBlockMinecart {
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

    /**
     * Inserts the command block minecart's information into the given <code>StructureBuilder</code>.
     *
     * @param builder The builder to insert this command block minecart to.
     * */
    public void insertTo(StructureBuilder builder) {
        StructureEntityEntry entry = new StructureEntityEntry(new Vector3D.Double(pos.x + 0.5,pos.y + 0.0625, pos.z + 0.5));

        entry.putNBT(new StringTag("Command", command));
        entry.putNBT(new StringTag("CustomName", name));
        entry.putNBT(new StringTag("id", "minecraft:commandblock_minecart"));
        entry.putNBT(new ByteTag("TrackOutput", (byte) 1));

        ArrayList<Tag> tagsTag = new ArrayList<>();
        for(String tag : tags) {
            tagsTag.add(new StringTag("", tag));
        }
        entry.putNBT(new ListTag("Tags", StringTag.class, tagsTag));

        builder.entities.addEntry(entry);
    }

    @Override
    public String toString() {
        return name + ": " + command;
    }
}
