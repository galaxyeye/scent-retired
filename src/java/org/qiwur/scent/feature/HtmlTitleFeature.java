package org.qiwur.scent.feature;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public final class HtmlTitleFeature {

  static final Logger logger = LogManager.getLogger(HtmlTitleFeature.class);

  //
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

  public static final int MinProductTitleSize = 10;

  public static final int MaxProductTitleSize = 200;

  // 移除括号中的内容，最小匹配
  public static final Pattern PAT_REMOVE_COMMENT_POTION = Pattern.compile("((（.+?）)|(【.+?】))");

  public static String WebsiteNameSuffixFile = "conf/website-name-suffix.txt";

  public static String TitleSuffixeFile = "conf/webpage-title-suffix.txt";

  Set<String> WebsiteNameSuffixes = new HashSet<String>();

  Set<String> TitleSuffixes = new HashSet<String>();

  static HtmlTitleFeature instance = null;

  private HtmlTitleFeature() {

  }

  public static HtmlTitleFeature getInstance() {
    if (instance == null) {
      instance = new HtmlTitleFeature();

      try {
        instance.load();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return instance;
  }

  private void load() throws IOException {
    List<String> lines = Files.readLines(new File(WebsiteNameSuffixFile), Charsets.UTF_8);
    WebsiteNameSuffixes.addAll(lines);

    lines = Files.readLines(new File(TitleSuffixeFile), Charsets.UTF_8);
    TitleSuffixes.addAll(lines);
  }

  public static boolean validate(String title) {
    // 商品标题，至少10个字符，最多200个字符
    if (title == null || title.length() <= MinProductTitleSize || title.length() >= MaxProductTitleSize) {
      return false;
    }

    return true;
  }

  // 预处理
  public static String preprocess(String title) {
    if (!validate(title))
      return "";

    title = StringUtil.trimNonChar(title, "[]()【】（）");

    return title;
  }

  // 移除标题后缀，如xx网
  public String removeSuffix(String title) {
    if (!validate(title))
      return "";

    int lastTitleLength = title.length();
    boolean removed = false;

    // 在词汇表中
    for (String suffix : HtmlTitleFeature.getInstance().TitleSuffixes) {
      title = StringUtils.chomp(title, suffix);

      if (title.length() < lastTitleLength) {
        // logger.debug("Suffix {} removed", suffix);

        removed = true;
        break;
      }
    }

    // 在词汇表中
    if (!removed) {
      for (String suffix : HtmlTitleFeature.getInstance().WebsiteNameSuffixes) {
        title = StringUtils.chomp(title, suffix);

        if (title.length() < lastTitleLength) {
          // logger.debug("Suffix {} removed", suffix);

          removed = true;
          break;
        }
      }
    }

    // 不在词汇表中，删除最后一个"-"以后的部分
    if (!removed) {
      int pos = StringUtils.lastIndexOf(title, '-');
      if (pos > title.length() / 2) {
        String suffix = title.substring(pos + 1).replaceAll("\\s+", "");

        // 5个字符网站名的案例：（暂时没找到）
        if (suffix.length() <= 5) {
          title = title.substring(0, pos);

          // logger.debug("Suffix {} removed", suffix);
        }
      }
    }

    if (!validate(title))
      title = "";

    return title;
  }

  // 在<title>标签中的字符串往往会增加一些无用信息，这里将整个字符串进行分解，从中找到潜在的主体信息
  // 比较长的放到前面
  public Set<String> getPotentialTitles(String title) {
    Set<String> potentialTitles = new TreeSet<String>(new Comparator<String>() {
      @Override
      public int compare(String s, String s2) {
        return new Integer(s2.length()).compareTo(s.length());
      }
    });

    if (!validate(title)) {
      return potentialTitles;
    }

    // 移除后缀的 -xx网
    title = removeSuffix(title);

    // 移除无效字符
    title = preprocess(title);

    if (!validate(title)) {
      return potentialTitles;
    }

    potentialTitles.add(title);

    for (Pattern pattern : PotentialTitlePatterns) {
      String p = StringUtil.getLongestPart(title, pattern);
      if (validate(p)) {
        potentialTitles.add(p);
      }
    }

    // 将括号中的部分去除，然后分解
    title = PAT_REMOVE_COMMENT_POTION.matcher(title).replaceAll("").trim();

    if (!validate(title)) {
      return potentialTitles;
    }

    potentialTitles.add(title);

    for (Pattern pattern : PotentialTitlePatterns) {
      String p = StringUtil.getLongestPart(title, pattern);
      if (validate(p)) {
        potentialTitles.add(p);
      }
    }
    logger.debug("potential titles : {}", potentialTitles);

    return potentialTitles;
  } // getPotentialTitles
}
