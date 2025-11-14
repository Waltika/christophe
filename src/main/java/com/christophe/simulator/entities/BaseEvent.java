package com.christophe.simulator.entities;

import com.christophe.simulator.Simulator;
import com.christophe.simulator.utils.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Base event with condition and effect expressions (from Actions tab).
 */
public class BaseEvent implements Event {
    private static final Logger logger = LoggerFactory.getLogger(BaseEvent.class);

    private final long time;
    private final int priority;
    private final String type;
    private final String conditionExpr;  // e.g., "$queue.size < $max_capacity"
    private final String effectExpr;  // e.g., "queue.add($notified_id)" - stub eval
    private final Map<String, Object> params;  // Added for event params like entity_id

    public BaseEvent(long time, int priority, String type, String conditionExpr, String effectExpr, Map<String, Object> params) {
        this.time = time;
        this.priority = priority;
        this.type = type;
        this.conditionExpr = conditionExpr;
        this.effectExpr = effectExpr;
        this.params = params != null ? new HashMap<>(params) : new HashMap<>();
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, Object> getParams() {
        return new HashMap<>(params);  // Defensive copy
    }

    @Override
    public void apply(Simulator simulator, Map<String, Object> context) throws Exception {
        logger.debug("Applying event {} with context keys: {}", type, context.keySet());
        if (conditionExpr != null) {
            logger.debug("Evaluating condition: {}", conditionExpr);
            Object cond = ExpressionEvaluator.evaluate(conditionExpr, context);
            logger.debug("Condition result: {}", cond);
            if (!(cond instanceof Boolean) || !(Boolean) cond) {
                logger.debug("Condition false for event {}", type);
                return;
            }
        }

        if (effectExpr != null) {
            logger.debug("Evaluating effect: {}", effectExpr);
            Object result = ExpressionEvaluator.evaluate(effectExpr, context);
            logger.info("Applied effect '{}' for event {}, result: {}", effectExpr, type, result);
        }

        // If notification, broadcast to entities for handleActions (moved to Simulator.run for broadcast)
    }

    @Override
    public int compareTo(Event o) {
        if (this.time != o.getTime()) {
            return Long.compare(this.time, o.getTime());
        }
        return Integer.compare(this.priority, o.getPriority());  // Lower priority first (higher priority = lower number?)
    }
}