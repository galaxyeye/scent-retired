package org.qiwur.scent.classifier.statistics;

import org.apache.commons.lang.ArrayUtils;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.utils.StringUtil;

public class StatIndicator {

  private final String name;
  private final String script;

  public StatIndicator(String name) {
    this(name, "");
  }

  public StatIndicator(String name, String script) {
    this.name = name;
    this.script = script;
  }

  public String name() {
    return name;
  }

  public String script() {
    return script;
  }

  public boolean isSimple() {
    return ArrayUtils.contains(Indicator.names, name);
  }

  @Override
  public String toString() {
    String s = name;
    if (script != null) {
      s += ", ";
      s += StringUtil.stripNonChar(script);
    }
    return s;
  }
}
