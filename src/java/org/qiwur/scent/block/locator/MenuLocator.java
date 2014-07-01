package org.qiwur.scent.block.locator;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.select.ElementTraversor;
import org.qiwur.scent.jsoup.select.InterruptiveElementVisitor;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

public final class MenuLocator extends BlockLocator {

  // TODO : use feature file
  public static Set<String> badWords = Sets.newHashSet();

  static {
    badWords.add("商品分类");
  }

	public MenuLocator(Document doc) {
		super(doc, BlockLabel.Menu);
	}

  @Override
	protected DomSegment quickLocate() {
    double maxScore = 3;
    DomSegment menu = null;

    for (DomSegment segment : doc.domSegments()) {
      double score = getScore(segment.block());

      // logger.debug("menu score : {} : {}", segment.body().prettyName(), score);

      if (score > maxScore) {
        maxScore = score;
        menu = segment;
      }
    }

    if (menu != null) {
      menu.tag(targetLabel, FuzzyProbability.MUST_BE);
    }

    return menu;
  }

	@Override
  protected DomSegment deepLocate() {
    MenuItemFounder founder = new MenuItemFounder();
    new ElementTraversor(founder).traverse(doc());

    DomSegment segment = null;
    if (!founder.getMenuItems().isEmpty()) {
      segment = new DomSegment(null, null, founder.getMenuItems().values().iterator().next());
      segment.tag(targetLabel, FuzzyProbability.MUST_BE);
    }

		return segment;
	}

  public static DomSegment createMenu(Document doc, Configuration conf) {
<<<<<<< HEAD
    Element ele = doc.body().prependElement("ul class='created'");
=======
    Element ele = doc.body().prependElement("ul");
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
    ele.append("<li><a href='/'>首页</a></li>");
    ele.sequence(doc.body().sequence() + 100); // TODO : use a machine learned sequence

    return DomSegment.create(ele, BlockLabel.Menu, FuzzyProbability.MUST_BE);
  }

  // TODO : use classifier system
	private double getScore(Element root) {
    if (root == null) return 0;

	  int score = 0;

	  double seq = root.indic(Indicator.SEQ);
	  double a = root.indic(Indicator.A);
	  double img = root.indic(Indicator.IMG);
	  double c = root.indic(Indicator.C);
    double tb = root.indic(Indicator.TB);
    double sep = root.indic(Indicator.SEP);
    double aah = root.indic(Indicator.AAH);
    double docD = doc.body().indic(Indicator.D);

    // it's bad when there is a "all categories" section
    if (seq > (2 * docD / 3) || a < 4 || a > 25 || img > 3 || c / tb > 5 || sep > 2) {
      score -= 5;
    }

    if (c / tb < 5) {
      ++score;
    }

    if (c / tb < 4) {
      ++score;
    }

    if (seq < 200) {
      ++score;
    }

    if (tb == a) {
      ++score;
    }

    String text = StringUtil.stripNonChar(root.text());
    if (text.contains("首页")) {
      score += 5;
    }

    if (root.attr("class").matches("nav|menu")) {
      ++score;
    }

    if (root.attr("id").matches("nav|menu")) {
      ++score;
    }

    // vision feature
    if (aah > 0 && aah < 22) {
      score -= 3;
    }

    if (aah > 30) {
      score += 3;
    }

    return score;
	}

  /*
   * 通过一个菜单项，找到整个菜单的根节点
   * */
  private Element getMenuFromItem(final Element menuItem) {
    // 至少6层深度,一个典型的路径是：
    // html > body > div[class="main"] > div[class="nav"] > ul > li > a
    if (menuItem == null || menuItem.depth() < 6) {
      return null;
    }

    Element menuElement = null;
    Element ele = menuItem;

    int maxTry = 4;
    while(menuElement == null && ele != null && maxTry-- > 0) {
      if (!StringUtil.in(ele.tagName(), "ul", "ol", "p", "div")) {
        continue;
      }

      if (ele.tagName().equals("ul")) {
        menuElement = ele;
      }

      if (menuElement == null && ele.indic(Indicator.C) >= 4 && ele.indic(Indicator.D) >= 8) {
        // at least 4 menu item
        menuElement = ele;
      }

      ele = ele.parent();
    }

    return menuElement;
  }

  private class MenuItemFounder extends InterruptiveElementVisitor {

    private Multimap<Double, Element> menuItems = TreeMultimap.create(Collections.reverseOrder(), ComparatorUtils.NATURAL_COMPARATOR);

    public Multimap<Double, Element> getMenuItems() {
      return menuItems;
    }

    @Override
    public void head(Element ele, int depth) {
//      if (!ele.tagName().equals("a")) {
//        return;
//      }

      // logger.debug("find menu item : {}, {}", ele.prettyName(), ele.text());

      double score = 0.0;
      String text = StringUtil.stripNonChar(ele.text());
      if (text.equals("首页")) {
        Element menu = getMenuFromItem(ele);
        score = getScore(ele);
        menuItems.put(score, ele);
      }
      else {
        String href = StringUtils.stripEnd(ele.attr("href"), "/");
        int count = StringUtils.countMatches(href, "/");
        // only http:// or https:// or no "/"
        if (count <= 2) {
          Element menu = getMenuFromItem(ele);
          score = getScore(ele);
          menuItems.put(score, ele);
        }
      }

      if (score >= 10) {
        stop();
        return;
      }
    }
  }
}
