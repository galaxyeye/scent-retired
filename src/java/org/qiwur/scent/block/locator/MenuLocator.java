package org.qiwur.scent.block.locator;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.select.ElementTraversor;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.jsoup.select.InterruptiveElementVisitor;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;

public final class MenuLocator extends BlockLocator {

	public MenuLocator(Document doc) {
		super(doc, BlockLabel.Menu);
	}

  @Override
	protected DomSegment quickLocate() {
    double maxScore = 3;
    DomSegment menu = null;

    for (DomSegment segment : doc.domSegments()) {
      double score = getScore(segment.body());

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
		Elements menuCandidates = findMenuCandidates();

    double maxScore = 3;
    Element menu = null;

    for (Element candidate : menuCandidates) {
      double score = getScore(candidate);

      // logger.debug("menu score : {} : {}", item.prettyName(), score);

      if (score > maxScore) {
        maxScore = score;
        menu = candidate;
      }
    }

    DomSegment segment = null;
    if (menu != null) {
      segment = new DomSegment(null, null, menu);
      segment.tag(targetLabel, FuzzyProbability.MUST_BE);
    }

		return segment;
	}

  public static DomSegment createMenu(Document doc, Configuration conf) {
    Element ele = doc.body().prependElement("ul");
    ele.append("<li><a href='/'>首页</a></li>");
    ele.sequence(doc.body().sequence() + 100); // TODO : use a machine learned sequence

    return DomSegment.create(ele, BlockLabel.Menu, FuzzyProbability.MUST_BE);
  }

	private int getScore(Element root) {
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
	 * find out all menu candidates in the document
	 * */
	private Elements findMenuCandidates() {
	  MenuItemFounder founder = new MenuItemFounder();
	  new ElementTraversor(founder).traverse(doc());

	  // logger.debug("menu items : {}", founder.getMenuItems());

	  Elements candidates = new Elements();
	  for (Element item : founder.getMenuItems()) {
	    Element menu = getMenuFromItem(item);
	    if (menu != null) candidates.add(menu);
	  }

	  return candidates;
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

    private Elements menuItems = new Elements();

    public Elements getMenuItems() {
      return menuItems;
    }

    @Override
    public void head(Element ele, int depth) {
      if (!ele.tagName().equals("a")) {
        return;
      }

      // logger.debug("find menu item : {}, {}", ele.prettyName(), ele.text());

      String text = StringUtil.stripNonChar(ele.text());
      if (text.equals("首页")) {
        menuItems.add(ele);
        return;
      }

      String href = StringUtils.stripEnd(ele.attr("href"), "/");
      int count = StringUtils.countMatches(href, "/");
      // only http:// or https:// or no "/"
      if (count <= 2) {
        menuItems.add(ele);
      }
    }
  }
}
