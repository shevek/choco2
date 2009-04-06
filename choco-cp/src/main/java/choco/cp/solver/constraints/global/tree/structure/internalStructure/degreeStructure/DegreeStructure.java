/* * * * * * * * * * * * * * * * * * * * * * * * * 
 *          _       _                            *
 *         |  °(..)  |                           *
 *         |_  J||L _|        CHOCO solver       *
 *                                               *
 *    Choco is a java library for constraint     *
 *    satisfaction problems (CSP), constraint    *
 *    programming (CP) and explanation-based     *
 *    constraint solving (e-CP). It is built     *
 *    on a event-based propagation mechanism     *
 *    with backtrackable structures.             *
 *                                               *
 *    Choco is an open-source software,          *
 *    distributed under a BSD licence            *
 *    and hosted by sourceforge.net              *
 *                                               *
 *    + website : http://choco.emn.fr            *
 *    + support : choco@emn.fr                   *
 *                                               *
 *    Copyright (C) F. Laburthe,                 *
 *                  N. Jussien    1999-2008      *
 * * * * * * * * * * * * * * * * * * * * * * * * */
package choco.cp.solver.constraints.global.tree.structure.internalStructure.degreeStructure;

/* ************************************************
 *           _       _                            *
 *          |  �(..)  |                           *
 *          |_  J||L _|       Choco-Solver.net    *
 *                                                *
 *     Choco is a java library for constraint     *
 *     satisfaction problems (CSP), constraint    *
 *     programming (CP) and explanation-based     *
 *     constraint solving (e-CP). It is built     *
 *     on a event-based propagation mechanism     *
 *     with backtrackable structures.             *
 *                                                *
 *     Choco is an open-source software,          *
 *     distributed under a BSD licence            *
 *     and hosted by sourceforge.net              *
 *                                                *
 *     + website : http://choco.emn.fr            *
 *     + support : choco@emn.fr                   *
 *                                                *
 *     Copyright (C) F. Laburthe,                 *
 *                   N. Jussien   1999-2008       *
 **************************************************/


import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.graphStructures.graphViews.VarGraphView;
import choco.kernel.memory.trailing.StoredBitSet;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;


public class DegreeStructure {

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affiche = false;

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * an integer variable that depicts the number of tree allowed to partition the graph
     */
    protected TreeParameters tree;

    /**
     * data structure related to the graph representation of the input graph
     */
    protected VarGraphView graph;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbVertices;

    /**
     * the number of nodes not yet fixed: the left nodes in the network flow
     */
    protected int nbLeftVertices;

    /**
     * network flow associated with the gcc
     */
    protected BitSet[] gccVars;

    /**
     * index who help to find a graph node from a network node
     */
    protected int[] indexVars;

    protected int[] OriginalMinFlow;

    protected int[] OriginalMaxFlow;

    /**
     * minimum current flow
     */
    protected int[] low;

    /**
     * maximum current flow
     */
    protected int[] up;

    protected boolean degree;

    /**
     * constructor
     *
     * @param solver    the Choco solver who uses the current tree constraint
     * @param tree  the input data structure available in the <code> structure.inputStructure </code> package
     */
    public DegreeStructure(Solver solver, TreeParameters tree, VarGraphView graph) {
        this.solver = solver;
        this.tree = tree;
        this.graph = graph;
        this.nbVertices = tree.getNbNodes();
        this.initFlow();
    }

