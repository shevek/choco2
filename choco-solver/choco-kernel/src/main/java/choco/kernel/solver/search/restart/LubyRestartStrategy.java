/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.kernel.solver.search.restart;

import choco.kernel.common.util.tools.MathUtils;

public final class LubyRestartStrategy  extends AbstractRestartStrategy {
	
	private int geometricalIntFactor;

	private int divFactor;


	public LubyRestartStrategy(int scaleFactor,
			int geometricalFactor) {
		super("LUBY", scaleFactor, geometricalFactor);
	}

	@Override
	public final void setGeometricalFactor(double geometricalFactor) {
		checkPositiveValue(geometricalFactor);
		double f= Math.floor(geometricalFactor);
		if(f != geometricalFactor) {throw new IllegalArgumentException("Luby geometrical parameter should be an integer");}
		super.setGeometricalFactor(geometricalFactor);
		geometricalIntFactor = (int) geometricalFactor;
		divFactor = geometricalIntFactor - 1;
	}

	private static final int geometricalSum(int value, int exponent) {
		return  ( MathUtils.pow(value,exponent)-1 ) / ( value -1 );
	}
	

	protected final int getLasVegasCoef(int i) {
		//<hca> I round it to PRECISION because of issues between versions of the jvm on mac and pc
		final double log = MathUtils.roundedLog( i * divFactor + 1,geometricalIntFactor);
		final int k = (int) Math.floor(log);
		if(log == k) {
			return MathUtils.pow(geometricalIntFactor,k-1);
		}else {
			//recursion
			return getLasVegasCoef(i - geometricalSum(geometricalIntFactor, k));
		}
	}


	@Override
	public int getNextCutoff(int nbRestarts) {
		return getLasVegasCoef(nbRestarts +1)*scaleFactor;
	}
	
}
