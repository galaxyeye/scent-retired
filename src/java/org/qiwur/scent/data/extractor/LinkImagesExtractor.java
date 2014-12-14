package org.qiwur.scent.data.extractor;

import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.entity.Link;
import org.qiwur.scent.data.entity.PageEntity;
import org.jsoup.block.BlockPattern;
import org.jsoup.block.DomSegment;
import org.jsoup.nodes.Element;
import org.qiwur.scent.utils.StringUtil;

public final class LinkImagesExtractor extends DomSegmentExtractor {

  private Set<Link> links = new HashSet<Link>();

	public LinkImagesExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
    super(segment, pageEntity, BlockPattern.LinkImages.text());
	}

	@Override
	public void process() {
    if (!valid()) return;

    for (Element ele : element().getElementsByTag("a")) {
      if (!Link.pseudoLink(ele.attr("href"))) {
        links.add(Link.create(ele));
      }
    }

    if (!links.isEmpty()) {
      pageEntity().put(displayLabel(), formatLinks(), segment().labels());
    }
	}

  protected String formatLinks() {
    StringBuilder sb = new StringBuilder();

    String cls = StringUtil.csslize(segment().labelTracker().getLabelsAsString());
    String clazz = StringUtil.csslize(getClass().getSimpleName());

    sb.append(String.format("<div class='%s' data-extractor='%s'>", cls, clazz));
    for (Link link : links) {
      sb.append("<div>");
      sb.append(link.toString());
      sb.append("</div>");
    }
    sb.append("</div>");

    return sb.toString();
  }
}
