package Navigation;

import Model.Agent;
import Model.Elements.Element;

import java.math.BigInteger;
import java.util.List;
import java.util.Stack;

public class BresenhamPath extends PathFinder {

    Element goal;
    Stack<Element> path;
    Agent agent;
    List<List<Element>> cells;

    public BresenhamPath(List<List<Element>> cells, Agent agent, Element goal){
        this.cells = cells;
        this.agent = agent;
        this.goal = goal;
    }

    public void findPath(){
//        int dx = goal.getX()-agent.getX();
//        int dy = goal.getX()-agent.getY();
//        int gcd = BigInteger.valueOf(dx).gcd(BigInteger.valueOf(dy)).intValue();
//    }
//
//    private List<String> computeDiagonal(){

        int dx, dy, p, x, y;
        int x0, x1, y0, y1;
        x1=agent.getX();
        y1=agent.getY();
        x0=goal.getX();
        y0=goal.getY();
        dx=x1-x0;
        dy=y1-y0;
        x = x0;
        y = y0;
        if (dx>dy) {
            determineStraightPath(y0,x0,y1,x1);
        }
        else{
            determineStraightPath(x0,y0,x1,y1);
        }
        //this.path=path;
    }

    private void determineStraightPath(int q0, int r0, int q1, int r1){

        Stack<Element> path = new Stack<>();

        int p,q,r;
        int dq = q1 - q0;
        int dr = r1 - r0;

        q = q0;
        r = r0;
        p = 2 * dq - dr;

        while (r<r1){
            System.out.println("x: " + q + " y: " + r + " p: " + p);
            if (p >= 0) {
                path.push(cells.get(q).get(r));
                path.push(cells.get(q + 1).get(r));
                q = q + 1;
                p = p + 2 * dq - 2 * dr;
            } else {
                path.push(cells.get(q).get(r));
                p = p + 2 * dq;
            }
            r = r + 1;
        }
        this.path = path;
    }


    @Override
    public Stack<Element> getPath() {
        return path;
    }
}
