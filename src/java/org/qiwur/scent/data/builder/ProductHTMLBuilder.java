package org.qiwur.scent.data.builder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Attribute;
import org.qiwur.scent.jsoup.nodes.Attributes;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.utils.StringUtil;

public class ProductHTMLBuilder extends ProductBuilder {

  public static final String[] labels = {
      "Title",
      "Categories",
      "Metadata",
      "Gallery",
      "ProductShow",
      "ProductSpec",
      "Images",
      "SimilarEntity",
      "Links",
  };

  private Document doc;

  public ProductHTMLBuilder(PageEntity pageEntity, Configuration conf) {
    super(pageEntity, conf);

    try {
      doc = Jsoup.parse(new File("conf/product.template.html"), "utf-8");
    } catch (IOException e) {
      logger.error(e);
    }
  }

  public void process() {
    Validate.notNull(doc);

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
      else if (StringUtil.in(label, "ProductShow", "ProductSpec", "Metadata")) {
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
        div.appendElement("div").html(attributes.iterator().next().value());
      }
      else {
        
      }

      body.appendElement("hr");
    }

    setAllImageSize(doc);
    adjustAttributes(doc);
  }

  public Document doc() {
    return this.doc;
  }

  private Element buildGallery(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      root.append(attr.value());
    }

    for (Element img : root.getElementsByTag("img")) {
      img.removeAttr("alt");
    }

    return root;
  }

  private Element buildImages(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      if (attr.hasCategory("ProductDetail")) {
        root.append(attr.value());
      }
    }

    for (Element img : root.getElementsByTag("img")) {
      img.removeAttr("alt");
    }

    return root;
  }

  private Element buildLinks(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      root.append(attr.value());
    }

    return root;
  }

  private Element buildTable(Element root, Collection<EntityAttribute> attributes) {
    Element table = root.appendElement("table");
    for (EntityAttribute attr : attributes) {
      if (attr.value().length() > 300) continue;

      Element tr = table.appendElement("tr");
      Element th = tr.appendElement("td");
      Element td = tr.appendElement("td");

      th.text(attr.name());
      td.text(attr.value());
    }

    return table;
  }

  private void setAllImageSize(Element root) {
    for (Element img : root.select("img")) {
      int width = getImgWidth(img);

      // if it's greater than 200, just show it as it's original width
      if (width > 200) {
        img.attr("width", String.valueOf(width));
      }

      // probably banner ad
      if (width > 500) {
        img.remove();
      }
    }
  }

  private void adjustAttributes(Element root) {
    for (Element ele : root.getAllElements()) {
      Attributes validAttrs = new Attributes();

      if (ele.attributes() != null) {
        for (Attribute attr : ele.attributes()) {
          if (ArrayUtils.contains(PermittedAttributes, attr.getKey())) {
            validAttrs.put(attr);
          }
        }

        ele.clearAttrs();
        ele.attributes().addAll(validAttrs);
      }
      
      if (ele.tagName().equals("a")) {
        ele.attr("target", "_blank");
      }
    }
  }

  private int getImgWidth(Element image) {
    try {
      return Integer.parseInt(image.attr("data-offset-width"));
    }
    catch(Exception e) {
    }

    return -1;
  }
}
