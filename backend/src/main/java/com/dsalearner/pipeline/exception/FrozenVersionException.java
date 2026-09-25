package com.dsalearner.pipeline.exception;

public class FrozenVersionException extends RuntimeException {
    public FrozenVersionException(String message) { super(message); }
}
