package sim.test;

import java.io.Serializable;

import sim.engine.SimState;
import sim.engine.Steppable;

public class TestAgent implements Steppable, Serializable {
	private static final long serialVersionUID = 1L; // For checkpointing (see p49)
	
	public void step(SimState state) {
		TestState testState = (TestState) state;
		
		int x = testState.random.nextInt(101);
		int y = testState.random.nextInt(101);
		
		testState.grid.setObjectLocation(this, x, y);
	}
}
