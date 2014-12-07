package org.qiwur.scent.data.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.qiwur.scent.jsoup.nodes.Attribute;
import org.qiwur.scent.jsoup.nodes.Element;

public class Link {

  private Map<String, String> attributes = new TreeMap<String, String>();
  private String text = "";
  private Image image = null;

  public void setText(String text) {
    if (text != null) this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public Image getImage() {
    return image;
  }

  public void putAttribute(String name, String value) {
    Validate.notEmpty(name);
    Validate.notEmpty(value);

    attributes.put(name, value);
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public static Link create(Element ele) {
    if (ele.tagName() != "a")
      return null;

    Link link = new Link();

    Element image = ele.getElementsByTag("img").first();
    if (image != null) {
      link.setImage(Image.create(image));
    }

    link.setText(sniffLinkText(ele, image));

    final List<String> ignoredAttrs = Arrays.asList("id", "class", "style", "_target", "target", "title");

    for (Attribute attr : ele.attributes()) {
      String name = attr.getKey();
      String value = attr.getValue();

      if (ignoredAttrs.contains(name)) {
        continue;
      }

      if (name.startsWith("data-") && value.equals("0")) {
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

  public static String sniffLinkText(Element link, Element image) {
    String text = StringUtils.trimToNull(link.text());
    if (text == null) text = StringUtils.trimToNull(link.attr("title"));
    if (text == null && image != null) text = StringUtils.trimToNull(image.attr("alt"));

    return text;
  }

  public static boolean maybeUrl(String attrName, String attrValue) {
    final List<String> urlAttrs = Arrays.asList("src", "url", "data-url");

    if (urlAttrs.contains(attrName)) return true;
    if (attrValue.contains("http://")) return true;
    if (StringUtils.countMatches(attrValue, "/") > 3) return true;

    return false;
  }

  public static boolean pseudoLink(String href) {
    return href.startsWith("#") && !href.startsWith("java") && !href.startsWith("void"); 
  }

  @Override
  public String toString() {
    StringBuilder link = new StringBuilder();
    if (image != null) link.append(image.toString());

    link.append("<a ");
    for (Entry<String, String> attr : attributes.entrySet()) {
      link.append(attr.getKey());
      link.append("=\"");
      link.append(attr.getValue());
      link.append("\" ");
    }
    link.append(">");

    link.append(text);

    link.append("</a>");

    return link.toString();
  }
}
