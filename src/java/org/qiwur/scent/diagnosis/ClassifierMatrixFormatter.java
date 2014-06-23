package org.qiwur.scent.diagnosis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.classifier.BlockClassifier;

import com.google.common.collect.Lists;

public class ClassifierMatrixFormatter extends DiagnosisFormatter {

  private static String columnSeparator = "^^";
  private final BlockClassifier classifier;

  public ClassifierMatrixFormatter(BlockClassifier classifier, Configuration conf) {
    super(conf);

    this.classifier = classifier;
  }

  @Override
  public void process() {
    ArrayList<String> lines = getLines();

    setCaption(getClass().getName() + " - " + classifier.getClass().getSimpleName());

    int i = 0;
    for (String line : lines) {
      if (i++ == 0) {
        buildHeader(Arrays.asList(StringUtils.split(line, columnSeparator)));
      }
      else {
        buildRow(Arrays.asList(StringUtils.split(line, columnSeparator)));
      }
    }
  }

  private ArrayList<String> getLines() {
    ArrayList<String> lines = Lists.newArrayList();

    try {
      InputStream in = IOUtils.toInputStream(classifier.getMatrixString(columnSeparator), "UTF-8");
      lines.addAll(IOUtils.readLines(in));
    }
    catch (IOException e) {
      logger.error(e);
    }

    return lines;
  }
}
