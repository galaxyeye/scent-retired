package org.qiwur.scent.data.builder;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.utils.StringUtil;

public class ProductHTMLBuilder extends ProductBuilder {

  public static final String[] displayLabels = {
      "Title",
      "Categories",
      "Metadata",
      "Gallery",
      "TitleContainer",
      "ProductSpec",
      "ProductDetail",
      "SimilarEntity",
      "Links",
  };

  public static final String[] formats = {"Simplified", "Waterfall", "All"};

  private String format = "Simplified";

  public ProductHTMLBuilder(PageEntity pageEntity, Configuration conf) {
    super(pageEntity, conf);
  }

  public void setFormat(String format) {
    if (ArrayUtils.contains(formats, format)) {
      this.format = format;
    }
  }

  public void process() {
    if (format.equalsIgnoreCase("Waterfall")) {
      buildWaterfall();
    }
    else if (format.equalsIgnoreCase("All")) {
      buildHtml();
    }
    else {
      buildSimplified();
    }
  }

  protected void buildSimplified() {
    Validate.notNull(doc);

    Element body = doc.body();

    for (String label : displayLabels) {
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
      else if (StringUtil.in(label, "TitleContainer", "ProductSpec", "Metadata")) {
        buildTable(div, attributes);
      }
      else if (label.equals("ProductDetail")) {
        buildProductDetail(div, attributes);
        buildPureImages(div, attributes);
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

  protected void buildWaterfall() {
    
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

  private Element buildProductDetail(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      root.append(attr.value());
    }

    return root;
  }
}
