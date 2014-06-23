package org.qiwur.scent.entity.extractor;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.extractor.DomSegmentExtractor;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.block.DomSegment;

public class ProductShowExtractor extends DomSegmentExtractor {

	public ProductShowExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
		super(segment, pageEntity, "ProductShow");
	}

	@Override
	public void process() {
    if (!valid()) return;

    pageEntity().put("ProductShow", getPrettyText(segment().text()), "ProductShow");
	}

	@Override
	public String getPrettyText(String text) {
	  // TODO : html2wiki
		return text;
	}
}
