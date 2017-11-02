package de.adrodoc55.minecraft.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.evilco.mc.nbt.tag.ITag;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagDouble;
import com.evilco.mc.nbt.tag.TagInteger;
import com.evilco.mc.nbt.tag.TagList;
import com.evilco.mc.nbt.tag.TagString;
import com.google.common.collect.Lists;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Coordinate3I;

/**
 * @author Adrodoc55
 */
public class Structure {
  static List<ITag> toNbt(Coordinate3D coordinate) {
    return Arrays.asList(//
        new TagDouble("", coordinate.x), //
        new TagDouble("", coordinate.y), //
        new TagDouble("", coordinate.z) //
    );
  }

  static List<ITag> toNbt(Coordinate3I coordinate) {
    return Arrays.asList(//
        new TagInteger("", coordinate.x), //
        new TagInteger("", coordinate.y), //
        new TagInteger("", coordinate.z) //
    );
  }

  private final Map<Coordinate3I, Block> blocks = new HashMap<>();
  private final List<Entity> entities = new ArrayList<>();
  private int dataVersion;
  private String author;
  private @Nullable BlockState background;

  public Structure(int dataVersion, String author) {
    this(dataVersion, author, null);
  }

  public Structure(int dataVersion, String author, @Nullable BlockState background) {
    setDataVersion(dataVersion);
    setAuthor(author);
    setBackground(background);
  }

  /**
   * @return the value of {@link #dataVersion}
   */
  public int getDataVersion() {
    return dataVersion;
  }

  /**
   * @param dataVersion the new value for {@link #dataVersion}
   */
  public void setDataVersion(int dataVersion) {
    this.dataVersion = dataVersion;
  }

  /**
   * @return the value of {@link #author}
   */
  public String getAuthor() {
    return author;
  }

  /**
   * @param author the new value for {@link #author}
   */
  public void setAuthor(String author) {
    this.author = checkNotNull(author, "author == null!");
  }

  /**
   * @return the value of {@link #background}
   */
  public @Nullable BlockState getBackground() {
    return background;
  }

  /**
   * @param background the new value for {@link #background}
   */
  public void setBackground(@Nullable BlockState background) {
    this.background = background;
  }

  public void addBlocks(Collection<? extends Block> blocks) {
    for (Block block : blocks) {
      addBlock(block);
    }
  }

  public void addBlock(Block block) {
    Coordinate3I coordinate = block.getCoordinate();
    if (blocks.containsKey(coordinate)) {
      throw new IllegalArgumentException(
          "There is already a block associated with the coordinate " + coordinate);
    }
    replaceBlock(block);
  }

  public void replaceBlock(Block block) {
    blocks.put(block.getCoordinate(), block);
  }

  public void addEntities(Collection<? extends Entity> entities) {
    this.entities.addAll(entities);
  }

  public void addEntity(Entity entity) {
    checkNotNull(entity, "entity == null!");
    entities.add(entity);
  }

  public TagCompound toNbt() {
    TagCompound result = new TagCompound("");
    result.setTag(new TagInteger("DataVersion", dataVersion));
    result.setTag(new TagString("author", author));
    Coordinate3I size = getSize();
    List<Block> blocks = new ArrayList<>(this.blocks.values());
    if (background != null) {
      for (int x = 0; x <= size.getX(); x++) {
        for (int y = 0; y <= size.getY(); y++) {
          for (int z = 0; z <= size.getZ(); z++) {
            Coordinate3I coordinate = new Coordinate3I(x, y, z);
            if (!this.blocks.containsKey(coordinate)) {
              blocks.add(new SimpleBlock(background, coordinate));
            }
          }
        }
      }
    }
    result.setTag(new TagList("size", toNbt(size)));
    Palette palette = new Palette();
    result.setTag(palette.toNbt(blocks));
    result.setTag(palette.toNbt());
    result.setTag(getEntities());
    return result;
  }

  public Coordinate3I getSize() {
    return Stream.concat(//
        blocks.keySet().stream(), //
        entities.stream()//
            .map(Entity::getCoordinate)//
            .map(Coordinate3D::ceil)//
    ).reduce(Coordinate3I.getBinaryOperator(Math::max))//
        .orElse(new Coordinate3I());
  }

  private ITag getEntities() {
    List<ITag> tags = Lists.transform(entities, this::toNbt);
    return new TagList("entities", tags);
  }

  private TagCompound toNbt(Entity entity) {
    TagCompound result = new TagCompound("");
    result.setTag(new TagList("pos", toNbt(entity.getCoordinate())));
    result.setTag(new TagList("blockPos", toNbt(entity.getCoordinate().floor())));
    TagCompound nbt = entity.getNbt();
    if (nbt != null) {
      result.setTag(nbt);
    }
    return result;
  }

  @Override
  public String toString() {
    return "Structure [blocks=" + blocks + ", entities=" + entities + ", dataVersion=" + dataVersion
        + ", author=" + author + ", background=" + background + "]";
  }
}
