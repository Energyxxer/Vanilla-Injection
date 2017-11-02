package de.adrodoc55.minecraft.structure;

import de.adrodoc55.minecraft.coordinate.Coordinate3I;

/**
 * @author Adrodoc55
 */
public interface Block extends BlockState {
  Coordinate3I getCoordinate();
}
