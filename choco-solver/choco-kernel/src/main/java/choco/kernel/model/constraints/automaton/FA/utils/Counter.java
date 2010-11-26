package choco.kernel.model.constraints.automaton.FA.utils;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 24, 2010
 * Time: 10:24:53 AM
 */
public class Counter implements ICounter
{


int[][] costs;
Bounds bounds;

public Counter(int[][] layer_value, int min, int max)
{
        this.costs  = layer_value;
        this.bounds = Bounds.makeBounds(min,min,null,max,max,null);


}

public Counter(int[][][] layer_value_state, int inf, int sup)
{
        this.costs = new int[layer_value_state.length][];
        for (int i = 0 ; i < layer_value_state.length ; i++)
        {
                this.costs[i] = new int[layer_value_state[i].length];
                for (int j = 0 ; j < this.costs[i].length ; j++)
                {
                        this.costs[i][j] = layer_value_state[i][j][0];
                }
        }
}


@Override
public Bounds bounds()
{
        return bounds;
}

@Override
public double cost(int layer, int value)
{
        return this.costs[layer][value];
}

@Override
public double cost(int layer, int value, int state)
{
        return cost(layer,value);
}
}