    private void initFlow() {
        // build the network associated with a gcc constraint
        this.indexVars = new int[nbVertices];
        this.degree = true;
        for (int i = 0; i < nbVertices; i++) {
			indexVars[i] = i;
		}
        this.gccVars = new BitSet[nbVertices];
        nbLeftVertices = 0;
        nbLeftVertices = nbVertices;
        for (int i = 0; i < nbVertices; i++) {
            // +1 comes from the modeling of state "potential loop" in the network
            BitSet succ = new BitSet(nbVertices + 1);
            StoredBitSet next_i = graph.getGlobal().getSuccessors(i);
            for (int j = next_i.nextSetBit(0); j >= 0; j = next_i.nextSetBit(j + 1)) {
                if (j != i) {
					succ.set(j, true);
				}
                if (j == i) {
					succ.set(nbVertices, true);
				}
            }
            this.gccVars[i] = succ;
        }
        this.OriginalMinFlow = new int[nbVertices + 1];
        for (int i = 0; i < OriginalMinFlow.length; i++) {
			OriginalMinFlow[i] = 0;
		}
        this.low = new int[nbVertices + 1];
        for (int i = 0; i < low.length; i++) {
            if (i < nbVertices) {
                IntDomainVar deg_i = tree.getNodes()[i].getInDegree();
                low[i] = deg_i.getInf();
                OriginalMinFlow[i] = deg_i.getInf();
            }
            if (i == nbVertices) {
                low[i] = tree.getNtree().getInf();
                OriginalMinFlow[i] = tree.getNtree().getInf();
            }
        }
        this.OriginalMaxFlow = new int[nbVertices + 1];
        for (int i = 0; i < OriginalMaxFlow.length; i++) {
			OriginalMaxFlow[i] = 0;
		}
        this.up = new int[nbVertices + 1];
        for (int i = 0; i < up.length; i++) {
            if (i < nbVertices) {
                IntDomainVar deg_i = tree.getNodes()[i].getInDegree();
                up[i] = deg_i.getSup();
                OriginalMaxFlow[i] = deg_i.getSup();
            }
            if (i == nbVertices) {
                up[i] = tree.getNtree().getSup();
                OriginalMaxFlow[i] = tree.getNtree().getSup();
            }
        }
    }

