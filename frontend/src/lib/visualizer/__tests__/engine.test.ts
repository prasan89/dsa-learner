import { VisualizationEngine } from "../engine";
import type { VisualizationStep, VisualizationState } from "../types";

// ─── Helpers ────────────────────────────────────────────────────────────────

function makeStep(msg: string): VisualizationStep {
  return {
    cells: [{ value: msg.length, index: 0, state: "default" }],
    pointers: {},
    variables: {},
    message: msg,
  };
}

function makeSteps(n: number): VisualizationStep[] {
  return Array.from({ length: n }, (_, i) => makeStep(`step ${i}`));
}

// ─── Tests ───────────────────────────────────────────────────────────────────

describe("VisualizationEngine", () => {

  describe("load()", () => {
    it("sets currentStepIndex to 0 after loading non-empty steps", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      expect(engine.snapshot().currentStepIndex).toBe(0);
    });

    it("sets totalSteps correctly", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(7));
      expect(engine.snapshot().totalSteps).toBe(7);
    });

    it("sets status to 'idle' after load", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(3));
      expect(engine.snapshot().status).toBe("idle");
    });

    it("provides the first step as currentStep", () => {
      const engine = new VisualizationEngine();
      const steps = makeSteps(3);
      engine.load(steps);
      expect(engine.snapshot().currentStep?.message).toBe("step 0");
    });

    it("resets to step 0 if called again while playing", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      engine.stepForward();
      engine.stepForward();
      engine.load(makeSteps(3));
      expect(engine.snapshot().currentStepIndex).toBe(0);
      engine.destroy();
    });
  });

  describe("stepForward()", () => {
    it("advances currentStepIndex by 1", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      engine.stepForward();
      expect(engine.snapshot().currentStepIndex).toBe(1);
    });

    it("sets status to 'paused' when not playing", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      engine.stepForward();
      expect(engine.snapshot().status).toBe("paused");
    });

    it("sets status to 'done' on last step", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(2));
      engine.stepForward(); // now at index 1 = last
      expect(engine.snapshot().status).toBe("done");
    });

    it("does not go beyond last step", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(2));
      engine.stepForward();
      engine.stepForward(); // already at last, status done
      expect(engine.snapshot().currentStepIndex).toBe(1);
    });
  });

  describe("stepBack()", () => {
    it("decrements currentStepIndex", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      engine.stepForward();
      engine.stepForward();
      engine.stepBack();
      expect(engine.snapshot().currentStepIndex).toBe(1);
    });

    it("does not go below 0", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(3));
      engine.stepBack();
      expect(engine.snapshot().currentStepIndex).toBe(0);
    });

    it("sets status to 'paused'", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(3));
      engine.stepForward();
      engine.stepBack();
      expect(engine.snapshot().status).toBe("paused");
    });
  });

  describe("reset()", () => {
    it("returns to step 0", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      engine.stepForward();
      engine.stepForward();
      engine.reset();
      expect(engine.snapshot().currentStepIndex).toBe(0);
    });

    it("sets status to 'idle'", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      engine.stepForward();
      engine.reset();
      expect(engine.snapshot().status).toBe("idle");
    });
  });

  describe("setSpeed()", () => {
    it("clamps speed to minimum 0.5", () => {
      const engine = new VisualizationEngine();
      engine.setSpeed(0);
      expect(engine.snapshot().speed).toBe(0.5);
    });

    it("clamps speed to maximum 10", () => {
      const engine = new VisualizationEngine();
      engine.setSpeed(999);
      expect(engine.snapshot().speed).toBe(10);
    });

    it("sets valid speed", () => {
      const engine = new VisualizationEngine();
      engine.setSpeed(4);
      expect(engine.snapshot().speed).toBe(4);
    });
  });

  describe("subscribe()", () => {
    it("immediately calls the listener with current state", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(3));
      const received: VisualizationState[] = [];
      engine.subscribe((s) => received.push(s));
      expect(received.length).toBe(1);
      expect(received[0].totalSteps).toBe(3);
      engine.destroy();
    });

    it("notifies subscriber on stepForward", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(3));
      const states: number[] = [];
      engine.subscribe((s) => states.push(s.currentStepIndex));
      engine.stepForward();
      // initial emit (index 0) + stepForward emit (index 1)
      expect(states).toEqual([0, 1]);
      engine.destroy();
    });

    it("unsubscribe stops notifications", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(3));
      const states: number[] = [];
      const unsub = engine.subscribe((s) => states.push(s.currentStepIndex));
      unsub();
      engine.stepForward();
      // Only the initial emit captured before unsubscribe
      expect(states).toEqual([0]);
      engine.destroy();
    });
  });

  describe("play/pause via setInterval", () => {
    beforeEach(() => jest.useFakeTimers());
    afterEach(() => jest.useRealTimers());

    it("advances steps automatically when playing", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      engine.setSpeed(2); // 500ms per step
      engine.play();
      expect(engine.snapshot().status).toBe("playing");

      jest.advanceTimersByTime(500);
      expect(engine.snapshot().currentStepIndex).toBe(1);

      jest.advanceTimersByTime(500);
      expect(engine.snapshot().currentStepIndex).toBe(2);
      engine.destroy();
    });

    it("stops advancing after pause()", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(5));
      engine.setSpeed(2);
      engine.play();
      jest.advanceTimersByTime(500);
      engine.pause();
      const idx = engine.snapshot().currentStepIndex;
      jest.advanceTimersByTime(1000);
      expect(engine.snapshot().currentStepIndex).toBe(idx);
      engine.destroy();
    });

    it("sets status to 'done' when all steps played", () => {
      const engine = new VisualizationEngine();
      engine.load(makeSteps(3));
      engine.setSpeed(2);
      engine.play();
      // 3 steps: step at 0ms (load), advance at 500ms, advance at 1000ms, done
      jest.advanceTimersByTime(1500);
      expect(engine.snapshot().status).toBe("done");
      engine.destroy();
    });
  });

  describe("snapshot() without load", () => {
    it("returns null currentStep when no steps loaded", () => {
      const engine = new VisualizationEngine();
      expect(engine.snapshot().currentStep).toBeNull();
      expect(engine.snapshot().totalSteps).toBe(0);
    });
  });
});
