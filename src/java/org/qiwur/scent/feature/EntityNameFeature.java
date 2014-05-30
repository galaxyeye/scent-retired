package org.qiwur.scent.feature;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.utils.FiledLines;
import org.qiwur.scent.utils.ObjectCache;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;
import ruc.irm.similarity.sentence.editdistance.StandardEditDistance;
import ruc.irm.similarity.sentence.morphology.MorphoSimilarity;

import com.google.common.collect.Multiset;

public final class EntityNameFeature extends FiledLines {

  static final Logger logger = LogManager.getLogger(EntityNameFeature.class);

  public static final int MinProductTitleSize = 10;

  public static final int MaxProductTitleSize = 100;

  // 移除括号中的内容，最小匹配
  public static final Pattern PAT_REMOVE_COMMENT_POTION = Pattern.compile("((（.+?）)|(【.+?】))");

  public static final String BadProductNameWordsFile = "conf/bad-product-name-words.txt";

  private EntityNameFeature() {
    super(StringUtil.LongerFirstComparator, BadProductNameWordsFile);
  }

  public static EntityNameFeature create(Configuration conf) {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = EntityNameFeature.class.getName();

    if (objectCache.getObject(cacheId) != null) {
      return (EntityNameFeature) objectCache.getObject(cacheId);
    }
    else {
      EntityNameFeature feature = new EntityNameFeature();
      objectCache.setObject(cacheId, feature);
      return feature;
    }
  }

  public Multiset<String> badWords() {
    return getLines(BadProductNameWordsFile);
  }

  public static double getQuickSimilarity(String text, String text2) {
    Set<String> set = new HashSet<String>();
    set.add(text2);
    return getQuickSimilarity(text, set);
  }

  // 由于相似度计算性能太差，所以使用一些加速的方法
  // 只要一个潜在标题是目标项的字串并且长度是目标项的70%，那么认为目标项是标题项
  public static double getQuickSimilarity(final String text, Set<String> potentialTitles) {
    if (!validate(text)) return 0.0;

    double sim = 0.0;

    // 由于相似度计算性能太差，所以使用一些加速的方法
    // 只要一个潜在标题是目标项的字串并且长度是目标项的70%，那么认为目标项是标题项
    for (String potentialTitle : potentialTitles) {
      String text2 = StringUtil.stripNonChar(text).toLowerCase();
      potentialTitle = StringUtil.stripNonChar(potentialTitle).toLowerCase();

      if (validate(text2) && text2.contains(potentialTitle)) {
        sim = potentialTitle.length() / (double) text2.length() + 0.1;

        logger.debug("sim by length : {}, protential : {}, header : {}", sim, potentialTitle, text2);

        if (FuzzyProbability.veryLikely(sim)) {
          return sim;
        }
      }
    }

    return sim;
  }

  public static double getStandardEditSimilarity(String text, String text2) {
    if (Math.abs(text2.length() - text.length()) > 5) return 0.0;

    return new StandardEditDistance().getSimilarity(text, text2);
  }

  public static double getSimilarity(String text, String text2) {
    Set<String> set = new HashSet<String>();
    set.add(text2);
    return getSimilarity(text, set);
  }

  public static double getSimilarity(final String text, Set<String> potentialTitles) {
    if (!validate(text)) return 0.0;

    double sim = getQuickSimilarity(text, potentialTitles);
    if (FuzzyProbability.veryLikely(sim)) return sim;

    // 句子包含方法没有找到，使用语义计算方法
    logger.debug("try semantic based lookup");

    for (String potentialTitle : potentialTitles) {
      if (!validate(potentialTitle) || !validate(text)) {
        continue;
      }

      String text2 = StringUtil.stripNonChar(text).toLowerCase();
      potentialTitle = StringUtil.stripNonChar(potentialTitle).toLowerCase();

      // 耗时子过程，原因是相似度计算效率太差，一个典型的句子（譬如商品标题）相似度计算需耗时半秒钟
      sim = MorphoSimilarity.getInstance().getSimilarity(potentialTitle, text2);

      logger.debug("sim by similartiy : {}, protential : {}, header : {}", sim, potentialTitle, text2);

      // 已经找到标题项
      if (FuzzyProbability.veryLikely(sim)) {
        return sim;
      }
    }

    return sim;
  }

  // tags should be header tags : h1~6
  // NOTICE : this changes the original DOM
  public Elements getSortedHeadElements(Element root, String[] tagNames) {
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
  public String preprocess(String name) {
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
