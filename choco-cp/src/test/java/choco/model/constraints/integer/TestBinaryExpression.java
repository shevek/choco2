package choco.model.constraints.integer;

import static choco.Choco.*;
import static choco.Choco.geq;
import static choco.Choco.makeIntVar;
import static choco.Choco.minus;
import static choco.Choco.*;
import static choco.Choco.neq;
import static choco.Choco.scalar;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.scheduling.SchedUtilities;

public class TestBinaryExpression {


	public IntegerVariable v0, v1, v2;

	public static final int MAX = 100;

	public static final int NB_TESTS = 20;

	public int c0, c1, cste;

	interface IConstraintBuilder {

		Constraint[] makeConstraints();

	}

	protected final IConstraintBuilder eqSumBuilder = new IConstraintBuilder() {

		@Override
		public Constraint[] makeConstraints() {
			return new Constraint[] {
					eq( plus(v0, v1), cste), 
					eq( minus(v0, v2), cste),
					eq( getSumPlus(), cste),
					eq( getSumMinus(), cste)
			};
		}
	};
	
	protected final IConstraintBuilder geqSumBuilder = new IConstraintBuilder() {

		@Override
		public Constraint[] makeConstraints() {
			return new Constraint[] {
					geq( plus(v0, v1), cste), 
					geq( minus(v0, v2), cste),
					geq( getSumPlus(), cste),
					geq( getSumMinus(), cste)
			};
		}

	};
	
	protected final IConstraintBuilder neqSumBuilder = new IConstraintBuilder() {

		@Override
		public Constraint[] makeConstraints() {
			return new Constraint[] {
					neq( plus(v0, v1), cste), 
					neq( minus(v0, v2), cste),
					neq( getSumPlus(), cste),
					neq( getSumMinus(), cste)
			};
		}
	};
	
	protected final IConstraintBuilder eqScalarBuilder = new IConstraintBuilder() {

			
		@Override
		public Constraint[] makeConstraints() {
			return new Constraint[] {
					eq( getScalarPlus(), cste),
					eq( getScalarMinus(), cste)
			};
		}
	};
	
	protected final IConstraintBuilder geqScalarBuilder = new IConstraintBuilder() {

		@Override
		public Constraint[] makeConstraints() {
			return new Constraint[] {
					geq( getScalarPlus(), cste),
					geq( getScalarMinus(), cste)
			};
		}
	};
	
	protected final IConstraintBuilder neqScalarBuilder = new IConstraintBuilder() {

		@Override
		public Constraint[] makeConstraints() {
			return new Constraint[] {
					neq( getScalarPlus(), cste),
					neq( getScalarMinus(), cste)
			};
		}
	};

	private Model m;
	private CPSolver s;



	protected void generateData(int seed) {
		Random rnd= new Random(seed);
		v0 = makeIntVar("v0", - rnd.nextInt(MAX), rnd.nextInt(MAX));
		v1 = makeIntVar("v1", - rnd.nextInt(MAX), rnd.nextInt(MAX));
		v2 = makeIntVar("v2", - v1.getUppB(), - v1.getLowB());
		c0 = rnd.nextInt(4);
		c1 = rnd.nextInt(4);
		cste = v0.getLowB() - v1.getLowB() + rnd.nextInt( v0.getDomainSize() + v1.getDomainSize() + 5);
	}

	
	protected final void testSum(IConstraintBuilder builder) {
		for (int i = 0; i < NB_TESTS; i++) {
			generateData(i);
			test(-1, builder.makeConstraints());
		}
	}
	
	protected final void testScalar(IConstraintBuilder builder) {
		generateData(-1);
		c0 = 0;
		test(-1, builder.makeConstraints());
		c1 = 0;
		test(-1, builder.makeConstraints());
		for (int i = 0; i < NB_TESTS; i++) {
			generateData(i);
			test(-1, builder.makeConstraints());
		}
	}
	
	protected final void test(int nbsols, Constraint...constraints) {
		CPSolver[] solvers = new CPSolver[constraints.length];
		for (int i = 0; i < constraints.length; i++) {
			m = new CPModel();
			m.addConstraint(constraints[i]);
			solvers[i] = new CPSolver();
			solvers[i].read(m);
			//System.out.println(solvers[i].pretty());
		}		
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);
		SchedUtilities.compare(nbsols, SchedUtilities.CHECK_NODES, "Compare Equivalent BinExp", solvers);
	}


	private final IntegerExpressionVariable getSumPlus() {
		return sum(new IntegerVariable[]{v0, v1});
	}

	private final IntegerExpressionVariable getSumMinus() {
		return scalar(new int[]{ 1, -1}, new IntegerVariable[]{v0, v2});
	}

	private final IntegerExpressionVariable getScalarPlus() {
		return scalar(new int[]{ c0, c1}, new IntegerVariable[]{v0, v1});
	}

	private final IntegerExpressionVariable getScalarMinus() {
		return scalar(new int[]{ c0, -c1}, new IntegerVariable[]{v0, v2});
	}


	@Test
	public final void testSumEq() {
		testSum(eqSumBuilder);	
	}


	@Test
	public final void testSumGeq() {
		testSum(geqSumBuilder);

	}

	@Test
	public final void testSumNeq() {
		testSum(neqSumBuilder);
	}

	@Test
	public final void testScalarEq() {
		testScalar(eqScalarBuilder);	
	}

	@Test
	public final void testScalarGeq() {
		testScalar(geqScalarBuilder);
	}

	@Test
	public final void testScalarNeq() {
		testScalar(neqScalarBuilder);
	}

	@Test
	public final void testIrreductible() {
		m = new CPModel();
		v0 = makeIntVar("v0", 1, 5);
		v1 = makeIntVar("v1", 1, 5);
		final IntegerExpressionVariable exp=  minus( mult(2,v0), mult(2, v1) );

		//Equal
		m.addConstraint( eq( exp, 3) );
		s = new CPSolver();
		s.read(m);
		SchedUtilities.solveRandom(s, 0, -1, "Equal");

		//GreaterOrEqual
		m = new CPModel();
		m.addConstraint( geq( exp, 9));
		s = new CPSolver();
		s.read(m);
		SchedUtilities.solveRandom(s, 0, -1, "Equal");
	}


	@Test
	public final void testGeq() {
		test(1, geq( mult(2, Choco.makeIntVar("v", 0,5)), 9),  geq( mult(3, Choco.makeIntVar("v", 0,5)), 13));		
	}
}