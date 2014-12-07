package org.qiwur.scent.feature;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.math.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.qiwur.scent.data.feature.PhraseFeatureParser;
import org.qiwur.scent.utils.ScentConfiguration;

public class TestPhraseFeatureParser {

  private Configuration conf;

  @Before
  public void setUp() throws Exception {
    conf = ScentConfiguration.create();
  }

  @Test
  public void testParse() {
    String[] featureFiles = conf.getStrings("scent.block.text.feature.file");
    PhraseFeatureParser parser = new PhraseFeatureParser(featureFiles);
    parser.parse();

    System.out.println(parser.report());
  }
}
