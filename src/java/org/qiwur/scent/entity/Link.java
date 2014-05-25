package org.qiwur.scent.entity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

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

  @Override
  public String toString() {
    String cls = "class='link";
    if (text.length() > 10) cls += " text";
    cls += "' ";

    StringBuilder link = new StringBuilder();
    if (image != null) link.append(image.toString());

    link.append("<a " + cls);
    for (Entry<String, String> attr : attributes.entrySet()) {
      link.append(attr.getKey());
      link.append("=\"");
      link.append(attr.getValue().replaceAll("\"", "'"));
      link.append("\" ");
    }
    link.append(">");

    link.append(text);

    link.append("</a>");

    return link.toString();
  }
}
