package com.christophe.simulator.entities;

import com.christophe.simulator.Simulator;

import java.util.Map;

/**
 * Interface for events (notifications, reactions). Comparable for PriorityQueue sorting.
 */
public interface Event extends Comparable<Event> {
    /**
     * Gets the scheduled time (tick).
     * @return time
     */
    long getTime();

    /**
     * Gets the priority (lower = higher priority for same time).
     * @return priority
     */
    int getPriority();

    /**
     * Gets the event type (e.g., "Cow.Maturity").
     * @return type
     */
    String getType();

    /**
     * Applies the event if condition true, using context (attrs/globals/notified).
     * @param simulator for enqueue, destroy, getEntities
     * @param context for expr eval
     * @throws Exception on eval failure
     */
    void apply(Simulator simulator, Map<String, Object> context) throws Exception;

    @Override
    default int compareTo(Event o) {
        int timeCmp = Long.compare(getTime(), o.getTime());
        return timeCmp != 0 ? timeCmp : Integer.compare(getPriority(), o.getPriority());
    }
}