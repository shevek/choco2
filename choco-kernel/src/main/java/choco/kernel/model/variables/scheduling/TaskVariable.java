/* ************************************************
 *           _       _                            *
 *          |  °(..)  |                           *
 *          |_  J||L _|        CHOCO solver       *
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
 *                   N. Jussien    1999-2008      *
 **************************************************/
package choco.kernel.model.variables.scheduling;

import choco.kernel.model.ModelException;
import choco.kernel.model.variables.IComponentVariable;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.Properties;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 23 janv. 2009
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 */
public class TaskVariable extends MultipleVariables implements IComponentVariable, ITaskVariable<IntegerVariable>{

	private int NO_HOOK = -1;
	
	protected final String name;

	protected String variableManager;

	private int hook = NO_HOOK;

	public TaskVariable(String name, IntegerVariable start, IntegerVariable end, IntegerVariable duration) {
		super(3);
		this.variables.add(start);
		this.variables.add(end);
		this.variables.add(duration);
		this.name = name;
		this.stored = true;
	}



	public String getComponentClass() {
		return variableManager;
	}

	/**
	 * Get the duration of the task
	 * @return
	 */
	public IntegerVariable duration() {
		return (IntegerVariable)variables.get(2);
	}


	/**
	 * Get the end time of the task
	 * @return
	 */
	public IntegerVariable end() {
		return (IntegerVariable)variables.get(1);
	}


	/**
	 * Get the start time of the task
	 * @return
	 */
	public IntegerVariable start() {
		return (IntegerVariable)variables.get(0);
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	public int getHook() {
		return hook;
	}

	public void setHook(int hook) {
		if (this.hook !=  NO_HOOK) {
			new ModelException("hook already used");
		} else if(hook == NO_HOOK){new ModelException("invalid hook value:"+hook);}
		else {this.hook = hook;}
	}

	public String pretty() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append(name).append(" {").append(start().pretty());
		buffer.append(" + ").append(duration().pretty());
		buffer.append(" = ").append(end().pretty());
		buffer.append('}');
		return buffer.toString();
	}

	@Override
	public final void findManager(Properties propertiesFile) {
		if (variableManager == null) {
			variableManager = propertiesFile.getProperty(VariableType.TASK.property);
		}
		if (variableManager == null) {
			throw new ModelException("Can not find " + type.property + " in application.properties");
		}
		super.findManager(propertiesFile);
	}

}
