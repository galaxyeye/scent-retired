package org.qiwur.scent.jsoup.select;

import org.qiwur.scent.jsoup.nodes.Element;

/**
 * Depth-first e traversor. Use to iterate through all es under and including the specified root e.
 * <p/>
 * This implementation does not use recursion, so a deep DOM does not risk blowing the stack.
 */
public class ElementTraversor {
    private ElementVisitor visitor;

    /**
     * Create a new traversor.
     * @param visitor a class implementing the {@link ElementVisitor} interface, to be called when visiting each e.
     */
    public ElementTraversor(ElementVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Start a depth-first traverse of the root and all of its descendants.
     * @param root the root e point to traverse.
     */
    public void traverse(Element root) {
        Element e = root;
        int depth = 0;

        while (e != null && !visitor.stopped()) {
            visitor.head(e, depth);

            if (visitor.stopped()) break;

            if (e.children().size() > 0) {
                e = e.child(0);
                depth++;
            } else {
                while (e.nextElementSibling() == null && depth > 0 && !visitor.stopped()) {
                    visitor.tail(e, depth);
                    e = e.parent();
                    depth--;
                }

                visitor.tail(e, depth);

                if (e == root)
                    break;
                e = e.nextElementSibling();
            }
        }
    }
}
