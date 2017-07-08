package com.energyxxer.inject.structures;

import com.energyxxer.inject.jnbt.ByteTag;
import com.energyxxer.inject.jnbt.StringTag;
import com.energyxxer.inject.structures.builder.StructureBlockEntry;
import com.energyxxer.inject.structures.builder.StructureBuilder;
import com.energyxxer.inject.structures.builder.StructurePaletteEntry;
import com.energyxxer.inject.utils.Vector3D;

/**
 * Class containing information about a command block. It contains more information about the command block than the <code>AbstractCommand</code> class.
 *
 * @see com.energyxxer.inject.AbstractCommand
 * @see CommandBlockMinecart
 */
public class CommandBlock {
    /**
     * Enum containing all types of command blocks and their block IDs.
     * */
    public enum Type {
        IMPULSE("minecraft:command_block"), CHAIN("minecraft:chain_command_block"), REPEATING("minecraft:repeating_command_block");

        /**
         * The ID of the command block type.
         * */
        public String id;
        /**
         * Creates a command block type with the given block ID.
         *
         * @param id The block ID.
         * */
        Type(String id) {
            this.id = id;
        }
    }
    /**
     * Enum containing all world directions and their vector representations.
     * */
    public enum Orientation {
        NORTH(0,0,-1), SOUTH(0,0,1), WEST(-1,0,0), EAST(1,0,0), UP(0,1,0), DOWN(0,-1,0);

        /**
         * A Vector3D object to represent the relative change in position after moving
         * 1 block forward in the given orientation.
         * */
        public Vector3D vector;
        /**
         * The opposite to this orientation.
         * */
        public Orientation opposite;

        /**
         * Creates an Orientation by the given vector.
         *
         * @param x The change in the X axis.
         * @param y The change in the Y axis.
         * @param z The change in the Z axis.
         * */
        Orientation(int x, int y, int z) {
            this.vector = new Vector3D(x,y,z);
        }

        /*
         * Sets the opposite to each orientation. Can't be done within the constructor as it
         * would require forward references.
         * */
        static {
            NORTH.opposite = SOUTH;
            SOUTH.opposite = NORTH;
            WEST.opposite = EAST;
            EAST.opposite = WEST;
            UP.opposite = DOWN;
            DOWN.opposite = UP;
        }
    }

    /**
     * The name of this command block.
     * */
    private String name = null;
    /**
     * The type of this command block.
     * */
    private Type type;
    /**
     * The command in this block.
     * */
    private String command;
    /**
     * The orientation this command block is facing.
     * */
    private Orientation orientation;
    /**
     * The point in 3D space this command block is in.
     * */
    private Vector3D pos;
    /**
     * Whether this command block is conditional. Currently unused by the injector.
     * */
    private boolean conditional;

    /**
     * Whether this command block should track its output.
     * */
    private boolean trackOutput = false;

    /**
     * Creates a command block with the given command.
     * Type defaults to chain, position defaults to (0,0,0) and conditional state defaults to false.
     *
     * @param command The command in this block.
     * */
    public CommandBlock(String command) {
        this(Type.CHAIN, command);
    }

    /**
     * Creates a command block with the given type and command.
     * Position defaults to (0,0,0) and conditional state defaults to false.
     *
     * @param type The command block's type.
     * @param command The command block's command.
     * */
    public CommandBlock(Type type, String command) {
        this(type, command, false);
    }

    /**
     * Creates a command block with the given type, command and conditional state.
     * Position defaults to (0,0,0).
     *
     * @param type The command block's type.
     * @param command The command block's command.
     * @param conditional Whether this command block is conditional.
     * */
    public CommandBlock(Type type, String command, boolean conditional) {
        this(type, command, new Vector3D(), conditional);
    }

    /**
     * Creates a command block with the given type, command, position and conditional state.
     *
     * @param type The command block's type.
     * @param command The command block's command.
     * @param pos The position of this command block in a 3D coordinate space.
     * @param conditional Whether this command block is conditional.
     * */
    public CommandBlock(Type type, String command, Vector3D pos, boolean conditional) {
        this.type = type;
        this.command = command;
        this.pos = pos;
        this.conditional = conditional;
        this.orientation = Orientation.SOUTH;
    }

    /**
     * Gets the type of this command block.
     *
     * @return The type.
     * */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this command block.
     *
     * @param type The new type for this command block.
     * */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the command in this command block.
     *
     * @return The command.
     * */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the command in this command block.
     *
     * @param command The new command in this command block.
     * */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Gets the orientation of this command block.
     *
     * @return The orientation.
     * */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation of this command block.
     *
     * @param orientation The new orientation for this command block.
     * */
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Gets the conditional state of this command block.
     *
     * @return true if block is conditional, otherwise false.
     * */
    public boolean isConditional() {
        return conditional;
    }

    /**
     * Sets the conditional state of this command block.
     *
     * @param conditional Whether the command block should be conditional or not.
     * */
    public void setConditional(boolean conditional) {
        this.conditional = conditional;
    }

    /**
     * Returns whether the command block's output is enabled.
     *
     * @return true if this block's output is enabled, otherwise false.
     * */
    public boolean isTrackOutputEnabled() {
        return trackOutput;
    }

    /**
     * Sets the TrackOutput of this command block.
     *
     * @param trackOutput The new TrackOutput for this command block.
     * */
    public void setTrackOutput(boolean trackOutput) {
        this.trackOutput = trackOutput;
    }

    /**
     * Gets the custom name of this command block.
     *
     * @return The command block's name. May be <code>null</code>.
     * */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this command block.
     *
     * @param name The new name of this command block.
     * */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the position of this command block in 3D space.
     *
     * @return The position of this command block.
     * */
    public Vector3D getPos() {
        return pos;
    }

    /**
     * Sets the position of this command block.
     *
     * @param pos The new position for this command block.
     * */
    public void setPos(Vector3D pos) {
        this.pos = pos;
    }

    /**
     * Inserts the command block's information into the given <code>StructureBuilder</code>. This includes
     * adding the block palette entry and the block entry.
     *
     * @param builder The builder to insert this command block to.
     * */
    public void insertTo(StructureBuilder builder) {
        StructurePaletteEntry paletteEntry = new StructurePaletteEntry(this.type.id);
        paletteEntry.putProperty("facing", this.orientation.toString().toLowerCase());
        int state = builder.palette.addEntry(paletteEntry);

        StructureBlockEntry blockEntry = new StructureBlockEntry(this.pos, state);
        blockEntry.putNBT(new ByteTag("auto", (byte) 1));
        blockEntry.putNBT(new StringTag("Command", this.command));
        blockEntry.putNBT(new ByteTag("TrackOutput", (byte) ((trackOutput) ? 1 : 0)));
        if(name != null)
            blockEntry.putNBT(new StringTag("CustomName", name));
        builder.blocks.addEntry(blockEntry);
    }

    @Override
    public String toString() {
        return "" + type + ":" + name + ":" + command + " @" + pos;
    }
}
