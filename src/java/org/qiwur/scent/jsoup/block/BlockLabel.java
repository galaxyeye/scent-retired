package org.qiwur.scent.jsoup.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BlockLabel implements Comparable<BlockLabel> {
  public static BlockLabel UnknownBlock = BlockLabel.fromString("UnknownBlock");
  public static BlockLabel BadBlock = BlockLabel.fromString("BadBlock");
  public static BlockLabel Metadata = BlockLabel.fromString("Metadata");
  public static BlockLabel Title = BlockLabel.fromString("Title");
  public static BlockLabel Menu = BlockLabel.fromString("Menu");
  public static BlockLabel Categories = BlockLabel.fromString("Categories");
  public static BlockLabel Gallery = BlockLabel.fromString("Gallery");
  public static BlockLabel Links = BlockLabel.fromString("Links");
  public static BlockLabel DensyLinks = BlockLabel.fromString("DensyLinks");
  public static BlockLabel LinkImages = BlockLabel.fromString("LinkImages");
  public static BlockLabel PureImages = BlockLabel.fromString("PureImages");
  public static BlockLabel SimilarEntity = BlockLabel.fromString("SimilarEntity");

  public static List<BlockLabel> labels = new ArrayList<BlockLabel>();
  public static List<BlockLabel> noInheritLabels = new ArrayList<BlockLabel>();

  static {
    labels.add(UnknownBlock);
    labels.add(BadBlock);
    labels.add(Metadata);
    labels.add(Title);
    labels.add(Menu);
    labels.add(Categories);
    labels.add(Gallery);
    labels.add(Links);
    labels.add(DensyLinks);
    labels.add(LinkImages);
    labels.add(PureImages);
    labels.add(SimilarEntity);

    noInheritLabels.add(UnknownBlock);
    noInheritLabels.add(BadBlock);
    noInheritLabels.add(LinkImages);
    noInheritLabels.add(PureImages);
    noInheritLabels.add(Links);
    noInheritLabels.add(DensyLinks);
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

  public static List<String> mergeLabels(String... incoming) {
    return mergeLabels(Arrays.asList(incoming));
  }

  public static List<String> mergeLabels(Collection<String> incoming) {
    List<String> result = new ArrayList<String>();

    for (BlockLabel l : labels) {
      result.add(l.text());
    }
    result.addAll(incoming);

    return result;
  }

  public boolean inheritable() {
    return !noInheritLabels.contains(this);
  }

  public static boolean inheritable(BlockLabel label) {
    return !noInheritLabels.contains(label);
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
