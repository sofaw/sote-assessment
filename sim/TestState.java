package sim;

import java.io.Serializable;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;

public class TestState extends SimState implements Serializable {
	private static final long serialVersionUID = 1L; // For checkpointing (see
														// p49)

	public SparseGrid2D grid = new SparseGrid2D(100, 100);

	public TestState(long seed) {
		super(seed);
	}

	public void start() {
		super.start();

		grid.clear();

		TestAgent t = new TestAgent();
		grid.setObjectLocation(t, 50, 50);

		schedule.scheduleRepeating(t);
	}

	public static void main(String[] args) {
		doLoop(TestState.class, args);
	}

}
