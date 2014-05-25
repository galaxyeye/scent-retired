package org.qiwur.scent.data.extractor;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.utils.StringUtil;

public class TitleExtractor extends DomSegmentExtractor {

	public static final String KeepCharacterRegex = "[:：/\\\\]";

  public TitleExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
    super(segment, pageEntity, BlockLabel.Title);
  }

	@Override
	public void process() {
    if (!valid()) return;

    pageEntity.put(sectionLabel, getPrettyText(segment.text()), sectionLabel);
	}

	@Override
	public String getPrettyText(String name) {
		return StringUtil.trimNonChar(name).replaceAll(KeepCharacterRegex, "");
	}
}
