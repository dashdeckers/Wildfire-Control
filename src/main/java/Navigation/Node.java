package Navigation;

import Model.Elements.Element;

public class Node {
    public Element element;
    public int d;

    // A parameterized student constructor
    public Node(Element e, int dist) {

        this.element = e;
        this.d = dist;
    }

    public Element getElement() {
        return element;
    }
}
