package org.qiwur.scent.jsoup.parser;

import org.qiwur.scent.jsoup.nodes.Attribute;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Node;
import org.qiwur.scent.jsoup.select.InterruptiveNodeVisitor;

public class NodeFilter extends InterruptiveNodeVisitor {

  @Override
  public void head(Node node, int depth) {
  }

  @Override
  public void tail(Node node, int depth) {
    if (node == null) return;

    if (node instanceof Element) {
      Element ele = (Element)node;

      for (Attribute attr : ele.attributes()) {
        if (attr.getKey().startsWith("on")) {
          attr.setValue("");
        }
      }
    }
  }
}
