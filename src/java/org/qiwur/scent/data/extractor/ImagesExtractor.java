package org.qiwur.scent.data.extractor;

import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.Image;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.utils.StringUtil;

public final class ImagesExtractor extends DomSegmentExtractor {

	private Set<Image> images = new HashSet<Image>();

	public ImagesExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
    super(segment, pageEntity, BlockLabel.LinkImages);
	}

	@Override
	public void process() {
    if (!valid()) return;

    for (Element ele : element().getElementsByTag("img")) {
      images.add(Image.create(ele));
		}

    if (!images.isEmpty()) {
      pageEntity.put(sectionLabel, formatImages(), segment.labels());
    }
	}

  protected String formatImages() {
    StringBuilder gallery = new StringBuilder();

    String cls = StringUtil.csslize(segment.labelTracker().getLabelsAsString());
    String clazz = StringUtil.csslize(getClass().getSimpleName());

    gallery.append(String.format("<div class='%s' data-extractor='%s'>", cls, clazz));
    for (Image image : images) {
      gallery.append(image.toString());
    }
    gallery.append("</div>");

    return gallery.toString();
  }

  @Override
  public String toString() {
    return formatImages();
  }
}
