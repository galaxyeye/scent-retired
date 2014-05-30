package org.qiwur.scent.utils;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.feature.BlockStatFeature;
import org.qiwur.scent.feature.PhraseFeature;
import org.qiwur.scent.feature.PhraseFeatureFactory;

public class FeatureTest {

  protected static final Logger logger = LogManager.getLogger(FeatureTest.class);

  public static void main(String[] args) throws IOException {
    Configuration conf = ScentConfiguration.create();

    String file = conf.get("scent.block.stat.feature.file");
    BlockStatFeature feature = BlockStatFeature.create(file, conf);
    logger.debug("feature : {}", feature);

    file = conf.get("scent.block.code.feature.file");
    PhraseFeature codeFeature = new PhraseFeatureFactory(file, conf).getFeature();
    logger.debug("feature : {}", codeFeature);

    file = conf.get("scent.block.text.feature.file");
    PhraseFeature textFeature = new PhraseFeatureFactory(file, conf).getFeature();
    logger.debug("feature : {}", textFeature);

    file = conf.get("scent.block.title.feature.file");
    PhraseFeature titleFeature = new PhraseFeatureFactory(file, conf).getFeature();
    logger.debug("feature : {}", titleFeature);
  }
}
