package Navigation.PathFinding;

import Model.Elements.Element;

import java.util.Stack;

public abstract class PathFinder {
    abstract public Stack<Element> getPath();
    abstract public void findPath();
}
