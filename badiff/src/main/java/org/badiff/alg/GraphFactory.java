package org.badiff.alg;


public interface GraphFactory {
	public static final GraphFactory EDIT_GRAPH = new GraphFactory() {
		@Override
		public Graph newGraph(int capacity) {
			return new EditGraph(capacity);
		}
	};
	public static final GraphFactory INERTIAL_GRAPH = new GraphFactory() {
		@Override
		public Graph newGraph(int capacity) {
			return new InertialGraph(capacity);
		}
	};
	public static final GraphFactory ADJUSTABLE_GRAPH = new GraphFactory() {
		@Override
		public Graph newGraph(int capacity) {
			return new AdjustableInertialGraph(capacity);
		}
	};

	public Graph newGraph(int capacity);
}