package org.qiwur.scent.feature;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.utils.FiledLines;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;
import ruc.irm.similarity.sentence.SentenceSimilarity;
import ruc.irm.similarity.sentence.morphology.MorphoSimilarity;

import com.google.common.collect.Multiset;

public final class ProductNameFeature extends FiledLines {

  static final Logger logger = LogManager.getLogger(ProductNameFeature.class);

  public static final int MinProductTitleSize = 10;

  public static final int MaxProductTitleSize = 200;

  // 移除括号中的内容，最小匹配
  public static final Pattern PAT_REMOVE_COMMENT_POTION = Pattern.compile("((（.+?）)|(【.+?】))");

  public static final String BadProductNameWordsFile = "conf/bad-product-name-words.txt";

  static ProductNameFeature instance = null;

  private ProductNameFeature() throws IOException {
    super(StringUtil.LongerFirstComparator, BadProductNameWordsFile);
  }

  public static ProductNameFeature getInstance() {
    if (instance == null) {
      try {
        instance = new ProductNameFeature();
      } catch (IOException e) {
        logger.error(e);
      }
    }

    return instance;
  }

  public static Multiset<String> badWords() {
    return getInstance().getLines(BadProductNameWordsFile);
  }

  public static double getTitleSimilarity(String text, String text2) {
    Set<String> set = new HashSet<String>();
    set.add(text2);
    return getTitleSimilarity(text, set);
  }

  public static double getTitleSimilarity(String text, Set<String> potentialTitles) {
    double sim = 0.0;

    // 由于相似度计算性能太差，所以使用一些加速的方法
    // 只要一个潜在标题是目标项的字串并且长度是目标项的70%，那么认为目标项是标题项
    for (String potentialTitle : potentialTitles) {
      String text2 = text.toLowerCase();
      potentialTitle = potentialTitle.toLowerCase();

      if (text2.contains(potentialTitle)) {
        sim = potentialTitle.length() / (double) text.length() + 0.1;

        logger.trace("sim by length : {}, protential : {}, header : {}", sim, potentialTitle, text2);

        if (FuzzyProbability.veryLikely(sim))
          break;
      }
    }

    // 句子包含方法没有找到，使用语义计算方法
    if (!FuzzyProbability.veryLikely(sim)) {
      logger.trace("we didn't find the similar name, try semantic based lookup");

      for (String potentialTitle : potentialTitles) {
        if (!SentenceSimilarity.isValidLengthRate(text, potentialTitle))
          continue;

        // 耗时子过程，原因是相似度计算效率太差，一个典型的句子（譬如商品标题）相似度计算需耗时半秒钟
        sim = MorphoSimilarity.getInstance().getSimilarity(potentialTitle, text);

        logger.trace("sim by similartiy : {}, protential : {}, header : {}", sim, potentialTitle, text);

        // 1. 候选项和潜在标题风牛马不相及, 2. 已经找到标题项
        if (FuzzyProbability.veryLikely(sim)) {
          break;
        }
      }
    }

    return sim;
  }

  // tags should be header tags : h1~6
  // NOTICE : this changes the original DOM
  public static Elements getSortedHeadElements(Element root, String[] tagNames) {
    Elements rawCandidates = root.getElementsByAnyTag(tagNames);

    Elements candidates = new Elements();
    for (Element h : rawCandidates) {
      // TODO : we can do better here
      String name = preprocess(h.ownText());
      if (validate(name)) {
        // NOTICE : this changes the original DOM
        h.text(name);
        candidates.add(h);
      }
    }

    Collections.sort(candidates, new Comparator<Element>() {
      @Override
      public int compare(Element e, Element e2) {
        return e.tagName().compareTo(e2.tagName());
      }
    });

    return candidates;
  }

  public static boolean validate(String name) {
    // 商品标题，至少10个字符，最多200个字符
    if (name == null || name.length() <= MinProductTitleSize || name.length() >= MaxProductTitleSize) {
      return false;
    }

    return true;
  }

  // 预处理
  public static String preprocess(String name) {
    if (!validate(name))
      return "";

    // 去除括号以及括号中的部分
    // name = PAT_REMOVE_COMMENT_POTION.matcher(name).replaceAll("");
    // 去除两端的空白
    name = StringUtil.trimNonChar(name, "[]()【】（）");

    // 去除黑名单中的词语
    for (String word : badWords()) {
      name = name.replace(word, "");
    }

    if (!validate(name))
      return "";

    return name;
  }
}
