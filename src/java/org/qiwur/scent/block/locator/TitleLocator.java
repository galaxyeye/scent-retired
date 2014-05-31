package org.qiwur.scent.block.locator;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.feature.HtmlTitleFeature;
import org.qiwur.scent.feature.EntityNameFeature;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.parser.Tag;
import org.qiwur.scent.jsoup.select.ElementTraversor;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.jsoup.select.InterruptiveElementVisitor;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;
import ruc.irm.similarity.sentence.SentenceSimilarity;
import ruc.irm.similarity.sentence.morphology.MorphoSimilarity;

/**
 * 通过比较文本块和网页标题的相似度来找到商品标题
 * 
 */
public final class TitleLocator extends BlockLocator {

  @SuppressWarnings("unchecked")
  private static final Comparator<Double> ReversedDoubleComparator = ComparatorUtils
      .reversedComparator(ComparatorUtils.NATURAL_COMPARATOR);

  private TreeMap<Double, Element> titleElementCandidates = new TreeMap<Double, Element>(ReversedDoubleComparator);

  private final Configuration conf;
  private final EntityNameFeature productNameFeature;
  private final HtmlTitleFeature htmlTitleFeature;
  private Set<String> potentialTitles = null;

  private Element titleElement = null;

  public TitleLocator(Document doc, Configuration conf) {
    super(doc, BlockLabel.Title);

    this.conf = conf;
    productNameFeature = EntityNameFeature.create(conf);
    htmlTitleFeature = HtmlTitleFeature.create(conf);
  }

  @Override
  protected DomSegment quickLocate() {
    DomSegment segment = null;

    final String[] tagNames = { "h1", "h2", "h3" };
    if (potentialTitles == null) {
      potentialTitles = htmlTitleFeature.getPotentialTitles(doc.title());
    }
    final Elements candidates = productNameFeature.getSortedHeadElements(doc, tagNames);

    // logger.debug(candidates);

    double sim = 0.0;
    for (Element h : candidates) {
      if (!FuzzyProbability.veryLikely(sim)) {
        sim = EntityNameFeature.getMaxSimilarity(h.ownText(), potentialTitles);
      }

      if (FuzzyProbability.veryLikely(sim)) {
        segment = new DomSegment(null, null, h);
        segment.tag(targetLabel, sim);

        return segment;
      }
    } // for

    return segment;
  }

  @Override
  protected DomSegment deepLocate() {
    DomSegment segment = null;

    // 计算标题候选项
    if (potentialTitles == null) {
      potentialTitles = htmlTitleFeature.getPotentialTitles(doc.title());
    }

    // 第二步：从所有标签中寻找，这一步要么找到商品标题，要么收集了候选项
    if (titleElement == null) {
      new ElementTraversor(new ProductTitleFounder()).traverse(doc());
    }

    // 第三步：如果没有找到，那么从候选项中寻找一个作为商品标题
    if (titleElement == null && !titleElementCandidates.isEmpty()) {
      // TODO : 利用其他辅助信息来判断标题项，此处简单地讲相似度最大的项判断为标题项
      titleElement = titleElementCandidates.firstEntry().getValue();
    }

    if (titleElement != null) {
      segment = new DomSegment(null, null, titleElement);

      // No chance to consider any other candidate, this is the final
      // choose
      segment.tag(targetType(), FuzzyProbability.MUST_BE);
    }

    return segment;
  }

  public static DomSegment createTitle(Document doc, Configuration conf) {
    Element body = doc.getElementsByTag("body").first();

    if (body == null) {
      logger.error("bad document");
      return null;
    }

    Element ele = body.prependElement("h1");
    ele.sequence(body.sequence() + 1);

    String title = doc.title();

    HtmlTitleFeature titleFeature = HtmlTitleFeature.create(conf);
    title = titleFeature.removeSuffix(title);
    title = titleFeature.trim(title);
    ele.text(title);

    DomSegment segment = new DomSegment(null, null, ele);
    segment.tag(BlockLabel.Title, FuzzyProbability.MUST_BE);

    return segment;
  }

  private class ProductTitleFounder extends InterruptiveElementVisitor {

    private Element body = null;

    public void head(Element e, int depth) {
      if (shouldStop(e)) {
        stopped = true;
        return;
      }

      Element candidate = findCandidateTitle(e);
      if (candidate != null) {
        // TODO : also check candidate.text()
        String text = productNameFeature.preprocess(candidate.ownText());

        if (htmlTitleFeature.validate(text)) {
          titleElement = evaluateCandidate(text, candidate);

          if (titleElement != null) {
            stopped = true;
          }
        }
      } // local title not null
    }

    private boolean shouldStop(Element e) {
      if (body == null) {
        body = doc.select("body").first();
        if (body == null) {
          logger.error("bad document");
          return true;
        }
      }

      if (titleElement != null || potentialTitles == null) {
        return true;
      }

      if (e.sequence() > 0.5 * body.indic(Indicator.D)) {
        logger.debug("tooo far away the beginnig to find a title, abort. sequence : {}", e.sequence());
        return true;
      }

      return false;
    }

    /*
     * 如果e可能包含标题文本，那么返回true，否则返回false
     */
    private Element findCandidateTitle(Element e) {
      if (e == null)
        return null;

      Element candidate = null;

      if (!StringUtil.in(e.tagName(), "p", "div", "h1", "h2", "h3", "h4", "h5", "h6")) {
        return null;
      }

      // 包含商品名的节点不会太复杂，但可能会有strong, span，b，em等标签
      if (e.childNodeSize() >= 4) {
        return null;
      }

      // 缩小范围，如果孩子中有h1 ~ h6，那么范围缩小，以最大的h标签为准
      for (Element child : e.children()) {
        if (candidate != null)
          break;

        for (String h : Tag.headerTags) {
          if (child.tagName() == h) {
            titleElement = child;
            break;
          }
        }
      }

      if (candidate == null)
        candidate = e;

      return candidate;
    } // end findPotentialText

    private Element evaluateCandidate(String text, Element candidate) {
      if (text == null || candidate == null)
        return null;

      // 计算相似度
      double sim = 0.0;
      for (String potentialTitle : potentialTitles) {
        if (!SentenceSimilarity.isValidLengthRate(text, potentialTitle))
          continue;

        sim = MorphoSimilarity.getInstance().getSimilarity(text, potentialTitle);

        // 如果有一个候选项完全不匹配，那么可以断定其他候选项也不匹配
        // TODO:数据检验
        if (FuzzyProbability.strictlyNot(sim)) {
          logger.warn("break the loop since the candidate <{}> is strictly not the potential title <{}>", text,
              potentialTitle);

          return null;
        }

        // 如果找不到相似度为veryLikely的文本，那么使用相似度最高的文本
        if (FuzzyProbability.maybe(sim)) {
          titleElementCandidates.put(sim, candidate);
        }

        // if it's very likely a candidate, no need to check others
        if (FuzzyProbability.veryLikely(sim)) {
          logger.debug("find one : {}, {}", sim, text);
          return candidate;
        }
      } // end for

      return null;
    }

  } // ProductTitleFounder
}
