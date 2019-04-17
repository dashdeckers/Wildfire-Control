package Navigation.PathFinding;

import Model.Agent;
import Model.Elements.Element;

import java.util.List;
import java.util.Stack;

public class BresenhamPath extends PathFinder {

    Element goal;
    Stack<Element> path;
    Agent agent;
    List<List<Element>> cells;

    boolean cutPath;

    public BresenhamPath(List<List<Element>> cells, Agent agent, Element goal, boolean cutPath){
        this.cells = cells;
        this.agent = agent;
        this.goal = goal;
        this.cutPath = cutPath;
    }

    /**
     * Function used to make a Stack of all cells that the agent needs to travel in order to reach the goal in a
     * straight line. Quadrants are indicated from the perspective of the agent, i.e. a goal North-East to the agent is
     * in quadrant I. N-W is quadrant II, S-W quadrant III and S-E quadrant IV.
     *
     * As the path needs the added the a stack, the path needs to be determined in reverse order (goal -> agent).
     * By doing so, the agent can pop the element from the stack in order to determine its next move.
     */
    public void findPath(){

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

        this.path = new Stack<>();

        if (dy>=0) {
            if (dx >= 0) {
                /*
                Quadrant III
                 */
                if (dx > dy) {
                    determineStraightPath(y0, x0, y1, x1, false, 0, 0);
                } else {
                    determineStraightPath(x0, y0, x1, y1, true, 0, 0);
                }
            } else {
                /*
                Quadrant IV
                 */
                if (Math.abs(dx) > dy) {
                    determineStraightPath(y0, x1, y1, x0, false, 0, 1);
                } else {
                    determineStraightPath(x1, y0, x0, y1, true, 1, 0);
                }
            }
        } else {
            if (dx >= 0) {
                /*
                Quadrant II
                 */
                if (dx > Math.abs(dy)) {
                    determineStraightPath(y1, x0, y0, x1, false, 1, 0);
                } else {
                    determineStraightPath(x0, y1, x1, y0, true, 0, 1);
                }
            } else {
                /*
                Quadrant I
                 */
                if (Math.abs(dx) > Math.abs(dy)) {
                    determineStraightPath(y1, x1, y0, x0, false, 1, 1);
                } else {
                    determineStraightPath(x1, y1, x0, y0, true, 1, 1);
                }
            }
        }
        //this.path=path;
    }

    /**
     * Bresenman implementation for path finding. Determines a straight line from point a (r0, q0) to point b (r1, q1),
     * provided that the dr>=0, dq>=0 and dr/dq>=1. In the default case (sv = false, qf=0, rf=0), r corresponds to
     * the x-axis and q to the y-axis.
     * @param q0
     * @param r0
     * @param q1
     * @param r1
     * @param sv (Swap Values) when dx/dy<1, r==y and q==x such that dr/dq>1, essentially swapping the axis. To push
     *           the correct path to this.path, the usual manner of getting elements out of cells needs to be
     *           swapped as well. i.e. cells.get(q).get(r) instead of cells.get(r).get(q).
     * @param qf (q-Factor) when dy<0, q0=y1 and q1=y0, essentially flipping the line on the y-axis. To counter
     *           for this in the retrieval of cells, cell retrieval has to start from q1+q0-q instead of q.
     * @param rf (r-Factor) when dx<0, r0=x1 and r1=x0, essentially flipping the line on the x-axis. To counter
     *           for this in the retrieval of cells, cell retrieval has to start from r1+r0-r instead of r.
     */
    private void determineStraightPath(int q0, int r0, int q1, int r1, boolean sv, int qf, int rf){

        int p,q,r, x,y,z;
        int dq = q1 - q0;
        int dr = r1 - r0;

        q = q0;
        r = r0;
        p = 2 * dq - dr;

        Element cell;

        while (r<r1){
            x=q*(1-qf)+qf*(q1+q0-q);
            y=r*(1-rf)+rf*(r1+r0-r);
            if (p >= 0) {
                if (sv){
                    pushCell(cells.get(x).get(y));

                    pushCell(cells.get((x+1)*(1-qf)+(x-1)*qf).get(y));
                }
                else {
                    z=x;
                    x=y;
                    y=z;

                    pushCell(cells.get(x).get(y));

                    pushCell(cells.get(x).get((y+1)*(1-qf)+(y-1)*qf));
                }
                q = q + 1;
                p = p + 2 * dq - 2 * dr;
            } else {
                if (sv) {

                    pushCell(cells.get(x).get(y));
                } else {
                    z=x;
                    x=y;
                    y=z;
                    pushCell(cells.get(x).get(y));
                }
                p = p + 2 * dq;
            }
            r = r + 1;
        }

        //Final push to make sure the cell the agent is standing is also cut if needed.
        cell = cells.get(agent.getX()).get(agent.getY());
        if (cutPath && (cell.getType().equals("Grass") || cell.getType().equals("Tree"))){
            this.path.push(cell);
        }
    }

    private void pushCell(Element cell){
        path.push(cell);
        if (cutPath && (cell.getType().equals("Grass") || cell.getType().equals("Tree"))){
            path.push(cell);
        }

    }


    @Override
    public Stack<Element> getPath() {
        return path;
    }
}
