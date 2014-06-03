package org.qiwur.scent.entity.extractor;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.extractor.DomSegmentExtractor;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.block.DomSegment;

public final class RelativeProductExtractor extends DomSegmentExtractor {

  public RelativeProductExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
    super(segment, pageEntity, "RelativeProduct");
  }

}
