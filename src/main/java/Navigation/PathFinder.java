package Navigation;

import Model.Elements.Element;

import java.util.Stack;

public abstract class PathFinder {
    abstract Stack<Element> getPath();
    abstract void findPath();
}
