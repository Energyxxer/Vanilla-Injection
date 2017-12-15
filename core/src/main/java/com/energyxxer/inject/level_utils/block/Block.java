package com.energyxxer.inject.level_utils.block;

/**
 * Class representing a single block in a chunk.
 */
public class Block {
    /**
     * The block type.
     * */
    public final BlockType type;
    /**
     * The block's data value. -1 if generic.
     * */
    public final int data;

    /**
     * Creates a block with the given type.
     * Data value defaults to -1.
     *
     * @param type The block's type.
     * */
    Block(BlockType type) {
        this(type, -1);
    }

    /**
     * Creates a block with the given type and data value.
     *
     * @param type The block's type.
     * @param data THe block's data value.
     * */
    Block(BlockType type, int data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return "" + type + ((data >= 0) ? (":" + data) : "");
    }

    /**
     * Returns a block by the given numerical ID and data.
     *
     * @param id The numerical ID.
     * @param data The numerical data.
     *
     * @return The constant block object representing the given info. <code>null</code> if it doesn't exist.
     * */
    public static Block get(int id, int data) {
        BlockType type = BlockType.getBlockForId(id);
        if(type == null) {
            return null;
        }
        return type.getByData(data);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Block)) return false;
        Block ob = (Block) o;
        return (ob.type == this.type) && ((this.data == -1 || ob.data == -1) || this.data == ob.data);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + data;
        return result;
    }
}
