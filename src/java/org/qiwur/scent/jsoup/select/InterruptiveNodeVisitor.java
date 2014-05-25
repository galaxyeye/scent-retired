package org.qiwur.scent.jsoup.select;

import org.qiwur.scent.jsoup.nodes.Node;

public abstract class InterruptiveNodeVisitor implements NodeVisitor {

  @Override
  public abstract void head(Node node, int depth);

  @Override
  public void tail(Node node, int depth) {
  }

  @Override
  public boolean stopped() {
    return false;
  }
}
