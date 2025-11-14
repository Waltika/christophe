package com.christophe.simulator.entities;

import com.christophe.simulator.Simulator;

import java.util.Map;

/**
 * Interface for events in the simulator.
 */
public interface Event extends Comparable<Event> {
    long getTime();

    int getPriority();

    String getType();

    void apply(Simulator simulator, Map<String, Object> context) throws Exception;

    Map<String, Object> getParams();
}