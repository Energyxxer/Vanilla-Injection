package de.adrodoc55.minecraft.structure;

import java.util.Map;

import javax.annotation.Nullable;

import com.evilco.mc.nbt.tag.TagCompound;

/**
 * @author Adrodoc55
 */
public interface BlockState {
  String getStringId();

  Map<String, String> getProperties();

  @Nullable
  TagCompound getNbt();
}
