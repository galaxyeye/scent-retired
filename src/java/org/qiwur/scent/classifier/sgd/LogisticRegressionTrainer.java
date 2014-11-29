package org.qiwur.scent.classifier.sgd;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.vectorizer.encoders.Dictionary;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.jsoup.nodes.Indicator;

import com.google.common.collect.Lists;

public class LogisticRegressionTrainer {

  static final Logger logger = LogManager.getLogger(LogisticRegressionTrainer.class);

  private final Configuration conf;
  private final Dictionary categories = new Dictionary();
  private final List<File> files = Lists.newArrayList();
  private final String modelFile;
  private final String sampleBaseDir;
  private final int leakType = 0;
  private final int probes;
  private final BlockFeatureVectorEncoder featureEncoder;
  private final AdaptiveLogisticRegression model;

  public LogisticRegressionTrainer(Configuration conf) {
    this.conf = conf;
    this.modelFile = conf.get("scent.sgd.train.base.dir") + File.separator + "html-blocks-sgd.model";
    this.sampleBaseDir = conf.get("scent.sgd.train.base.dir") + File.separator + "segment";

    // this.probes = conf.getInt("scent.logistic.regression.classifier.probes", 10);

    featureEncoder = new BlockFeatureVectorEncoder("html-blocks", Indicator.names);
    this.probes = featureEncoder.numFeatures();
    featureEncoder.setProbes(this.probes);

    initFiles();

    model = new AdaptiveLogisticRegression(categories.size(), featureEncoder.numFeatures(), new L1());

    logger.info(String
        .format("\nmode file : %s\nsample base dir : %s\nprobes : %d\n", modelFile, sampleBaseDir, probes));
    logger.info("{} categories : {}", categories.size(), categories.values());
    logger.info("{} features : {}", featureEncoder.numFeatures(), featureEncoder.getFeatures());

    // TODO : configurable
    // model.setInterval(800);
    // model.setAveragingWindow(500);
  }

  public void train() throws IOException {
    Validate.notEmpty(files);

    logger.info(files.size() + " training files");
    SGDInfo info = new SGDInfo();

    int k = 0;
    for (File file : files) {
      int actualCategory = categories.intern(file.getParentFile().getName());
      model.train(actualCategory, featureEncoder.encode(file));

      SGDDissector.analyzeState(info, leakType, ++k, model.getBest());

//      if (++k > 10)
//        break;
    } // for

    new SGDDissector(conf).dissect(leakType, categories, model, files);

    ModelSerializer.writeBinary(modelFile, model.getBest().getPayload().getLearner().getModels().get(0));
  }

  private void initFiles() {
    for (File categorizedDir : new File(sampleBaseDir).listFiles()) {
      if (categorizedDir.isDirectory()) {
        categories.intern(categorizedDir.getName());
        files.addAll(Arrays.asList(categorizedDir.listFiles()));
      }
    }

    Collections.shuffle(files);
  }

  public static void main(String[] args) throws IOException {
    Configuration conf = ScentConfiguration.create();

    logger.info("begin to train...");
    LogisticRegressionTrainer trainer = new LogisticRegressionTrainer(conf);
    trainer.train();
  }
}
