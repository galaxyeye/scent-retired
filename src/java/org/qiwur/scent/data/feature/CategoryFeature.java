package org.qiwur.scent.data.feature;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.StringUtil;

public final class CategoryFeature extends LinedFeature {

  static final Logger logger = LogManager.getLogger(CategoryFeature.class);

  public static final int MaxCategoryWordSize = 10;

  private String BadCategoryWordsFile = "conf/feature/default/bad-category-words.txt";

  public CategoryFeature(Configuration conf, String[] featureFile) {
    super(conf, featureFile);

    this.BadCategoryWordsFile = conf.get("scent.bad.category.words.file", "conf/feature/default/bad-category-words.txt");
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
