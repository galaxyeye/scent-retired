package org.qiwur.scent.data.extractor;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.feature.CategoriesFeature;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.learning.EntityCategoryLearner;
import org.qiwur.scent.utils.StringUtil;

public class CategoriesExtractor extends DomSegmentExtractor {

  private final Configuration conf;
  private final EntityCategoryLearner categoryLearner;

	public CategoriesExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
		super(segment, pageEntity, "Categories");

		this.conf = conf;
		this.categoryLearner = new EntityCategoryLearner(conf);
	}

	@Override
	public void process() {
    if (!valid()) return;

		Element root = adjustCategoryNavRoot(element());
		if (root == null) return;

		ArrayList<String> words = new ArrayList<String>();
		for (Element e : root.getAllElements()) {
			// 太长，可能是产品标题本身
			String text = StringUtil.stripNonChar(e.ownText()).trim();
			// TODO : product model may greater than 8
			if (text.length() > 8) {
				continue;
			}

			text = CategoriesFeature.preprocess(e.ownText());
			if (text != "") {
				words.add(text);
			}
		}

		String categoryText = StringUtils.join(words.toArray(), " > ");

		if (categoryText != null) {
		  categoryText = getPrettyText(categoryText);
      pageEntity.put(sectionLabel, categoryText, sectionLabel);

      categoryLearner.learn(categoryText);
		}
	}

	private Element adjustCategoryNavRoot(Element root) {
		Element newRoot = root.parent();

		double delta = newRoot.indic(Indicator.TB) - root.indic(Indicator.TB);
		double delta2 = newRoot.indic(Indicator.C) - root.indic(Indicator.C);
		double delta3 = newRoot.indic(Indicator.D) - root.indic(Indicator.D);
		if (delta <= 2 && delta2 <= 6 && delta3 <= 4) {
			return newRoot;
		}

		return root;
	}
}
