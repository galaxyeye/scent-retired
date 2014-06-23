package org.qiwur.scent.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.math.Matrix;
import org.qiwur.scent.classifier.heuristics.CodeFeatureClassifier;
import org.qiwur.scent.classifier.semantic.BlockTextFeatureClassifier;
import org.qiwur.scent.classifier.semantic.BlockTitleFeatureClassifier;
import org.qiwur.scent.classifier.sgd.LogisticRegressionClassifier;
import org.qiwur.scent.classifier.statistics.CodeStructureFeatureClassifier;
import org.qiwur.scent.diagnosis.ClassifierMatrixFormatter;
import org.qiwur.scent.diagnosis.ScentDiagnoser;
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
  public void classify() {
    for (BlockClassifier classifier : classifiers) {
      classifier.classify();
    }

    calculateOverallScore();

    normalize(-100, 100);

    label();

    inheritLabels();
  }

  public void report(ScentDiagnoser diagnoser) {
    for (BlockClassifier classifier : classifiers) {
      diagnoser.addFormatter(new ClassifierMatrixFormatter(classifier, conf));
    }

    diagnoser.addFormatter(new ClassifierMatrixFormatter(this, conf));
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

        // important : only if the total score greater than 0, we use the max score for this cell
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
