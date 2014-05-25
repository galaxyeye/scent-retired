package org.qiwur.scent.jsoup.select;

import org.qiwur.scent.jsoup.nodes.Element;

public abstract class InterruptiveElementVisitor implements ElementVisitor {

	protected boolean stopped = false;

	@Override
	public abstract void head(Element node, int depth);

	@Override
	public void tail(Element node, int depth) {
	}

	@Override
	public boolean stopped() {
		return stopped;
	}
}
