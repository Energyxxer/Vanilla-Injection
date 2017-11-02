package de.adrodoc55.minecraft.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.Immutable;

import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagString;
import com.google.common.collect.ImmutableMap;

/**
 * @author Adrodoc55
 */
@Immutable
class State {
  private final String name;
  private final ImmutableMap<String, String> properties;

  public State(String name, Map<String, String> properties) {
    this.name = checkNotNull(name, "name == null!");
    this.properties = ImmutableMap.copyOf(properties);
  }

  public State(Block block) {
    this(block.getStringId(), block.getProperties());
  }

  public TagCompound toNbt() {
    TagCompound result = new TagCompound("");
    result.setTag(new TagString("Name", name));
    result.setTag(getPropertiesTag());
    return result;
  }

  private TagCompound getPropertiesTag() {
    TagCompound properties = new TagCompound("Properties");
    for (Entry<String, String> entry : this.properties.entrySet()) {
      properties.setTag(new TagString(entry.getKey(), entry.getValue()));
    }
    return properties;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    State other = (State) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    return true;
  }
}
