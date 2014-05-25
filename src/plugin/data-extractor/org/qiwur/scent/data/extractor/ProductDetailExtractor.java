package org.qiwur.scent.data.extractor;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.block.DomSegment;

public class ProductDetailExtractor extends DomSegmentExtractor {

	public ProductDetailExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
		super(segment, pageEntity, "ProductDetail");
	}

	@Override
	public void process() {
    if (!valid()) return;

    pageEntity.put(sectionLabel, getPrettyText(segment.text()), sectionLabel);
	}

	@Override
	public String getPrettyText(String text) {
	  // TODO : html2wiki
		return text;
	}
}
