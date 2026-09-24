"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { VisualizationEngine } from "./engine";
import type { VisualizationState, VisualizationStep } from "./types";

const INITIAL_STATE: VisualizationState = {
  currentStep: null,
  currentStepIndex: -1,
  totalSteps: 0,
  status: "idle",
  speed: 2,
};

export interface UseVisualizerReturn extends VisualizationState {
  play: () => void;
  pause: () => void;
  stepForward: () => void;
  stepBack: () => void;
  reset: () => void;
  setSpeed: (s: number) => void;
}

export function useVisualizer(steps: VisualizationStep[], initialSpeed = 2): UseVisualizerReturn {
  const engineRef = useRef<VisualizationEngine | null>(null);
  const [vizState, setVizState] = useState<VisualizationState>(INITIAL_STATE);

  // Create engine once per component lifetime
  useEffect(() => {
    const engine = new VisualizationEngine();
    engineRef.current = engine;
    engine.setSpeed(initialSpeed);
    const unsub = engine.subscribe((s) => setVizState(s));
    return () => {
      unsub();
      engine.destroy();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Reload steps whenever they change (new algorithm or new input)
  useEffect(() => {
    if (!engineRef.current) return;
    engineRef.current.load(steps);
  }, [steps]);

  const play         = useCallback(() => engineRef.current?.play(), []);
  const pause        = useCallback(() => engineRef.current?.pause(), []);
  const stepForward  = useCallback(() => engineRef.current?.stepForward(), []);
  const stepBack     = useCallback(() => engineRef.current?.stepBack(), []);
  const reset        = useCallback(() => engineRef.current?.reset(), []);
  const setSpeed     = useCallback((s: number) => engineRef.current?.setSpeed(s), []);

  return { ...vizState, play, pause, stepForward, stepBack, reset, setSpeed };
}
