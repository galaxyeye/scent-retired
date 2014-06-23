package org.qiwur.scent.block.locator;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.feature.EntityNameFeature;
import org.qiwur.scent.feature.FeatureManager;
import org.qiwur.scent.feature.HtmlTitleFeature;
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

/**
 * 通过比较文本和网页标题的相似度来找到标题
 */
public final class TitleLocator extends BlockLocator {

  private final Configuration conf;  
  private final HtmlTitleFeature htmlTitleFeature;

  private final Set<String> potentialTitles;
  private final TreeMap<Double, Element> candiateTitles = new TreeMap<Double, Element>(Collections.reverseOrder());

  public TitleLocator(Document doc, Configuration conf) {
    super(doc, BlockLabel.Title);
    this.conf = conf;

    htmlTitleFeature = FeatureManager.get(conf, HtmlTitleFeature.class, conf.get("scent.bad.html.title.feature.file"));

    potentialTitles = htmlTitleFeature.getPotentialTitles(doc.title());
  }

  @Override
  protected DomSegment quickLocate() {
    Elements candidates = doc.select("h1,h2,h3");

    // let h1 comes first, then h2, and then h3
    Collections.sort(candidates, new Comparator<Element>() {
      @Override
      public int compare(Element e, Element e2) {
        return e.tagName().compareToIgnoreCase(e2.tagName());
      }
    });

    boolean found = false;
    for (Element h : candidates) {
      found = collectCandidate(h);
      if (found) break;
    }

    return found ? getTitleSegment() : null;
  }

  @Override
  protected DomSegment deepLocate() {
    new ElementTraversor(new EntityTitleFounder(doc)).traverse(doc());

    return getTitleSegment();
  }

  public static DomSegment createTitle(Document doc, Configuration conf) {
    Element ele = doc.body().prependElement("h1");
    ele.sequence(doc.body().sequence() + 200); // TODO : use a machine learned sequence

    String featureFile = conf.get("scent.html.title.feature.file");
    HtmlTitleFeature titleFeature = FeatureManager.get(conf, HtmlTitleFeature.class, featureFile);
    ele.html(titleFeature.strip(doc.title()));

    return DomSegment.create(ele, BlockLabel.Title, FuzzyProbability.MUST_BE);
  }

  private boolean collectCandidate(Element ele) {
    if (ele == null) return false;

    double sim = 0.0, sim2 = 0.0, sim3 = 0.0;
    String name = ele.text(), name2 = ele.ownText();

    // TODO : optimization
    sim2 = EntityNameFeature.getMaxSimilarity(name, potentialTitles);
    if (name.length() > name2.length()) {
      sim3 = EntityNameFeature.getMaxSimilarity(name2, potentialTitles);
    }
    sim = Math.max(sim2, sim3);

    if (FuzzyProbability.maybe(sim)) {
      candiateTitles.put(sim, ele);
    }

    boolean found = FuzzyProbability.veryLikely(sim);
    if (found && sim3 > sim2) {
      ele.html(name2);
    }

    return found;
  }

  private DomSegment getTitleSegment() {
    if (candiateTitles.isEmpty()) return null;

    Entry<Double, Element> eleTitle = candiateTitles.firstEntry();
    DomSegment segTitle = new DomSegment(null, null, eleTitle.getValue());
    segTitle.tag(targetLabel(), eleTitle.getKey());

    return segTitle;
  }

  private class EntityTitleFounder extends InterruptiveElementVisitor {

    private final Document doc;
    private final Element body;

    public EntityTitleFounder(Document doc) {
      this.doc = doc;
      body = doc.body();

      Validate.notNull(body);
    }

    public void head(Element e, int depth) {
      if (shouldStop(e)) {
        return;
      }

      boolean found = collectCandidate(findCandidateTitle(e));
      if (found) stop();
    }

    private boolean shouldStop(Element e) {
      // 2/3
      if (e.sequence() > 0.6667 * body.indic(Indicator.D)) {
        logger.debug("tooo far away from the beginning to find a title, abort. sequence : {}", e.sequence());
        return true;
      }

      return stopped();
    }

    /*
     * 如果e可能包含标题文本，那么返回true，否则返回false
     */
    private Element findCandidateTitle(Element e) {
      if (e == null) return null;

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
        if (candidate != null) break;

        for (String h : Tag.headerTags) {
          if (child.tagName() == h) {
            candidate = child;
            break;
          }
        }
      }

      if (candidate == null) {
        candidate = e;
      }

      return candidate;
    }
  } // EntityTitleFounder
  
}
