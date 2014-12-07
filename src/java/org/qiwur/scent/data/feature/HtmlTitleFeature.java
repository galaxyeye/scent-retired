package org.qiwur.scent.data.feature;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multiset;

public final class HtmlTitleFeature extends LinedFeature {

  static final Logger logger = LogManager.getLogger(HtmlTitleFeature.class);

  public static final Pattern PAT_REMOVE_CHARACTERS = Pattern.compile("[\\?？\\!！\\.。\\-\\:：]+");

  // 标题中常见的分隔符号，这些分隔符号往往带有一些低价值信息，
  // 如：【索尼PJ390E】索尼（SONY） HDR-PJ390E 投影高清数码摄像机 白色（239万像素 3英寸屏 30倍光学变焦 32G内存）【行情
  // 报价 价格 评测】-京东商城
  // 我们需要找到核心信息
  public static final Pattern[] PotentialTitlePatterns = {
      // 最精细分隔
      Pattern.compile("[ ]*[»,，:：_（）【】\\|\\-\\(\\)][ ]*"),
      // 相比上一个，少了-。某些标题中会带有范围参数如：索尼 NEX-3NL 16-50mm 单变焦镜头
      Pattern.compile("[ ]*[»,，:：_（）【】\\|\\(\\)][ ]*"),
      // 少了:和：
      Pattern.compile("[ ]*[»,，_（）【】\\|\\(\\)][ ]*"),
      // 少了_
      Pattern.compile("[ ]*[»,，（）【】\\|\\(\\)][ ]*"),
      // 最少分隔
      Pattern.compile("[ ]*[»【】][ ]*"), };

  public static final int MinTitleSize = 6;

  public static final int MaxTitleSize = 200;

  // 移除括号中的内容，最小匹配
  public static final Pattern PAT_REMOVE_COMMENT_POTION = Pattern.compile("((（.+?）)|(【.+?】))");

  public HtmlTitleFeature(Configuration conf, String[] featureFile) {
    super(conf, featureFile);
  }

  public boolean validate(String title) {
    // 商品标题，至少10个字符，最多200个字符
    if (title == null || title.length() <= MinTitleSize || title.length() >= MaxTitleSize) {
      return false;
    }

    return true;
  }

  // 在<title>标签中的字符串往往会增加一些无用信息，这里将整个字符串进行分解，从中找到潜在的主体信息
  // 比较长的放到前面
  public Set<String> getPotentialTitles(String title) {
    Set<String> potentialTitles = new TreeSet<String>(StringUtil.LongerFirstComparator);

    // step 1. add the whole original title
    potentialTitles.add(title);

    // 移除后缀的 -xx网
    String candidate = strip(title);
    if (!validate(candidate)) {
      return potentialTitles;
    }

    // step 2. add the whole title preprocessed title
    potentialTitles.add(candidate);
    // split into two parts, since some web page have repeated texts in the title
    int middle = candidate.length() / 2;
    potentialTitles.add(StringUtils.substring(candidate, 0, middle));
    potentialTitles.add(StringUtils.substring(candidate, middle));

    // step 3, split by patterns, find out the longest part for each pattern
    for (Pattern pattern : PotentialTitlePatterns) {
      String p = StringUtil.getLongestPart(candidate, pattern);
      if (validate(p)) {
        potentialTitles.add(p);
      }
    }

    // step 4, remove text in brackets, and then split by patterns, find out the longest part for each pattern
    // 将括号中的部分去除，然后分解
    candidate = PAT_REMOVE_COMMENT_POTION.matcher(title).replaceAll("").trim();
    if (!validate(candidate)) {
      return potentialTitles;
    }
    potentialTitles.add(candidate);
    for (Pattern pattern : PotentialTitlePatterns) {
      String p = StringUtil.getLongestPart(candidate, pattern);
      if (validate(p)) {
        potentialTitles.add(p);
      }
    }

    // logger.debug("potential titles : {}", potentialTitles);

    return potentialTitles;
  } // getPotentialTitles

  public Multiset<String> badWords() {
    return lines();
  }

  public String strip(String title) {
    int lastTitleLength = title.length();
    boolean removed = false;

    // 在词汇表中
    for (String word : badWords()) {
      title = StringUtils.remove(title, word);

      if (title.length() < lastTitleLength) {
        removed = true;
      }
    }

    // 不在词汇表中，删除最后一个"-"以后的部分
    if (!removed) {
      int pos = StringUtils.lastIndexOf(title, '-');
      if (pos > title.length() / 2) {
        title = title.substring(0, pos);
      }
    }

    if (!validate(title)) title = "";

    return trim(title);
  }

  // 预处理
  public String trim(String title) {
    if (!validate(title))
      return "";

    title = StringUtil.trimNonChar(title, "[]()【】（）");

    return title;
  }
}
