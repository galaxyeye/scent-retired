package org.qiwur.scent.data.extractor;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.qiwur.scent.entity.Image;
import org.qiwur.scent.entity.Link;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Attribute;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.select.Elements;

import com.google.common.collect.Multimap;

public class DomSegmentExtractor extends KeyValueExtractor {

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
      pageEntity.put(sectionLabel, segment.outerHtml(), sectionLabel);
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

  protected Image extractImage(Element ele) {
    if (ele == null || ele.tagName() != "img")
      return null;

    Image image = new Image();

    final List<String> ignoredAttrs = Arrays.asList("id", "class", "style");

    String lazySrc = null;
    for (Attribute attr : ele.attributes()) {
      String name = attr.getKey();
      String value = attr.getValue();

      if (ignoredAttrs.contains(name)) {
        continue;
      }

      if (maybeUrl(name, value)) {
        if (name.contains("lazy")) {
          lazySrc = ele.absUrl(name);
        }

        value = ele.absUrl(name);
      }

      if (!name.isEmpty() && !value.isEmpty()) {
        image.putAttribute(name, value);
      }
    }

    if (lazySrc != null) {
      image.putAttribute("src", lazySrc);
    }

    return image;
  }

  protected Link extractLink(Element ele) {
    if (ele.tagName() != "a")
      return null;

    Link link = new Link();

    Element image = ele.getElementsByTag("img").first();
    if (image != null) {
      link.setImage(extractImage(image));
    }

    // sniff link text
    String text = StringUtils.trimToNull(ele.text());
    if (text == null) text = StringUtils.trimToNull(ele.attr("title"));
    if (text == null && image != null) text = StringUtils.trimToNull(image.attr("alt"));
    link.setText(text);

    final List<String> ignoredAttrs = Arrays.asList("id", "class", "style", "_target", "target");

    for (Attribute attr : ele.attributes()) {
      String name = attr.getKey();
      String value = attr.getValue();

      if (ignoredAttrs.contains(name)) {
        continue;
      }

      if (maybeUrl(name, value)) {
        // TODO : better sniff strategy
        value = ele.absUrl(name);
      }

      if (!name.isEmpty() && !value.isEmpty()) {
        link.putAttribute(name, value);
      }
    }

    return link;
  }
  
  private boolean maybeUrl(String name, String value) {
    final List<String> urlAttrs = Arrays.asList("src", "url", "data-url");

    if (urlAttrs.contains(name))
      return true;
    if (value.contains("http://"))
      return true;
    if (StringUtils.countMatches(value, "/") > 3)
      return true;

    return false;
  }

}
