package org.qiwur.scent.entity.extractor;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.entity.PageEntity;
import org.qiwur.scent.data.extractor.DomSegmentExtractor;
import org.jsoup.block.DomSegment;

public final class RelativeProductExtractor extends DomSegmentExtractor {

  public RelativeProductExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
    super(segment, pageEntity, "RelativeProduct");
  }

}
