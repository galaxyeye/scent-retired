package org.qiwur.scent.feature;

import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.FiledLines;
import org.qiwur.scent.utils.ObjectCache;
import org.qiwur.scent.utils.StringUtil;

public final class CategoryFeature extends LinedFeature {

  static final Logger logger = LogManager.getLogger(CategoryFeature.class);

  public static final int MaxCategoryWordSize = 10;

  public static final String BadCategoryWordsFile = "conf/bad-category-words.txt";

  private CategoryFeature() {
    super(BadCategoryWordsFile);
    setPreprocessor(new FiledLines.Preprocessor() {
      @Override
      public String process(String line) {
        line = line.startsWith("#") ? "" : line.trim();
        return Pattern.quote(line);
      }
    });
  }

  public static CategoryFeature create(Configuration conf) {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = BadCategoryWordsFile;

    if (objectCache.getObject(cacheId) != null) {
      return (CategoryFeature) objectCache.getObject(cacheId);
    }
    else {
      CategoryFeature feature = new CategoryFeature();
      objectCache.setObject(cacheId, feature);
      return feature;
    }
  }

  public String strip(String name) {
    int count = StringUtil.countChinese(name);

    if (name == null || count > MaxCategoryWordSize) {
      return "";
    }

    name = StringUtil.stripNonChar(name, StringUtil.DefaultKeepChars);
    for (String word : lines()) {
      name = name.replaceAll(word, "");
    }

    return name;
  }
}
