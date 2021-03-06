package sim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.junit.Test;

import org.junit.rules.ExpectedException;
import sim.field.grid.Grid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import sim.util.IntBag;

public class SimTest {

	/////////////////////////
	/// Integration tests ///
	/////////////////////////

	/**
	 * TEST CASE 1
	 * Initialise two custom SimState objects with the same random seed.
	 * Run them both and assert that the state is the same.
	 */
	@Test
	public void simState_testDuplicable() {
		TestState t1 = new TestState(4);
		TestState t2 = new TestState(4);
		
		t1.start();
		TestAgent a1 = (TestAgent) t1.grid.getObjectsAtLocation(50, 50).get(0);
		assertNotNull(a1);
		t1.schedule.step(t1);	
		
		t2.start();
		TestAgent a2 = (TestAgent) t2.grid.getObjectsAtLocation(50, 50).get(0);
		assertNotNull(a2);
		t2.schedule.step(t2);	
		
		Int2D l1 = t1.grid.getObjectLocation(a1);
		Int2D l2 = t2.grid.getObjectLocation(a2);
		
		assertEquals(l1, l2);
		
	}
	
	/**
	 * TEST CASE 2
	 * Run a simulation and checkpoint it.
	 * Reload simulation and check results are the same.
	 */
	@Test
	public void simState_testCheckpointing() {
		final String checkpointFileName = "simState_testCheckpointing.checkpoint";

		TestState t1 = new TestState(4);
		t1.start();
		TestAgent a1 = (TestAgent) t1.grid.getObjectsAtLocation(50, 50).get(0);
		
		// Run for 1000 steps
		for(int i = 0; i < 1000; i++) {
			t1.schedule.step(t1);
		}
		// Get object location
		Int2D intermediateLocation = t1.grid.getObjectLocation(a1);

		// Checkpoint
		File checkpointFile = new File(checkpointFileName);
		t1.writeToCheckpoint(checkpointFile);
		// Run for another 1000 steps TODO: keep running simulation or just check that state is the same?
		for(int i = 0; i < 1000; i++) {
			t1.schedule.step(t1);
		}

		TestState t2 = (TestState) TestState.readFromCheckpoint(new File(checkpointFileName));
		TestAgent a2 = (TestAgent) t2.grid.getObjectsAtLocation(intermediateLocation.x, intermediateLocation.y).get(0);

		// Run checkpointed state for 1000 steps
		for(int i = 0; i < 1000; i++) {
			t2.schedule.step(t2);
		}
		
		// Check that state is the same
		Int2D l1 = t1.grid.getObjectLocation(a1);
		Int2D l2 = t2.grid.getObjectLocation(a2);

		assertEquals(l1, l2);
	}

    /**
     * TEST CASE 3
	 * Model independence
     */
	@Test
    public void simState_stepOnOtherModel() {
		TestState t1 = new TestState(4);
		TestState t2 = new TestState(6);

		t1.start();
		t2.start();
		t1.schedule.step(t2); // This adds a1 to t2 - quite weird behaviour
		TestAgent a1 = (TestAgent) t1.grid.getAllObjects().get(0);
		TestAgent a2 = (TestAgent) t2.grid.getAllObjects().get(0);
		System.out.println(t1.grid.getAllObjects().size());
		System.out.println(t2.grid.getAllObjects().size());

		Int2D l1 = t1.grid.getObjectLocation(a1);
		Int2D l2 = t2.grid.getObjectLocation(a2);
		System.out.println(l1);
		System.out.println(l2);
    }

	private class State {
    	int id;
    	Int2D location;
    	int numAgents;

    	public State(int id, Int2D location, int numAgents) {
    		this.id = id;
    		this.location = location;
    		this.numAgents = numAgents;
		}

		@Override
		public String toString() {
    		return "id: " + id + "\nLocation: " + location + "\nNumAgents: " + numAgents;
		}

		@Override
		public boolean equals(Object obj) {
    		State state = (State) obj;
    		return (this.location.equals(state.location)) && (this.numAgents == state.numAgents);
		}
	}

	/**
	 * TEST CASE 4
	 * Encapsualtion test
	 */
	@Test
	public void simState_testEncapsulation() throws ExecutionException, InterruptedException {
		TestState t1 = new TestState(4);
		t1.start();
		TestAgent a1 = (TestAgent) t1.grid.getAllObjects().get(0);
		for(int i = 0; i < 1000; i++) {
			t1.schedule.step(t1);
		}
		State t1state = new State(1, t1.grid.getObjectLocation(a1), t1.grid.getAllObjects().size());

		TestState t2 = new TestState(6);
		t2.start();
		TestAgent a2 = (TestAgent) t2.grid.getAllObjects().get(0);
		for(int i = 0; i < 1000; i++) {
			t2.schedule.step(t2);
		}
		State t2state = new State(2, t2.grid.getObjectLocation(a2), t2.grid.getAllObjects().size());

		ExecutorService es = Executors.newFixedThreadPool(2);

		final ArrayList<Callable<State>> tasks = new ArrayList<>();
		tasks.add(new Callable<State>() {
			@Override
			public State call() throws Exception {
				TestState t3 = new TestState(4);
				t3.start();
				TestAgent a3 = (TestAgent) t3.grid.getAllObjects().get(0);
				for(int i = 0; i < 1000; i++) {
					t3.schedule.step(t3);
				}
				State t3state = new State(3, t3.grid.getObjectLocation(a3), t3.grid.getAllObjects().size());
				return t3state;
			}
		});
		tasks.add(new Callable<State>() {
			@Override
			public State call() throws Exception {
				TestState t4 = new TestState(6);
				t4.start();
				TestAgent a4 = (TestAgent) t4.grid.getAllObjects().get(0);
				for(int i = 0; i < 1000; i++) {
					t4.schedule.step(t4);
				}
				State t4state = new State(4, t4.grid.getObjectLocation(a4), t4.grid.getAllObjects().size());
				return t4state;
			}
		});

		final List<Future<State>> futures = es.invokeAll(tasks);
		for (Future<State> future : futures)
		{
			final State resultOfTask = future.get();
			if(resultOfTask.id == 3) {
				assertEquals(t1state, resultOfTask);
			} else {
				assertEquals(t2state, resultOfTask);
			}
		}
	}

