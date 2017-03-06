/*
 * @see :
 * http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
 * http://java-performance.info/jmh/
 */

package org.lsc.sync;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lsc.SimpleSynchronize;
import org.lsc.exception.LscServiceException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@Measurement(batchSize = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class LdapToLdapSyncBenchmark extends AbstractSyncBenchmark {

	private static Log LOGGER = LogFactory.getLog(LdapToLdapSyncBenchmark.class);
	
	private final static String TASK_NAME = "ldapToldap";
	
	@Param({"1000"})
	int entries;
	
	@Param({"0"})
	int workers;
	
	@Param({"0"})
	int timelimit;
	 
	@Setup(Level.Trial)
	public void setup() throws Exception {
		initConf();
		initLDAPServer(TASK_NAME,entries);
	}
	
	@SuppressWarnings("serial")
	@Benchmark
	@Fork(1)
	@Timeout(time = 10, timeUnit = TimeUnit.HOURS)
	public void testMethod() throws Exception {
		SimpleSynchronize synchronizer = new SimpleSynchronize();
		if (workers > 0) {
			synchronizer.setThreads(workers);
		}
		if (timelimit > 0) {
			synchronizer.setTimeLimit(timelimit);
		}
		LOGGER.info("Starting synchronization with "+synchronizer.getThreads()+" workers and time limit of " + synchronizer.getTimeLimit() + " seconds ...");
		
		synchronizer.launch(
				new ArrayList<String>(),
				new ArrayList<String>(){{add(TASK_NAME);}},
				new ArrayList<String>()
		);
		
		LOGGER.info("Finished synchronizing data; " + dstJndiServices.getListPivots().size() + " entries in dst.");
	}

	@TearDown(Level.Trial)
	public void tearDown() throws LscServiceException {
		stopLDAPServer();
	}

    /**
     * ============================== HOW TO RUN THIS TEST: ====================================
     *
     * You can run this test:
     * a) Via the command line:
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar LdapToLdapSyncBenchmark -p entries=500 -p workers=2 -p timelimit=3600
     */
	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(LdapToLdapSyncBenchmark.class.getSimpleName())
				.build();
		new Runner(opt).run();
	}
}
