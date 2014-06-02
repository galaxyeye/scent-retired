package org.qiwur.scent.data.extractor;

import java.util.Collection;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.storage.WebPage.Field;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multimap;

public class DomSegmentExtractor extends KeyValueExtractor implements DataExtractor {

  protected Configuration conf;
  protected final DomSegment segment;
  protected final PageEntity pageEntity;
  protected final String sectionLabel;

  public DomSegmentExtractor(DomSegment segment, PageEntity pageEntity) {
    this(segment, pageEntity, segment.primaryLabel());
  }

  public DomSegmentExtractor(DomSegment segment, PageEntity pageEntity, BlockLabel sectionLabel) {
    this(segment, pageEntity, sectionLabel == null ? null : sectionLabel.text());
  }

  public DomSegmentExtractor(DomSegment segment, PageEntity pageEntity, String sectionLabel) {
    super(segment == null ? null : segment.body());

    Validate.notNull(segment);
    Validate.notNull(pageEntity);

    this.segment = segment;
    this.pageEntity = pageEntity;
    this.sectionLabel = sectionLabel;
  }

  public DomSegment segment() {
    return segment;
  }

  @Override
  public void process() {
    super.process();

    Multimap<String, String> attrs = getAttributes();
    for (Entry<String, String> entry : attrs.entries()) {
      pageEntity.put(entry.getKey(), entry.getValue(), sectionLabel);
    }

    if (attrs.isEmpty() && StringUtils.isNotEmpty(sectionLabel)) {
      pageEntity.put(sectionLabel, getExtractedValue(segment, sectionLabel), segment.labelTracker().getLabels());
    }
  }

  @Override
  public boolean valid() {
    return segment != null && super.valid();
  }

  public BlockLabel primaryLabel() {
    if (segment != null)
      return segment.primaryLabel();
    return BlockLabel.UnknownBlock;
  }

  public final PageEntity pageEntity() {
    return pageEntity;
  }

  public final String sectionLabel() {
    return sectionLabel;
  }

  public String getPrettyText(String name) {
    return name.trim();
  }

  protected String getExtractedValue(String text) {
    return getExtractedValue(text, sectionLabel);
  }

  protected String getExtractedValue(String text, String label) {
    String content = String.format("<div class='%s' data-extractor='%s'>%s</div>",
        StringUtil.csslize(label),
        StringUtil.csslize(getClass().getSimpleName()),
        getPrettyText(text)
    );
    return content;
  }

  protected String getExtractedValue(DomSegment segment) {
    return getExtractedValue(segment, sectionLabel);
  }

  protected String getExtractedValue(DomSegment segment, String label) {
    Validate.notNull(segment);
    return getExtractedValue(segment.body().outerHtml(), label);
  }

	@Override
	public Collection<Field> getFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

}
