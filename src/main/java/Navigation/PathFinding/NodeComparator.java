package Navigation.PathFinding;

import Navigation.PathFinding.Node;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {

    // Overriding compare()method of Comparator
    // for descending order of cgpa
    public int compare(Node e1, Node e2) {
        if (e1.d < e2.d)
            return -1;
        else if (e1.d > e2.d)
            return 1;
        return 0;
    }

}
