package org.qiwur.scent.data.extractor;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.entity.PageEntity;
import org.qiwur.scent.data.feature.CategoryFeature;
import org.qiwur.scent.data.feature.FeatureManager;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.learning.EntityCategoryLearner;

public class CategoriesExtractor extends DomSegmentExtractor {

  private final Configuration conf;
  private final CategoryFeature feature;
  private final EntityCategoryLearner categoryLearner;

	public CategoriesExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
		super(segment, pageEntity, BlockLabel.Categories.text());
		this.conf = conf;

    String featureFile = conf.get("scent.bad.category.words.file");
    feature = FeatureManager.get(conf, CategoryFeature.class, featureFile);

		this.categoryLearner = new EntityCategoryLearner(conf);
	}

	@Override
	public void process() {
    if (!valid()) return;

		Element root = adjustCategoryNavRoot(element());
		if (root == null) return;

		ArrayList<String> words = new ArrayList<String>();
		for (Element e : root.getAllElements()) {
			String text = feature.strip(e.ownText());

			if (!text.isEmpty()) {
				words.add(text);
			}
		}

		if (words.isEmpty()) return;

		String categoryText = StringUtils.join(words.toArray(), " > ");
    categoryText = getPrettyText(categoryText);

    pageEntity().put(BlockLabel.Categories.text(), categoryText, BlockLabel.Categories.text());
    categoryLearner.learn(categoryText);
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
