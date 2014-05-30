package org.qiwur.scent.learning;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.FiledLines;

import com.google.common.collect.Multiset;

public final class WordsLearner {

  public static final Logger logger = LogManager.getLogger(WordsLearner.class);

  public static final String LearningFileDir = "output" + File.separator + "learning";

  private final Configuration conf;

  private FiledLines filedLines = null;

  private Map<LearningDomain, String> featureFiles = new HashMap<LearningDomain, String>();

  public WordsLearner(Configuration conf) {
    this.conf = conf;

    // TODO : use config
    for (LearningDomain domain : LearningDomain.values()) {
      // ProductAttribute -> output/learning/product-property.txt
      String[] parts = domain.text().split("(?=\\p{Upper})");
      String fileName = StringUtils.join(parts, "-").toLowerCase();
      if (fileName.startsWith("-")) {
        fileName = fileName.substring(1);
      }
      fileName += ".txt";
      fileName = LearningFileDir + File.separator + fileName;

      // create if not exist
      File file = new File(fileName);
      if (!file.exists()) {
        try {
          file.createNewFile();
        } catch (IOException e) {
          logger.error(e);
        }
      }

      featureFiles.put(domain, fileName);
    }

    String[] files = new String[featureFiles.values().size()];
    featureFiles.values().toArray(files);
    filedLines = new FiledLines(files);
  }

  public String featureFile(LearningDomain domain) {
    return featureFiles.get(domain);
  }

  public Multiset<String> words(LearningDomain domain) {
    return filedLines.getLines(featureFile(domain));
  }

  public void learn(LearningDomain domain, Collection<String> words) {
    for (String w : words) {
      learn(domain, w);
    }
  }

  public void learn(LearningDomain domain, String words) {
    boolean ok = filedLines.add(featureFile(domain), words);

    if (!ok) {
      logger.warn("can not add words");
    }
  }

  public void save() {
    try {
      filedLines.saveAll();
    } catch (IOException e) {
      logger.error(e);
    }
  }
}
