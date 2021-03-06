package org.qiwur.scent.data.extractor;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.entity.PageEntity;
import org.jsoup.block.BlockLabel;
import org.jsoup.block.DomSegment;

public class TitleExtractor extends DomSegmentExtractor {

  public TitleExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
    super(segment, pageEntity, BlockLabel.Title.text());
  }

	@Override
	public void process() {
    if (!valid()) return;

    pageEntity().put(displayLabel(), getPrettyText(segment().text()), BlockLabel.Title.text());
	}
}
