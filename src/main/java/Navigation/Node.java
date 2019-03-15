package Navigation;

import Model.Elements.Element;

public class Node {
    public Element element;
    public int d;

    public Node previousNode;

    // A parameterized student constructor
    public Node(Element e, int dist, Node previousNode) {

        this.element = e;
        this.d = dist;
        this.previousNode = previousNode;

    }

    public Element getElement() {
        return element;
    }
    public Node getPreviousNode() {return previousNode;}
}
