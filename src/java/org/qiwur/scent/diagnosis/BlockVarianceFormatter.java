package org.qiwur.scent.diagnosis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.classifier.statistics.BlockVarianceCalculator;

import com.google.common.collect.Lists;

// TODO : may move to CSVFormatter
public class BlockVarianceFormatter extends DiagnosisFormatter {

  private static String columnSeparator = "^^";
  private final BlockVarianceCalculator calculator;

  public BlockVarianceFormatter(BlockVarianceCalculator calculator, Configuration conf) {
    super(conf);

    this.calculator = calculator;
  }

  @Override
  public void process() {
    ArrayList<String> lines = getLines();

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
      InputStream in = IOUtils.toInputStream(calculator.getVarianceString(), "UTF-8");
      lines.addAll(IOUtils.readLines(in));
    }
    catch (IOException e) {
      logger.error(e);
    }

    return lines;
  }
}
