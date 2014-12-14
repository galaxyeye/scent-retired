package org.qiwur.scent.data.extractor;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.storage.WebPage.Field;
import org.qiwur.scent.data.entity.PageEntity;
import org.jsoup.block.BlockLabel;
import org.jsoup.block.BlockPattern;
import org.jsoup.block.DomSegment;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class DomSegmentExtractor extends KeyValueExtractor implements DataExtractor {

  public static final String DefaultLabel = BlockLabel.UnknownBlock.text();

  private Configuration conf;
  private final DomSegment segment;
  private final PageEntity pageEntity;
  private final String displayLabel;

  static Map<BlockPattern, Class<? extends DomSegmentExtractor>> patternExtractors = Maps.newHashMap();

  static {
    patternExtractors.put(BlockPattern.Links, LinksExtractor.class);
    patternExtractors.put(BlockPattern.DenseLinks, LinksExtractor.class);
    patternExtractors.put(BlockPattern.Images, ImagesExtractor.class);
    patternExtractors.put(BlockPattern.LinkImages, LinkImagesExtractor.class);
  }

  public DomSegmentExtractor(DomSegment segment, PageEntity pageEntity, String displayLabel) {
    super(segment.block());

    Validate.notNull(segment);
    Validate.notNull(pageEntity);

    this.segment = segment;
    this.pageEntity = pageEntity;
    this.displayLabel = StringUtils.isEmpty(displayLabel) ? DefaultLabel : displayLabel;
  }

  public DomSegment segment() {
    return segment;
  }

  /*
   * Default extract behavior, firstly try to extract into key/value pairs, 
   * if failed, use the section label to be the key and the all segment's html to be the value
   * */
  @Override
  public void process() {
    if (segment.veryLikely(BlockPattern.II)) {
      logger.debug("extract II pattern for {}", segment.name());
      extractIIPattern(element(), I_I_PATTERN_SEPERATORS);
    }

    if (segment.veryLikely(BlockPattern.N2)) {
      logger.debug("extract N2 pattern for {}", segment.name());
      extractN2Pattern(element(), 4, "div", "p", "ol", "ul");
    }

    if (segment.veryLikely(BlockPattern.Table)) {
      logger.debug("extract Table for {}", segment.name());
      extractTable(element());
    }

    if (segment.veryLikely(BlockPattern.Dl)) {
      logger.debug("extract Dl for {}", segment.name());
      extractDl(element());
    }

    Multimap<String, String> attrs = getAttributes();
    // logger.debug("there are {} attributes", attrs.size());

    for (Entry<String, String> entry : attrs.entries()) {
      pageEntity.put(entry.getKey(), entry.getValue(), displayLabel);
    }

    if (attrs.isEmpty()) {
      pageEntity.put(displayLabel, getExtractedValue(segment, displayLabel), segment.labels());
    }
  }

  public boolean valid() {
    return segment != null;
  }

  public String displayLabel() {
    return displayLabel;
  }

  public PageEntity pageEntity() {
    return pageEntity;
  }

  public String getPrettyText(String name) {
    return name.trim();
  }

  protected String getExtractedValue(String text) {
    return getExtractedValue(text, displayLabel);
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
    return getExtractedValue(segment, displayLabel);
  }

  protected String getExtractedValue(DomSegment segment, String label) {
    Validate.notNull(segment);
    return getExtractedValue(segment.block().outerHtml(), label);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + displayLabel + "][" + segment.name() + "]";
  }

	@Override
	public Collection<Field> getFields() {
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
