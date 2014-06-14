package org.qiwur.scent.feature;

import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.FiledLines;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multiset;

public class LinedFeature implements WebFeature {

  protected static final Logger logger = LogManager.getLogger(LinedFeature.class);

  private final String featureFile;
  private Configuration conf;
  private FiledLines filedLines = new FiledLines();

  public LinedFeature(Configuration conf, String[] featureFile) {
    this.featureFile = featureFile[0];
    this.conf = conf;

    setPreprocessor(new FiledLines.Preprocessor() {
      @Override
      public String process(String line) {
        line = line.startsWith("#") ? "" : line.trim();
        return Pattern.quote(line);
      }
    });

    load();
  }

  public LinedFeature(Configuration conf, FiledLines.Preprocessor preprocessor, String... featureFile) {
    this.featureFile = featureFile[0];
    this.conf = conf;
    setPreprocessor(preprocessor);
    load();
  }

  public void setPreprocessor(FiledLines.Preprocessor preprocessor) {
    filedLines.setPreprocessor(preprocessor);
  }

  public boolean contains(String text) {
    return filedLines.contains(featureFile, text);
  }

  public Multiset<String> lines() {
    return filedLines.getLines(featureFile);
  }

  @Override
  public void reset() {
    filedLines.clear();
  }

  @Override
  public void load() {
    filedLines = new FiledLines(StringUtil.LongerFirstComparator, featureFile);
  }

  @Override
  public void reload() {
    filedLines = new FiledLines(StringUtil.LongerFirstComparator, featureFile);
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {    
    this.conf = conf;
  }
}
