package org.qiwur.scent.classifier;

import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SparseMatrix;
import org.apache.mahout.math.function.DoubleFunction;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.select.DOMUtil;
import org.qiwur.scent.utils.MatrixUtil;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;

public abstract class BlockClassifier {

  protected static final Logger logger = LogManager.getLogger(BlockClassifier.class);

	protected final DomSegment[] segments;

  protected final String[] labels;

  protected final Configuration conf;

  protected Matrix scoreMatrix;

  protected double weight = 1.0;

	public BlockClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
	  this.segments = segments;
	  this.labels = labels;
		this.conf = conf;

		scoreMatrix = new SparseMatrix(segments.length, labels.length);
	}

  public DomSegment[] segments() {
    return segments;
  }

	public String[] labels() {
	  return labels;
	}

	public Matrix scoreMatrix() {
	  return scoreMatrix;
	}

  public void weight(double weight) {
    this.weight = weight;
  }

	public double weight() {
	  return weight;
	}

	public String getLabel(int segmentIndex) {
	  int index = scoreMatrix.viewRow(segmentIndex).maxValueIndex();

	  return labels[index];
	}

  public double getLabelScore(int segmentIndex) {
    return scoreMatrix.viewRow(segmentIndex).maxValue();
  }

  public void classify() {
    if (weight == 0.0) return;

    for (int row = 0; row < segments.length; ++row) {
      for (int col = 0; col < labels.length; ++col) {
        double score = getScore(segments[row], labels[col]);
        if (score > 10) score = 10;
        if (score < -10) score = -10;

        scoreMatrix.set(row, col, score / 10);
      }
    }
  }

  protected Matrix normalize(double min, double max) {
    max = MatrixUtil.getMaxValue(scoreMatrix, max);
    scoreMatrix.assign(new ScoreMatrixNormalizer(min, max));

    return scoreMatrix;
  }

  protected void label() {
    for (int row = 0; row < segments.length; ++row) {
      for (int col = 0; col < labels.length; ++col) {
        double likelihood = scoreMatrix.get(row, col);
        if (FuzzyProbability.maybe(likelihood)) {
          segments[row].tag(BlockLabel.fromString(labels[col]), likelihood);
        }
      }
    }
  }

  protected void inheritLabels() {
    for (DomSegment segment : segments) {
      for (DomSegment segment2 : segments) {
        if (DOMUtil.isAncestor(segment2, segment)) {
          // segment2 is a decendant of segment, copy all labels from the ancestor
          for (BlockLabel label : segment.labelTracker().keySet()) {
            if (label.inheritable()) {
              segment2.tag(label, segment.labelTracker().get(label));
            }
          }
        }
      }
    }
  }

  public abstract Collection<String> labelsInCharge();

  protected abstract double getScore(DomSegment segment, String label);

  public String getMatrixString() {
    return getMatrixString(" ");
  }

  public String getMatrixString(String columnSeparator) {
    String[] diagnoseLabels = conf.getStrings("scent.diagnose.classifier.block.labels");

    String report = "";
    report += String.format("%-30s%s", getClass().getSimpleName(), columnSeparator);
    for (int col = 0; col < labels.length; ++col) {
      if (!ArrayUtils.contains(diagnoseLabels, labels[col])) {
        continue;
      }

      report += String.format("%-12s%s", StringUtils.substring(labels[col], 0, 12), columnSeparator);
    }
    report += "\n";

    for (int row = 0; row < segments.length; ++row) {
      report += String.format("%-30s%s", StringUtils.substring(segments[row].name(), 0, 30), columnSeparator);
      for (int col = 0; col < labels.length; ++col) {
        if (!ArrayUtils.contains(diagnoseLabels, labels[col])) {
          continue;
        }

        double score = scoreMatrix.get(row, col);
        if (score == 0) {
          report += String.format("%-14s", ".");
        }
        else if (score < 0.6) {
          report += String.format("%-12.2f", score);
        }
        else {
          report += String.format("<b>%-4.2f</b>", score);
        }

        report += columnSeparator;
      } // for

      report += "\n";
    } // for

    return report;
  }

  class ScoreMatrixNormalizer extends DoubleFunction {
    private final double min;
    private final double max;

    ScoreMatrixNormalizer(double min, double max) {
      this.min = min;
      this.max = max;
    }

    @Override
    public double apply(double x) {
      if (max == 0) return 0;

      if (x > max) x = max;
      if (x < min) x = min;

      return x / max;
    }
  };
}
