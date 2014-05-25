package org.qiwur.scent.feature;

import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.utils.StringUtil;

public class WordFeature {

  private Configuration conf;
  private int minLength = 1;
  private int maxLength = 10;
  private String pattern = null;
  private Set<String> blackList = new TreeSet<String>(StringUtil.LongerFirstComparator);

  public WordFeature(Configuration conf) {
    this.conf = conf;
  }

  public int minLength() {
    return minLength;
  }

  public int maxLength() {
    return maxLength;
  }

  public Set<String> blackList() {
    return blackList;
  }

  public String trim(String word) {
    return StringUtil.trimNonChar(word, StringUtil.DefaultKeepChars);
  }

  public String strip(String word) {
    return StringUtil.stripNonChar(word, StringUtil.DefaultKeepChars);
  }

  public boolean validate(String word) {
    if (word == null || word.length() <= minLength || word.length() >= maxLength) {
      return false;
    }

    if (pattern != null) {
      return word.matches(pattern);
    }

    return true;
  }

  // 预处理
  public String preprocess(String word) {
    if (!validate(word))
      return "";

    word = StringUtil.stripNonChar(word, StringUtil.DefaultKeepChars);
    for (String bad : blackList()) {
      word = word.replace(bad, "");
    }

    if (!validate(word))
      return "";

    return word;
  }
}
