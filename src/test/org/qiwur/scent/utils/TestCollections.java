package org.qiwur.scent.utils;

import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.block.DomSegment;

import com.google.common.collect.Lists;

public class TestCollections {

  private static void testBlockLabel() {
    Configuration conf = ScentConfiguration.create();
    String[] labels = conf.getStrings("scent.classifier.block.labels");

    for (String label : labels) {
      BlockLabel l = BlockLabel.fromString(label);
      if (BlockLabel.builtinLabels.contains(l)) {
        System.out.println(l);
      }
    }
  }

  @Test
  public void testBlockTracker() {
    // Map<BlockLabel, Double> trackees = new HashMap<BlockLabel, Double>();
    // BlockLabelTracker tracker = new BlockLabelTracker();
    DomSegment segment = new DomSegment(null);

    int count = 0;
    for (BlockLabel label : BlockLabel.builtinLabels) {
      double sim = 0.1 + count++ / 10.0;
      // trackees.put(label, sim);
      // tracker.set(label, sim);
      segment.tag(label, sim);
    }

    System.out.println(BlockLabel.fromString("BadBlock").text().length());
    System.out.println(BlockLabel.BadBlock.text().length());

    if (BlockLabel.BadBlock.equals(BlockLabel.fromString("BadBlock"))) {
      System.out.println("gooooooooood");
    }

    int b = BlockLabel.fromString("BadBlock").compareTo(BlockLabel.BadBlock);
    System.out.println(b);

    System.out.println(segment.labelTracker().get(BlockLabel.BadBlock));
    System.out.println(segment.labelTracker().get(BlockLabel.fromString("BadBlock")));
  }

  public List<BlockPattern> parsePatterns(String text) {
    List<BlockPattern> patterns = Lists.newArrayList();

    for (String pattern : text.split(",")) {
      BlockPattern p = BlockPattern.fromString(pattern);

      // for SET, the hashCode must be equal
      System.out.println(BlockPattern.LinkImages.equals(p));
      System.out.println(BlockPattern.patterns.contains(BlockPattern.LinkImages));
      System.out.println(BlockPattern.patterns.contains(p));

      if (BlockPattern.patterns.contains(p)) {
        System.out.println("++");
        patterns.add(p);
      }
    }

    return patterns;
  }

  public static void main(String[] args) throws Exception {

  }
}
