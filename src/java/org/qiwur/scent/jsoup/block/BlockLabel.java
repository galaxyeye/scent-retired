package org.qiwur.scent.jsoup.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class BlockLabel implements Comparable<BlockLabel> {
  public static BlockLabel UnknownBlock = BlockLabel.fromString("");
  public static BlockLabel BadBlock = BlockLabel.fromString("BadBlock");
  public static BlockLabel Metadata = BlockLabel.fromString("Metadata");
  public static BlockLabel Title = BlockLabel.fromString("Title");
  public static BlockLabel TitleContainer = BlockLabel.fromString("TitleContainer");
  public static BlockLabel Menu = BlockLabel.fromString("Menu");
  public static BlockLabel Areas = BlockLabel.fromString("Areas");
  public static BlockLabel Categories = BlockLabel.fromString("Categories");
  public static BlockLabel Gallery = BlockLabel.fromString("Gallery");
  public static BlockLabel SimilarEntity = BlockLabel.fromString("SimilarEntity");

  public static Set<BlockLabel> inheritableLabels = Sets.newHashSet();
  public static Set<BlockLabel> builtinLabels = Sets.newHashSet();

  static {
    builtinLabels.add(UnknownBlock);
    builtinLabels.add(BadBlock);
    builtinLabels.add(Metadata);
    builtinLabels.add(Title);
    builtinLabels.add(TitleContainer);
    builtinLabels.add(Menu);
    builtinLabels.add(Areas);
    builtinLabels.add(Categories);
    builtinLabels.add(Gallery);
    builtinLabels.add(SimilarEntity);

    inheritableLabels.add(Areas);
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

  public static Set<String> mergeLabels(String... incoming) {
    return mergeLabels(Arrays.asList(incoming));
  }

  public static Set<String> mergeLabels(Collection<String> incoming) {
    Set<String> result = Sets.newHashSet(incoming);

    for (BlockLabel l : builtinLabels) {
      result.add(l.text());
    }

    return result;
  }

  public boolean isBuiltin() {
    return builtinLabels.contains(this);
  }

  public static boolean isBuiltin(BlockLabel label) {
    return label.isBuiltin();
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
