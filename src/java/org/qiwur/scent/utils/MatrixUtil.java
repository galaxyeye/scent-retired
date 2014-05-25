package org.qiwur.scent.utils;

import org.apache.mahout.math.Matrix;

public class MatrixUtil {

  public static double getMaxValue(Matrix matrix, double max) {
    double maxValue = getMaxValue(matrix);

    if (maxValue > max) {
      maxValue = max;
    }

    return maxValue;
  }

  public static double getMaxValue(Matrix matrix) {
    double maxValue = Double.MIN_NORMAL;

    for (int i = 0; i < matrix.rowSize(); ++i) {
      for (int j = 0; j < matrix.columnSize(); ++j) {
        double value = matrix.get(i, j);
        if (value > maxValue) {
          maxValue = value;
        }
      }
    }

    return maxValue;
  }
}
