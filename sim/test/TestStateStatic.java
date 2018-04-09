package sim.test;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;

import java.io.Serializable;

public class TestStateStatic extends SimState implements Serializable {
	private static final long serialVersionUID = 1L; // For checkpointing (see
														// p49)

	public static SparseGrid2D grid = new SparseGrid2D(100, 100);

	public TestStateStatic(long seed) {
		super(seed);
	}

	public void start() {
		super.start();

		grid.clear();

		TestAgentStatic t = new TestAgentStatic();
		grid.setObjectLocation(t, 50, 50);

		schedule.scheduleRepeating(t);
	}

}
