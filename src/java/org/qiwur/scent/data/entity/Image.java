package org.qiwur.scent.data.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

public class Image {

  // TODO : configurable
  public static final String[] ImageSuffixes = { "jpg", "jpeg", "png", "gif", "webp" };

  private Map<String, String> attributes = new TreeMap<String, String>();

  public void putAttribute(String name, String value) {
    Validate.notEmpty(name);
    Validate.notEmpty(value);

    attributes.put(name, value);
  }

  public String attr(String name) {
    return attributes.get(name);
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public static Image create(Element ele) {
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

      if (name.startsWith("data-") && value.equals("0")) {
        continue;
      }

      if (Link.maybeUrl(name, value)) {
        // TODO : tricky
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

  @Override
  public String toString() {
    StringBuilder img = new StringBuilder();
    img.append("<img ");

    for (Entry<String, String> attr : attributes.entrySet()) {
      String name = attr.getKey();

      img.append(name);
      img.append("='");
      img.append(attr.getValue());
      img.append("\' ");
    }
    img.append("/>");

    return img.toString();
  }
}
