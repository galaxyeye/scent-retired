package org.qiwur.scent.data.extractor;

import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.entity.Image;
import org.qiwur.scent.data.entity.PageEntity;
import org.jsoup.block.BlockPattern;
import org.jsoup.block.DomSegment;
import org.jsoup.nodes.Element;
import org.qiwur.scent.utils.StringUtil;

public final class ImagesExtractor extends DomSegmentExtractor {

	private Set<Image> images = new HashSet<Image>();

	public ImagesExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
    super(segment, pageEntity, BlockPattern.Images.text());
	}

	@Override
	public void process() {
    if (!valid()) return;

    for (Element ele : element().getElementsByTag("img")) {
      images.add(Image.create(ele));
		}

    if (!images.isEmpty()) {
      pageEntity().put(displayLabel(), formatImages(), segment().labels());
    }
	}

  protected String formatImages() {
    StringBuilder gallery = new StringBuilder();

    String cls = StringUtil.csslize(segment().labelTracker().getLabelsAsString());
    String clazz = StringUtil.csslize(getClass().getSimpleName());

    gallery.append(String.format("<div class='%s' data-extractor='%s'>", cls, clazz));
    for (Image image : images) {
      gallery.append(image.toString());
    }
    gallery.append("</div>");

    return gallery.toString();
  }
}
