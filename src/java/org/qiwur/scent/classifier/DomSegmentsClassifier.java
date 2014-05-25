package org.qiwur.scent.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Matrix;
import org.qiwur.scent.classifier.heuristics.CodeFeatureClassifier;
import org.qiwur.scent.classifier.semantic.BlockTextFeatureClassifier;
import org.qiwur.scent.classifier.semantic.BlockTitleFeatureClassifier;
import org.qiwur.scent.classifier.sgd.LogisticRegressionClassifier;
import org.qiwur.scent.classifier.statistics.CodeStructureFeatureClassifier;
import org.qiwur.scent.jsoup.block.DomSegment;

public class DomSegmentsClassifier extends BlockClassifier {

  private List<BlockClassifier> classifiers = new ArrayList<BlockClassifier>();

  public DomSegmentsClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
    super(segments, labels, conf);

    classifiers.add(new CodeFeatureClassifier(segments, labels, conf));
    classifiers.add(new CodeStructureFeatureClassifier(segments, labels, conf));
    classifiers.add(new BlockTitleFeatureClassifier(segments, labels, conf));
    classifiers.add(new BlockTextFeatureClassifier(segments, labels, conf));
    classifiers.add(new LogisticRegressionClassifier(segments, labels, conf));
  }

  @Override
  public void process() {
    for (BlockClassifier classifier : classifiers) {
      classifier.process();
    }

    calculateOverallScore();

    normalize(-100, 100);

    report();

    tagSegments();
  }

  public void report() {
    final Logger logger = LogManager.getLogger(DomSegmentsClassifier.class);

    for (BlockClassifier classifier : classifiers) {
      logger.debug(classifier.getMatrixString());
    }

    logger.debug(getMatrixString());
  }

  private void calculateOverallScore() {
    for (BlockClassifier classifier : classifiers) {
      Matrix matrix = classifier.scoreMatrix();

      // TODO : optimization?
      matrix = matrix.times(classifier.weight());
      scoreMatrix = scoreMatrix.plus(matrix);
    }

    for (int row = 0; row < segments.length; ++row) {
      for (int col = 0; col < labels.length; ++col) {
        double score = scoreMatrix.get(row, col);

        // important
        if (score > 0) {
          score = getMaxScore(row, col);
        }
        else {
          score = 0;
        }

        scoreMatrix.set(row, col, score);
      }
    }
  }

  private double getMaxScore(int segment, int label) {
    double maxScore = -10;

    for (BlockClassifier classifier : classifiers) {
      double score = classifier.scoreMatrix.get(segment, label);
      if (score > maxScore) {
        maxScore = score;
      }
    }

    return maxScore;
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
