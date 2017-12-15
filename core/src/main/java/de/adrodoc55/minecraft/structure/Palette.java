package de.adrodoc55.minecraft.structure;

import java.util.ArrayList;
import java.util.List;

import com.evilco.mc.nbt.tag.ITag;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagInteger;
import com.evilco.mc.nbt.tag.TagList;
import com.google.common.collect.Lists;

/**
 * @author Adrodoc55
 */
class Palette {
  private List<State> states = new ArrayList<>();

  public ITag toNbt(List<Block> blocks) {
    List<ITag> tags = Lists.transform(blocks, this::toNbt);
    return new TagList("blocks", tags);
  }

  public TagCompound toNbt(Block block) {
    TagCompound result = new TagCompound("");
    result.setTag(new TagInteger("state", getStateIndex(block)));
    result.setTag(new TagList("pos", Structure.toNbt(block.getCoordinate())));
    TagCompound nbt = block.getNbt();
    if (nbt != null) {
      result.setTag(nbt);
    }
    return result;
  }

  private int getStateIndex(Block block) {
    State state = new State(block);
    int index = states.indexOf(state);
    if (index >= 0) {
      return index;
    }
    states.add(state);
    return states.size() - 1;
  }

  public TagList toNbt() {
    List<ITag> palette = Lists.transform(states, State::toNbt);
    return new TagList("palette", palette);
  }
}
