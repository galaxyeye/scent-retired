package org.qiwur.scent.jsoup.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class BlockLabel implements Comparable<BlockLabel> {
  public static BlockLabel UnknownBlock = BlockLabel.fromString("UnknownBlock");
  public static BlockLabel BadBlock = BlockLabel.fromString("BadBlock");
  public static BlockLabel Metadata = BlockLabel.fromString("Metadata");
  public static BlockLabel Title = BlockLabel.fromString("Title");
  public static BlockLabel TitleContainer = BlockLabel.fromString("TitleContainer");
  public static BlockLabel Menu = BlockLabel.fromString("Menu");
  public static BlockLabel Areas = BlockLabel.fromString("Areas");
  public static BlockLabel Categories = BlockLabel.fromString("Categories");
  public static BlockLabel Gallery = BlockLabel.fromString("Gallery");
  public static BlockLabel Links = BlockLabel.fromString("Links");
  public static BlockLabel DensyLinks = BlockLabel.fromString("DensyLinks");
  public static BlockLabel LinkImages = BlockLabel.fromString("LinkImages");
  public static BlockLabel PureImages = BlockLabel.fromString("PureImages");
  public static BlockLabel SimilarEntity = BlockLabel.fromString("SimilarEntity");

  public static Set<BlockLabel> labels = Sets.newHashSet();
  public static Set<BlockLabel> inheritableLabels = Sets.newHashSet();

  static {
    labels.add(UnknownBlock);
    labels.add(BadBlock);
    labels.add(Metadata);
    labels.add(Title);
    labels.add(TitleContainer);
    labels.add(Menu);
    labels.add(Areas);
    labels.add(Categories);
    labels.add(Gallery);
    labels.add(Links);
    labels.add(DensyLinks);
    labels.add(LinkImages);
    labels.add(PureImages);
    labels.add(SimilarEntity);

    inheritableLabels.add(Areas);
    inheritableLabels.add(DensyLinks);
    inheritableLabels.add(TitleContainer);
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
    return inheritableLabels.contains(this);
  }

  public static boolean inheritable(BlockLabel label) {
    return label.inheritable();
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
