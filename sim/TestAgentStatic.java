package sim;

import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.Serializable;

public class TestAgentStatic implements Steppable, Serializable {
	private static final long serialVersionUID = 1L; // For checkpointing (see p49)
	
	public void step(SimState state) {
		TestStateStatic testState = (TestStateStatic) state;

		int x = testState.random.nextInt(101);
		int y = testState.random.nextInt(101);
		
		testState.grid.setObjectLocation(this, x, y);
	}
}
