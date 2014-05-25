package org.qiwur.scent.entity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

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

  @Override
  public String toString() {
    StringBuilder img = new StringBuilder();
    img.append("<img ");

    for (Entry<String, String> attr : attributes.entrySet()) {
      img.append(attr.getKey());
      img.append("='");
      img.append(attr.getValue().replaceAll("\"", "'"));
      img.append("\' ");
    }
    img.append("/>");

    return img.toString();
  }
}
