package com.christophe.simulator.entities;

import com.christophe.simulator.utils.ExpressionEvaluator;
import com.christophe.simulator.Simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public BaseEvent(long time, int priority, String type, String conditionExpr, String effectExpr) {
        this.time = time;
        this.priority = priority;
        this.type = type;
        this.conditionExpr = conditionExpr;
        this.effectExpr = effectExpr;
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
    public void apply(Simulator simulator, Map<String, Object> context) throws Exception {
        if (conditionExpr != null) {
            Object cond = ExpressionEvaluator.evaluate(conditionExpr, context);
            if (!(cond instanceof Boolean) || ! (Boolean) cond) {
                logger.debug("Condition '{}' false for event {}", conditionExpr, type);
                return;
            }
        }

        if (effectExpr != null) {
            if (effectExpr.contains("=")) {
                String[] parts = effectExpr.split("=", 2);  // Split once
                String attr = parts[0].trim().replace("$", "");
                String right = parts[1].trim();
                Object value = ExpressionEvaluator.evaluate(right, context);
                BaseEntity notified = (BaseEntity) context.get("notified_entity");
                if (notified != null) {
                    notified.setAttribute(attr, value);
                    logger.info("Set attr '{}' to {} on entity {}", attr, value, notified.getId());
                } else {
                    logger.warn("No notified entity for assignment in effect '{}'", effectExpr);
                }
            } else {
                Object result = ExpressionEvaluator.evaluate(effectExpr, context);
                logger.info("Applied non-assignment effect '{}' for event {}, result: {}", effectExpr, type, result);
                // Future: if (result instanceof Event) simulator.enqueueEvent((Event) result);
            }
        }
    }
}