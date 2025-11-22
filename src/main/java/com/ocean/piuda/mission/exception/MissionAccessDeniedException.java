package com.ocean.piuda.mission.exception;

public class MissionAccessDeniedException extends RuntimeException {
    public MissionAccessDeniedException() {
        super("Access denied for this mission");
    }

    public MissionAccessDeniedException(String message) {
        super(message);
    }
}

