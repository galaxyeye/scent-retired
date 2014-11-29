package org.qiwur.scent.utils;

import org.apache.commons.lang.StringUtils;

public class TestCss {

  static String[] parseStyle(String style) {
    return StringUtil.stripNonChar(style, ":;").split(";");
  }

  static String getStyle(String[] styles, String styleKey) {
    String styleValue = "";

    String search = styleKey + ":";
    for (String style : styles) {
      if (style.startsWith(search)) {
        return style.substring(search.length());
      }
    }

    return styleValue;
  }

  static String getStyle(String styles, String styleKey) {
    return getStyle(parseStyle(styles), styleKey);
  }

  static String sniffHeight(String style) {
    String height = "";
    String[] attrs = StringUtil.stripNonChar(style, ":;").split(";");

    for (String attr : attrs) {
      System.out.println(attr);

      if (attr.startsWith("height:")) {
        height = attr.substring("height:".length());
        height = StringUtils.removeEnd(height, "px");
      }
    }

    return height;
  }

  static double pixelatedValue(String value, double defaultValue) {
    // TODO : we currently handle only px
    final String[] units = {"in", "%", "cm", "mm", "ex", "pt", "pc", "px"};

    value = StringUtils.removeEnd(value, "px");

    return StringUtil.tryParseDouble(value, defaultValue);
  }

  public static void main(String[] args) throws Exception {
    String height = getStyle("width: 750px; height: 488px;", "height");

    System.out.println(pixelatedValue(height, 0.0));
  }
}
