package org.qiwur.scent.feature;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.math.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.qiwur.scent.configuration.ScentConfiguration;

public class PhraseFeatureParserTest {

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
