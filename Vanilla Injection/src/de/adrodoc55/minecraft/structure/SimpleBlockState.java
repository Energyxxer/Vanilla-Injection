package de.adrodoc55.minecraft.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.evilco.mc.nbt.tag.TagCompound;

/**
 * @author Adrodoc55
 */
public class SimpleBlockState implements BlockState {
  protected String stringId;
  protected final Map<String, String> properties;
  protected @Nullable TagCompound nbt;

  public SimpleBlockState(String stringId) {
    this(stringId, null);
  }

  public SimpleBlockState(BlockState state) {
    this(state.getStringId(), state.getNbt());
    this.properties.putAll(state.getProperties());
  }

  public SimpleBlockState(String stringId, @Nullable TagCompound nbt) {
    this(stringId, Collections.emptyMap(), nbt);
  }

  public SimpleBlockState(String stringId, Map<? extends String, ? extends String> properties,
      @Nullable TagCompound nbt) {
    setStringId(stringId);
    this.properties = new HashMap<>(properties);
    setNbt(nbt);
  }

  /**
   * @return the value of {@link #stringId}
   */
  @Override
  public String getStringId() {
    return stringId;
  }

  /**
   * @param stringId the new value for {@link #stringId}
   */
  public void setStringId(String stringId) {
    this.stringId = checkNotNull(stringId, "stringId == null!");
  }

  /**
   * @return the value of {@link #properties}
   */
  @Override
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(properties);
  }

  public void putProperty(String name, String value) {
    properties.put(name, value);
  }

  /**
   * @return the value of {@link #nbt}
   */
  @Override
  public @Nullable TagCompound getNbt() {
    return nbt;
  }

  /**
   * @param nbt the new value for {@link #nbt}
   */
  public void setNbt(@Nullable TagCompound nbt) {
    this.nbt = nbt;
  }

  @Override
  public String toString() {
    return "SimpleBlockState [stringId=" + stringId + ", properties=" + properties + ", nbt=" + nbt
        + "]";
  }
}
