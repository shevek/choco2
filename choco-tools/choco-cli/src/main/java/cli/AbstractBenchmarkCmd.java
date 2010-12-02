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



package cli;

import choco.cp.solver.configure.LimitFactory;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.common.util.tools.PropertyUtils;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.search.limit.Limit;
import cli.explorer.FileExplorer;
import cli.explorer.FileProcedure;
import db.DbManager;
import db.EmbeddedDbConnector;
import db.IDbConnector;
import db.RemoteDbConnector;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import parser.instances.AbstractInstanceModel;
import parser.instances.BasicSettings;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;

/**
 * The class define a command pattern designed for benchmarking.
 * It parses input/output arguments as well as global settings (timelimit, seed, verbosity).
 * A connection with an embedded/remote database could also be established.
 *  
 * @author Arnaud Malapert</br> 
 * @since 11 nov. 2009 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public abstract class AbstractBenchmarkCmd extends AbstractCmdLine implements FileProcedure {

	@Option(name="-f",aliases={"--file","-file"},usage="Instance File or directory with optional wildcard pattern arguments.",required=true)
	protected File inputFile;

	@Option(name="-u",aliases={"--url"},usage="connect to remote database at URL.")
	protected String databaseURL;

	@Option(name="-e",aliases={"--export"},usage="activate embedded database and export it to odb file")
	protected File databaseFile;

	@Option(name="-p",aliases={"--properties"},usage="user properties file")
	protected File propertyFile;

	@Option(name="-o",aliases={"--output"},usage="specify output directory (logs, solutions, ...)")
	protected File outputDirectory;

	@Option(name="-s",aliases={"--seed", "-seed"},usage="global seed")
	protected long seed;

	@Option(name="-tl",aliases={"--timeLimit", "-time"},usage="time limit in seconds")
	protected Integer timeLimit;

	@Option(name="-v",aliases={"--verbosity"},usage="set the verbosity level")
	public static void setGlobalVerbosity(Verbosity verbosity) {
		ChocoLogging.setVerbosity(verbosity);
	}

	protected final Configuration settings;
	
	protected Random seeder;

	private IDbConnector dbConnector;

	protected AbstractInstanceModel instance;

	public AbstractBenchmarkCmd(Configuration settings) {
		super(true);
		this.settings = settings;
	}

	private void makeDbConnector() throws CmdLineException {
		if( databaseFile == null) {
			if( databaseURL == null) dbConnector = DISCONNECTED;
			else dbConnector = new RemoteDbConnector(databaseURL);
		} else { 
			if( databaseURL == null) dbConnector = new EmbeddedDbConnector(databaseFile);
			else throw new CmdLineException("choose between embedded (-f) and remote (-u) database.");
		}
	}


	@Override
	protected void checkData() throws CmdLineException {
		if( !inputFile.exists() || !inputFile.canRead() ) {
			throw new CmdLineException(inputFile + "is not a readable file.");
		}
		if( outputDirectory != null && ! outputDirectory.isDirectory()) {
			throw new CmdLineException(outputDirectory+" is not a directory");
		}
		if( propertyFile != null) {
			PropertyUtils.loadProperties(settings, propertyFile);
		}
		makeDbConnector();
		if( timeLimit != null) {
			LimitFactory.setSearchLimit(settings, Limit.TIME, timeLimit * 1000);
		}
		LOGGER.log(Level.CONFIG, "cmd...[seed:{0}][db:{1}][output:{2}]", new Object[]{seed, dbConnector, outputDirectory});
	}


	protected abstract AbstractInstanceModel createInstance();

	protected void configureInstance() {
		if(outputDirectory != null) instance.getConfiguration().putFile(BasicSettings.OUTPUT_DIRECTORY, outputDirectory);
		instance.setDatabaseManager(dbConnector.getDatabaseManager());
		instance.getConfiguration().putLong(Configuration.RANDOM_SEED, seed);
	}

	@Override
	protected void execute() {
		//configure
		dbConnector.setUp();
		instance = createInstance();
		configureInstance();
		final boolean ok = FileExplorer.explore(this, inputFile, arguments); //run benchmark
		assert ok; //for junit 
		dbConnector.tearDown();
	}


	public final static IDbConnector DISCONNECTED =  new IDbConnector() {

		@Override
		public void tearDown() {}

		@Override
		public void setUp() {}

		@Override
		public DbManager getDatabaseManager() {return null;}

		@Override
		public String toString() {
			return "null";
		}


	};

}
