package org.qiwur.scent.feature;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.FiledLines;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multiset;

public class LinedFeature {

  protected static final Logger logger = LogManager.getLogger(LinedFeature.class);

  private final String file;
  private FiledLines filedLines = null;

  public LinedFeature(String file) {
    this.file = file;

    try {
      filedLines = new FiledLines(StringUtil.LongerFirstComparator, file);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  public boolean contains(String text) {
    return filedLines.contains(file, text);
  }

  public Multiset<String> lines() {
    return filedLines.getLines(file);
  }
}
