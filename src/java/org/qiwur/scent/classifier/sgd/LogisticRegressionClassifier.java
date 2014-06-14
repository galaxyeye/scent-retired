package org.qiwur.scent.classifier.sgd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.Vector;
import org.qiwur.scent.classifier.BlockClassifier;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Indicator;

public class LogisticRegressionClassifier extends BlockClassifier {

  private final String modelFile;
  private final int probes;
  private final BlockFeatureVectorEncoder featureEncoder;

  public LogisticRegressionClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
    super(segments, labels, conf);
    this.modelFile = conf.get("scent.sgd.train.base.dir") + File.separator + "html-blocks-sgd.model";

    // this.probes = conf.getInt("scent.logistic.regression.classifier.probes", 2);
    featureEncoder = new BlockFeatureVectorEncoder("html-blocks", Indicator.names);
    this.probes = featureEncoder.numFeatures();
    featureEncoder.setProbes(probes);

    double weight = conf.getFloat("scent.logistic.regression.classifier.weight", 1.0f);
    this.weight(weight);
  }

  @Override
  public void classify() {
    if (!conf.getBoolean("scent.logistic.regression.classifier.enabled", false)) {
      return;
    }

    OnlineLogisticRegression classifier;
    try {
      classifier = ModelSerializer.readBinary(new FileInputStream(modelFile), OnlineLogisticRegression.class);
    } catch (IOException e) {
      logger.error(e);

      return;
    }

    // resultMatrix = new DenseMatrix(domSegments().size(), labels.length);
    for (int i = 0; i < segments.length; ++i) {
      DomSegment segment = segments[i];

      Vector result = classifier.classifyFull(featureEncoder.encode(segment.outerHtml()));
      scoreMatrix.assignRow(i++, result);

      // int cat = result.maxValueIndex();
      // double score = result.maxValue();
      // double ll = classifier.logLikelihood(actual, input);
      //
      // ClassifierResult cr = new
      // ClassifierResult(newsGroups.values().get(cat), score, ll);
      // resultAnalyzer.addInstance(newsGroups.values().get(actual), cr);
    }
  }

  @Override
  protected double getScore(DomSegment segment, String label) {
    return 0;
  }

  @Override
  public Collection<String> labelsInCharge() {
    return Arrays.asList(labels);
  }
}
