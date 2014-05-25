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

public final class GalleryExtractor extends DomSegmentExtractor {

	// TODO : add more suffixes

	private Set<Image> images = new HashSet<Image>();

	public GalleryExtractor(DomSegment segment, PageEntity pageEntity, Configuration conf) {
    super(segment, pageEntity, BlockLabel.Gallery);
	}

	@Override
	public void process() {
    if (!valid()) return;

    for (Element ele : element().getElementsByTag("img")) {
      images.add(extractImage(ele));
		}

    if (!images.isEmpty()) {
      pageEntity.put(sectionLabel, formatGallery(), sectionLabel);
    }
	}

  protected String formatGallery() {
    StringBuilder gallery = new StringBuilder();

    String cls = segment.labelTracker().getLabelsAsString();
    cls = StringUtil.csslize(cls);

    gallery.append("<div class='" + cls + "'>");
    for (Image image : images) {
      gallery.append(image.toString());
    }
    gallery.append("</div>");

    return gallery.toString();
  }

  @Override
  public String toString() {
    return formatGallery();
  }
}
