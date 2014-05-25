package org.qiwur.scent.block.locator;

import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.utils.NetUtil;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;

public final class MenuLocator extends BlockLocator {

	public MenuLocator(Document doc) {
		super(doc, BlockLabel.Menu);
	}

  @Override
	protected DomSegment quickLocate() {
    double maxScore = 0;
    DomSegment menu = null;

    for (DomSegment segment : doc.domSegments()) {
      double score = getScore(segment.body());
      if (score > maxScore) {
        maxScore = score;
        menu = segment;
      }
    }

    if (menu != null) {
      menu.tag(targetLabel, FuzzyProbability.MUST_BE);
    }

    return null;
  }

	@Override
  protected DomSegment deepLocate() {
		DomSegment segment = null;

		Elements candidates = findNavElementCandidates();

		if (candidates.size() > 0) {
			Element navElement = getMaxScoreNavElement(candidates);

			if (navElement != null) {
				Element mainNavElement = getMenuElement(navElement);

				if (mainNavElement != null) {
					segment = new DomSegment(mainNavElement);
					segment.tag(targetLabel, FuzzyProbability.MUST_BE);
				}
			}
		}

		return segment;
	}

	/*
	 * 通过一个菜单项，找到整个菜单的根节点
	 * 向上3层内，找到直接孩子数最多的节点，就是包含整个菜单的节点
	 * */
	private Element getMenuElement(final Element navElement) {
		// 至少6层深度,一个典型的路径是：
		// html > body > div[class="main"] > div[class="nav"] > ul > li > a
		if (navElement == null || navElement.depth() < 6) {
			return null;
		}

		int maxTry = 3;
		Element p = navElement;
		Element mainNavElement = navElement;
		// p的兄弟数
		int maxSiblingNum = p.siblingSize();
		while (p != null && maxTry-- > 0) {
			// System.out.println(p.tagName() + " " + p.siblingSize() + " " + p.attr("class") + " " + p.text());

			// p的父节点的兄弟数
			int siblingNum = p.parent().siblingSize();

			if (maxSiblingNum < siblingNum) {
				mainNavElement = p.parent().parent();
				maxSiblingNum = siblingNum;
			}

			p = p.parent();
		}

		return mainNavElement;
	}

	private int getScore(Element root) {
	  int score = 0;

	  double seq = root.indic(Indicator.SEQ);
	  double a = root.indic(Indicator.A);
	  double img = root.indic(Indicator.IMG);
	  double c = root.indic(Indicator.C);
    double tb = root.indic(Indicator.TB);
    double sep = root.indic(Indicator.SEP);

    double docD = doc.body().indic(Indicator.D);

    if (seq > docD / 2 || a < 4 || a > 25 || img > 0 || c / tb > 5 || sep > 1) {
      return 0;
    }

    if (c / tb < 5) {
      ++score;
    }

    if (tb == a) {
      ++score;
    }

    String text = StringUtil.stripNonChar(root.text());
    if (text.contains("首页")) {
      score += 10;
    }

    if (root.attr("class").matches("nav|menu")) {
      ++score;
    }

    if (root.attr("id").matches("nav|menu")) {
      ++score;
    }

    return score;
	}

	private Element getMaxScoreNavElement(Elements candidates) {
		Element navElement = null;
		int lastScore = 0;

		for (Element candidate : candidates) {
			int score = 0;

			if (candidate.depth() < 3 || candidate.tagName() != "a") {
				continue;
			}

			// 计算分值，分值越大，是菜单的可能性越大
			++score;

			// 计算该节点向上5层内的统计信息
			int maxTry = 5;
			Element p = candidate.parent();
			while (p != null && maxTry-- > 0) {
				double numChildren = p.indic(Indicator.C);
				score += numChildren;

				// 如：ul下有多个li，每个li下面一个a
				if (numChildren == p.indic(Indicator.G)) {
					score += numChildren;
				}

				if (p.tagName().equals("ul")) {
					score += numChildren;
				}

				if (p.attr("class").matches("nav|menu")) {
					score += numChildren;
				}

				if (p.attr("id").matches("nav|menu")) {
					score += numChildren;
				}

				p = p.parent();
			}

			if (score > lastScore) {
				lastScore = score;
				navElement = candidate;
			}
		}

		return navElement;
	}

	/*
	 * 多种查找规则，越往下，效率越差
	 * */
	private Elements findNavElementCandidates() {
		// TODO : 查找算法可以优化
		Elements candidates = doc().getElementsContainingStrippedOwnText("首页");
		if (candidates.size() > 0) return candidates;

		// TODO : 手动更正
		candidates = doc().getElementsContainingStrippedOwnText("天猫");
		if (candidates.size() > 0) return candidates;

		// 链接指向网站首页
		String domain = NetUtil.getDomain(doc().baseUri());
		if (domain != null && candidates.size() > 0) {
			candidates = doc().getElementsByAttributeValueContaining("href", domain);

			// 包含本网站域名的链接太多了，没有意义
			if (candidates.size() > 5) {
				candidates = null;
			}
		}
		if (candidates.size() > 0) return candidates;

		// 链接指向网站首页
		candidates = doc().getElementsByAttributeValue("href", "/");
		if (candidates.size() > 0) return candidates;

		// css信息
		candidates = doc().getElementsByAttributeValueMatching("id", "nav|menu");
		if (candidates.size() > 0) return candidates;
		candidates = doc().getElementsByAttributeValueMatching("css", "nav|menu");
		if (candidates.size() > 0) return candidates;

		return candidates;
	}
}
