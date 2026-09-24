import type {
  VisualizationStep,
  VisualizationState,
  VisualizerStatus,
  VisualizerListener,
} from "./types";

const DEFAULT_SPEED = 2;        // steps per second
const MIN_SPEED = 0.5;
const MAX_SPEED = 10;

export class VisualizationEngine {
  private steps: VisualizationStep[] = [];
  private currentIndex = -1;
  private status: VisualizerStatus = "idle";
  private speed: number = DEFAULT_SPEED;
  private intervalHandle: ReturnType<typeof setInterval> | null = null;
  private listeners: Set<VisualizerListener> = new Set();

  // ── Load ──────────────────────────────────────────────────────────────────

  load(steps: VisualizationStep[]): void {
    this.clear();
    this.steps = steps;
    this.currentIndex = steps.length > 0 ? 0 : -1;
    this.status = "idle";
    this.emit();
  }

  // ── Playback controls ─────────────────────────────────────────────────────

  play(): void {
    if (this.steps.length === 0) return;
    if (this.status === "done") this.reset();
    this.status = "playing";
    this.startInterval();
    this.emit();
  }

  pause(): void {
    if (this.status !== "playing") return;
    this.status = "paused";
    this.stopInterval();
    this.emit();
  }

  stepForward(): void {
    if (this.currentIndex >= this.steps.length - 1) {
      this.status = "done";
      this.stopInterval();
      this.emit();
      return;
    }
    this.currentIndex++;
    if (this.currentIndex >= this.steps.length - 1) {
      this.status = "done";
      this.stopInterval();
    } else if (this.status !== "playing") {
      this.status = "paused";
    }
    this.emit();
  }

  stepBack(): void {
    if (this.currentIndex <= 0) {
      this.currentIndex = 0;
      this.status = "paused";
      this.stopInterval();
      this.emit();
      return;
    }
    this.currentIndex--;
    this.status = "paused";
    this.stopInterval();
    this.emit();
  }

  reset(): void {
    this.stopInterval();
    this.currentIndex = this.steps.length > 0 ? 0 : -1;
    this.status = "idle";
    this.emit();
  }

  setSpeed(stepsPerSecond: number): void {
    this.speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, stepsPerSecond));
    if (this.status === "playing") {
      // restart interval at new cadence
      this.stopInterval();
      this.startInterval();
    }
    this.emit();
  }

  // ── Subscription ─────────────────────────────────────────────────────────

  subscribe(listener: VisualizerListener): () => void {
    this.listeners.add(listener);
    // immediately push current state to new subscriber
    listener(this.snapshot());
    return () => this.listeners.delete(listener);
  }

  // ── Cleanup ───────────────────────────────────────────────────────────────

  destroy(): void {
    this.stopInterval();
    this.listeners.clear();
  }

  // ── State snapshot ────────────────────────────────────────────────────────

  snapshot(): VisualizationState {
    return {
      currentStep: this.currentIndex >= 0 ? this.steps[this.currentIndex] : null,
      currentStepIndex: this.currentIndex,
      totalSteps: this.steps.length,
      status: this.status,
      speed: this.speed,
    };
  }

  // ── Internals ─────────────────────────────────────────────────────────────

  private startInterval(): void {
    this.stopInterval();
    const delayMs = Math.round(1000 / this.speed);
    this.intervalHandle = setInterval(() => {
      this.advanceOne();
    }, delayMs);
  }

  private stopInterval(): void {
    if (this.intervalHandle !== null) {
      clearInterval(this.intervalHandle);
      this.intervalHandle = null;
    }
  }

  private advanceOne(): void {
    if (this.currentIndex >= this.steps.length - 1) {
      this.status = "done";
      this.stopInterval();
      this.emit();
      return;
    }
    this.currentIndex++;
    if (this.currentIndex >= this.steps.length - 1) {
      this.status = "done";
      this.stopInterval();
    }
    this.emit();
  }

  private clear(): void {
    this.stopInterval();
    this.steps = [];
    this.currentIndex = -1;
    this.status = "idle";
  }

  private emit(): void {
    const state = this.snapshot();
    this.listeners.forEach((l) => l(state));
  }
}
