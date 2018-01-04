package com.energyxxer.inject.level_utils;

import com.energyxxer.inject.level_utils.nbt.Tag;

/**
 * Didn't know where to put this one method so I made a whole class for it.
 * OOP for the win.
 */
class TagUtils {
    /**
     * Checks if the given tag matches the name and tag type specified.
     *
     * @param tag The tag to check.
     * @param name The name to check for.
     * @param type The class of the tag type to check for.
     *
     * @return <code>true</code> if the tag matches the given info, <code>false</code> otherwise.
     * */
    static boolean match(Tag tag, String name, Class<? extends Tag> type) {
        return tag.getName().equals(name) && type.isInstance(tag);
    }
}