	////////////////////
	/// System tests ///
	////////////////////

    /**
	 * TEST CASE 5
     * System-level test
     */
    @Test
	public void simState_testRunFromCommandLine() {
		try {
			Runtime rt = Runtime.getRuntime();

			/**
			 * Compile TestState and show command line output
 			 */
			Process compile = rt.exec("javac -cp jar/mason.19.jar:. sim/TestState.java sim/TestAgent.java");
			BufferedReader stdCompileInput = new BufferedReader(new
					InputStreamReader(compile.getInputStream()));

			BufferedReader stdCompileError = new BufferedReader(new
					InputStreamReader(compile.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the compile command:\n");
			String s = null;
			while ((s = stdCompileInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out.println("Here is the standard error of the compile command (if any):\n");
			while ((s = stdCompileError.readLine()) != null) {
				System.out.println(s);
			}

			/**
			 * Run TestState and show command line output
			 */
			Process run = rt.exec("java sim.TestState -seed 4 -for 1000 -time 1");
			BufferedReader stdRunInput = new BufferedReader(new
					InputStreamReader(run.getInputStream()));

			BufferedReader stdRunError = new BufferedReader(new
					InputStreamReader(run.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the run command:\n");
			s = null;
			while ((s = stdRunInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out.println("Here is the standard error of the run command (if any):\n");
			while ((s = stdRunError.readLine()) != null) {
				System.out.println(s);
			}
		} catch(Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

    //////////////////
    /// Unit tests ///
	//////////////////

	/**
	 * TEST CASE 6
	 * Test that neighbouring methods work for unbounded grids and negative locations
	 */
	@Test
	public void sparseGrid_getNegativeLocationsUnboundedGrid() {
		SparseGrid2D sg = new SparseGrid2D(100, 100);
		IntBag xs = new IntBag();
		IntBag ys = new IntBag();
		sg.getMooreLocations(-1, -1, 0, Grid2D.UNBOUNDED, true, xs, ys);
		int[] xsArr = xs.toArray();
		int[] ysArr = ys.toArray();
		assertEquals(1, xsArr.length);
		assertEquals(1, ysArr.length);
		assertEquals(-1, xsArr[0]);
		assertEquals(-1, ysArr[0]);
	}

	/**
	 * TEST CASE 7
	 * Test that SparseGrid2D cannot be initialised with negative width/height
	 */
	@Test(expected = Exception.class)
	public void sparseGrid_initWithNegativeWidth() {
		SparseGrid2D sg = new SparseGrid2D(-100, -100);
	}

//    /**
//     * SparseGrid2D.setObjectLocation(null, _, _) should return false.
//     */
//    @Test
//    public void sparseField_testStoreNullObject() {
//        SparseGrid2D s = new SparseGrid2D(100, 100);
//        assertEquals(false, s.setObjectLocation(null, 1, 1));
//    }
//
//    /**
//     * SparseGrid2D.setObjectLocation(_, null) should return false.
//     */
//    @Test
//    public void sparseField_testStoreNullLocation() {
//        SparseGrid2D s = new SparseGrid2D(100, 100);
//        assertEquals(false, s.setObjectLocation(new TestAgent(), null));
//    }
//
//    /**
//     * Remove objects from invalid location returns null.
//     */
//    @Test
//    public void sparseField_testRemoveObjectsInvalidLocation() {
//       SparseGrid2D s = new SparseGrid2D(100, 100);
//       assertEquals(null, s.removeObjectsAtLocation(101,101));
//    }
//
//    /**
//     * Known issue:
//     * Should not be able to modify bag of all objects in grid.
//     */
//    @Test
//    public void sparseField_modifyGetAllObjects() {
//        SparseGrid2D s = new SparseGrid2D(100, 100);
//        for(int i = 0; i < 10; i++) {
//            s.setObjectLocation(new TestAgent(), 1, 1);
//        }
//        Bag allObjs = s.getAllObjects();
//        allObjs.remove(0);
//        assertEquals(10, s.getAllObjects().size());
//    }
//
//
//    @Test
//    public void intGrid_initWithZeroSize() {
//        IntGrid2D ig = new IntGrid2D(-0, -0);
//        ig.set(0, 0, 21);
//    }
}
