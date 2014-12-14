package org.qiwur.scent.entity.extractor;

import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.entity.PageEntity;
import org.qiwur.scent.data.extractor.DomSegmentExtractor;
import org.jsoup.block.BlockLabel;
import org.jsoup.block.DomSegment;
import org.jsoup.nodes.Element;

import com.google.common.collect.Multimap;

public class TitleContainerExtractor extends DomSegmentExtractor {

	public TitleContainerExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
		super(segment, pageEntity, BlockLabel.TitleContainer.text());
	}

	@Override
	public void process() {
    if (!valid()) return;

    for (Element ele : element().select("table,tbody")) {
      extractTable(ele);
    }

    for (Element ele : element().select("dl")) {
      extractDl(ele);
    }

    extractN2Pattern(element(), 4, "div", "p", "ol", "ul");

    Multimap<String, String> attrs = getAttributes();

    extractByPatterns(element().text(), attrs);

    for (Entry<String, String> entry : attrs.entries()) {
      pageEntity().put(entry.getKey(), getPrettyText(entry.getValue()), displayLabel());
    }
	}

	protected void extractByPatterns(String text, Multimap<String, String> attrs) {
	  
	}

	@Override
	public String getPrettyText(String text) {
		return text;
	}
}
