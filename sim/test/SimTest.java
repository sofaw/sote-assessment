package sim.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import sim.util.Int2D;

public class SimTest {

	/**
	 * Initialise two custom SimState objects with the same random seed.
	 * Run them both and assert that the state is the same.
	 */
	@Test
	public void simState_testDuplicable() {
		TestState t1 = new TestState(4);
		TestState t2 = new TestState(4);
		
		t1.start();
		TestAgent a1 = (TestAgent) t1.grid.getObjectsAtLocation(50, 50).get(0);
		t1.schedule.step(t1);	
		
		t2.start();
		TestAgent a2 = (TestAgent) t2.grid.getObjectsAtLocation(50, 50).get(0);
		t2.schedule.step(t2);	
		
		Int2D l1 = t1.grid.getObjectLocation(a1);
		Int2D l2 = t2.grid.getObjectLocation(a2);
		
		assertEquals(l1, l2);
		
	}
	
	/**
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
	
	/** TODO: Test encapsulation
	 * i.e. MASON models are run in parallel in the same process w/o affecting each other
	 * How to test this?
	 */

}
