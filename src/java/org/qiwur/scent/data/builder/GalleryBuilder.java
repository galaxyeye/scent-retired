package org.qiwur.scent.data.builder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.utils.StringUtil;

public class GalleryBuilder extends EntityBuilder {

  private static final String[] labels = {
      "Title",
      "Categories",
      "Metadata",
      "Gallery",
      "TitleContainer",
      "ProductSpec",
      "Images",
      "SimilarEntity",
      "Links",
  };

  private Document doc;

  public GalleryBuilder(PageEntity pageEntity, Configuration conf) throws IOException {
    super(pageEntity, conf);
    doc = Jsoup.parse(new File("wwwroot/template/page-entity-gallery-template.html"), "utf-8");
  }

  public void process() {
    Element body = doc.select("body").first();

    for (String label : labels) {
      String section = StringUtil.humanize(label);
      Set<EntityAttribute> attributes = pageEntity.getCategorized(label);
      if (attributes.isEmpty()) continue;

      Element div = body.appendElement("div");
      div.attr("class", section);

      if (!label.equals("Title")) {
        div.appendElement("h2").text(section);
      }

      if (label.equals("Title")) {
        String title = attributes.iterator().next().value();
        div.appendElement("h1").text(title);

        doc.title(title);
      }
      else if (StringUtil.in(label, "ProductSpec", "TitleContainer", "Metadata")) {
        buildTable(div, attributes);
      }
      else if (label.equals("Images")) {
        buildImages(div, attributes);
      }
      else if (label.equals("SimilarEntity")) {
        buildLinks(div, attributes);
      }
      else if (label.equals("Gallery")) {
        buildGallery(div, attributes);
      }
      else if (label.equals("Categories")) {
        div.appendElement("div").text(attributes.iterator().next().value());
      }
      else {
        
      }

      body.appendElement("hr");
    }

    for (Element img : body.select("img")) {
      if (getWidth(img) > 500) img.remove();
      img.removeAttr("width");
      img.removeAttr("height");
    }
  }

  public Document doc() {
    return this.doc;
  }

  private Element buildGallery(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      root.append(attr.value());
    }

    return root;
  }

  private Element buildImages(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      if (!attr.hasCategory("Links") && !attr.hasCategory("Gallery")) {
        root.append(attr.value());
      }
    }

    return root;
  }

  protected Element buildLinks(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      root.append(attr.value());
    }

    return root;
  }

  protected Element buildTable(Element root, Collection<EntityAttribute> attributes) {
    Element table = root.appendElement("table");
    for (EntityAttribute attr : attributes) {
      if (attr.value().length() > 50) {
        continue;
      }

      Element tr = table.appendElement("tr");
      Element th = tr.appendElement("td");
      Element td = tr.appendElement("td");

      th.text(attr.name());
      td.text(attr.value());
    }

    return table;
  }

  private int getWidth(Element ele) {
    try {
      return Integer.parseInt(ele.attr("width"));
    }
    catch(Exception e) {
    }

    return -1;
  }
}