    /**
     *
     * @return <code> true </code> iff there is any change in the structures that leads to an udpate of the degree
     * constraints
     */
    public boolean needUpdate() {
        initFlow();
        for (int i = 0; i <= nbVertices; i++) {
            int deg_i = 0;
            boolean instNull_i = (OriginalMaxFlow[i] == OriginalMaxFlow[i] && OriginalMaxFlow[i] == 0);
            for (BitSet gccVar : gccVars) {
                if (gccVar.get(i)) {
					deg_i++;
				}
            }
            if (!instNull_i && deg_i <= OriginalMaxFlow[i]) {
                return true;
            }
            if (instNull_i && deg_i > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * update the capacities of the arcs involved in the network associated with the gcc
     */
    public void updateDegree() {
        int[] mindeg = new int[low.length];
        int[] maxdeg = new int[up.length];
        updateCurrentDeg(mindeg,maxdeg);
        updateGccVars(mindeg,maxdeg);
        // update the network flow
        for (int i = 0; i <= nbVertices; i++) {
            up[i] = maxdeg[i];
            low[i] = mindeg[i];
        }
        // synchronize the degrees
        for (int i = 0; i < nbVertices; i++) {
            int nbused_i;
            int nbpotused_i;
            StoredBitSet maybePred_i = graph.getMaybe().getPredecessors(i);
            StoredBitSet surePred_i = graph.getSure().getPredecessors(i);
            if (surePred_i.get(i)) {
				nbused_i = surePred_i.cardinality() - 1;
			} else {
				nbused_i = surePred_i.cardinality();
			}
            if (maybePred_i.get(i)) {
				nbpotused_i = maybePred_i.cardinality() - 1;
			} else {
				nbpotused_i = maybePred_i.cardinality();
			}
            if (nbpotused_i < nbused_i) {
				nbpotused_i = nbused_i;
			}
            if (nbused_i > OriginalMaxFlow[i]) {
				degree = false;
			}
            if (nbpotused_i < OriginalMinFlow[i]) {
				degree = false;
			}
        }
        if (affiche) {
            System.out.println("*********************************");
            for (int i = 0; i < nbVertices + 1; i++) {
                System.out.println("deg[" + i + "] = [" + low[i] + "," + up[i] + "]");
            }
            System.out.println("------------------------------------------");
            for (int i = 0; i < nbVertices; i++) {
                System.out.println("gcc[" + i + "] = " + gccVars[i].toString());
            }
            System.out.println("*********************************");
        }
    }

    private void updateCurrentDeg(int[] mindeg, int[] maxdeg) {
        // udpate minFlow[] and maxFlow[] according to ntree and the graph
        int[] canBeUsed = new int[nbVertices + 1];
        int[] usedByInst = new int[nbVertices + 1];
        for (int i = 0; i < canBeUsed.length; i++) {
            canBeUsed[i] = 0;
            usedByInst[i] = 0;
        }
        for (int i = 0; i < nbVertices; i++) {
            StoredBitSet pred_i = graph.getGlobal().getPredecessors(i);
            StoredBitSet surePred_i = graph.getSure().getPredecessors(i);
            if (pred_i.get(i)) {
                canBeUsed[i] += pred_i.cardinality() - 1;
                canBeUsed[nbVertices]++;
                if (surePred_i.get(i)) {
                    usedByInst[i] += surePred_i.cardinality() - 1;
                    usedByInst[nbVertices]++;
                }
            } else {
                canBeUsed[i] += pred_i.cardinality();
                usedByInst[i] += surePred_i.cardinality();
            }
        }
        // update degree according to ntree, the graph and the precedence constraints
        for (int i = 0; i < mindeg.length; i++) {
            if (i < nbVertices) {
                if (OriginalMinFlow[i] <= usedByInst[i] && usedByInst[i] <= OriginalMaxFlow[i]) {
                    mindeg[i] = usedByInst[i];
                } else {
                    mindeg[i] = OriginalMinFlow[i];
                }
            }
            if (i == nbVertices) {
                mindeg[i] = Math.max(tree.getNtree().getInf(), Math.max(usedByInst[i], OriginalMinFlow[i]));
            }
        }
        for (int i = 0; i < maxdeg.length; i++) {
            if (i < nbVertices) {
                if (OriginalMaxFlow[i] >= canBeUsed[i] && canBeUsed[i] >= OriginalMinFlow[i]) {
                    maxdeg[i] = canBeUsed[i];
                } else {
                    maxdeg[i] = OriginalMaxFlow[i];
                }
            }
            if (i == nbVertices) {
                maxdeg[i] = Math.min(tree.getNtree().getSup(), Math.min(canBeUsed[i], OriginalMaxFlow[i]));
            }
        }
    }

    private void updateGccVars(int[] mindeg, int[] maxdeg) {
        // update the network by removing the fixed variables and the related capacities
        int nbInstVars = 0;
        for (int i = 0; i < nbVertices; i++) {
            if (graph.getSure().getSuccessors(i).cardinality() > 0) {
				nbInstVars++;
			}
        }
        nbLeftVertices = nbVertices - nbInstVars;
        this.gccVars = new BitSet[nbVertices - nbInstVars];
        this.indexVars = new int[nbVertices - nbInstVars];
        for (int i = 0; i < gccVars.length; i++) {
			gccVars[i] = new BitSet(nbVertices + 1);
		}
        for (int i = 0; i < indexVars.length; i++) {
			indexVars[i] = i;
		}
        int decal = 0;
        for (int i = 0; i < nbVertices; i++) {
            StoredBitSet maybeSucc_i = graph.getMaybe().getSuccessors(i);
            StoredBitSet sureSucc_i = graph.getSure().getSuccessors(i);
            for (int j = maybeSucc_i.nextSetBit(0); j >= 0; j = maybeSucc_i.nextSetBit(j + 1)) {
                if (i != j) {
					gccVars[i - decal].set(j, true);
				}
                if (i == j) {
					gccVars[i - decal].set(nbVertices, true);
				}
                indexVars[i - decal] = i;
            }
            for (int j = sureSucc_i.nextSetBit(0); j >= 0; j = sureSucc_i.nextSetBit(j + 1)) {
                if (j == i) {
					j = nbVertices;
				}
                decal++;
                maxdeg[j] = maxdeg[j] - 1;
                mindeg[j] = mindeg[j] - 1;
                if (maxdeg[j] < 0) {
                    maxdeg[j] = 0;
                    mindeg[j] = 0;
                }
                if (mindeg[j] < 0) {
                    mindeg[j] = 0;
                }
            }
        }
    }

    public boolean isCompatibleDegree() {
        return degree;
    }

    public int[] getLow() {
        return low;
    }

    public int[] getUp() {
        return up;
    }

    public BitSet[] getGccVars() {
        return gccVars;
    }

    public int getNbLeftVertices() {
        return nbLeftVertices;
    }

    public int[] getIndexVars() {
        return indexVars;
    }
}
