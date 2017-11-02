package de.adrodoc55.minecraft.structure;

import java.util.Map;

import javax.annotation.Nullable;

import com.evilco.mc.nbt.tag.TagCompound;

/**
 * A Minecraft {@link Block}, but without {@link Block#getCoordinate()}.
 *
 * @author Adrodoc55
 */
public interface BlockState {
  /**
   * Returns the Minecraft identifier for this {@link BlockState}, for instance "minecraft:air".
   *
   * @return the Minecraft identifier
   */
  String getStringId();

  /**
   * The <a href="https://minecraft.gamepedia.com/Block_states">block states</a> of this
   * {@link BlockState}.
   *
   * @return block states
   */
  Map<String, String> getProperties();

  /**
   * The specific <a href="https://minecraft-de.gamepedia.com/NBT-Format">NBT</a> of this
   * {@link BlockState}. The {@link TagCompound} must be named "nbt".
   *
   * @return the specific NBT or {@code null}
   */
  @Nullable
  TagCompound getNbt();
}
