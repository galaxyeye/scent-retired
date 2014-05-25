package org.qiwur.scent.jsoup.block;

import java.util.HashSet;
import java.util.Set;

public class BlockLabel implements Comparable<BlockLabel> {
  public static BlockLabel UnknownBlock = BlockLabel.fromString("UnknownBlock");
  public static BlockLabel BadBlock = BlockLabel.fromString("BadBlock");
  public static BlockLabel Metadata = BlockLabel.fromString("Metadata");
  public static BlockLabel Title = BlockLabel.fromString("Title");
  public static BlockLabel Menu = BlockLabel.fromString("Menu");
  public static BlockLabel Categories = BlockLabel.fromString("Categories");
  public static BlockLabel Gallery = BlockLabel.fromString("Gallery");
  public static BlockLabel Links = BlockLabel.fromString("Links");
  public static BlockLabel Images = BlockLabel.fromString("Images");
  public static BlockLabel SimilarEntity = BlockLabel.fromString("SimilarEntity");

  public static Set<BlockLabel> labels = new HashSet<BlockLabel>();

  static {
    labels.add(UnknownBlock);
    labels.add(BadBlock);
    labels.add(Metadata);
    labels.add(Title);
    labels.add(Menu);
    labels.add(Categories);
    labels.add(Gallery);
    labels.add(Links);
    labels.add(Images);
    labels.add(SimilarEntity);
  }

  private String text;

  private BlockLabel(String text) {
    this.text = text;
  }

  public String text() {
    return text;
  }

  public static BlockLabel fromString(String text) {
    if (text != null) {
      return new BlockLabel(text);
    }

    return null;
  }

  @Override
  public String toString() {
    return text;
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof BlockLabel) && text.equals(((BlockLabel)other).text);
  }

  @Override
  public int compareTo(BlockLabel other) {
    return text.compareTo(other.text);
  }
}
