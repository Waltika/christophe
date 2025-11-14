package com.christophe.simulator;

import com.christophe.simulator.entities.BaseEntity;
import com.christophe.simulator.entities.BaseEvent;
import com.christophe.simulator.entities.Event;
import com.christophe.simulator.utils.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Simulator {
    private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

    private final PriorityQueue<Event> eventQueue = new PriorityQueue<>();
    public final Map<String, BaseEntity> entities = new HashMap<>(); // ID to entity
    private long currentTick = 0;

    // Stub globals (from Globals.xlsx later)
    public final Map<String, Object> globals = new HashMap<>();

    public void addEntity(BaseEntity entity) {
        entities.put(entity.getId(), entity);
    }

    public void enqueueEvent(Event event) {
        eventQueue.add(event);
        logger.debug("Enqueued event {} at tick {}", event.getType(), event.getTime());
    }

    public void destroyEntity(String id) {
        BaseEntity entity = entities.remove(id);
        if (entity != null) {
            logger.info("Destroyed entity {}", id);
            // Stub: handle outputs (from CaptureDeletion tab)
        }
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public void run(long maxTicks) {
        logger.info("Starting simulation up to tick: {}", maxTicks);
        int loopCount = 0; // Safety to detect infinite loop
        while (currentTick <= maxTicks) {
            logger.debug("Entering tick {} (loop iteration {}) with queue size {}", currentTick, loopCount++, eventQueue.size());

            // Check smart events first, once per tick (adds events at current or future ticks)
            checkSmartEvents();

            boolean hasMoreToProcess;
            do {
                hasMoreToProcess = false;

                // Process all events at or before currentTick
                while (!eventQueue.isEmpty() && eventQueue.peek().getTime() <= currentTick) {
                    Event e = eventQueue.poll();
                    logger.debug("Polled event {} at tick {}", e.getType(), e.getTime());
                    Map<String, Object> context = buildContext(e);
                    try {
                        e.apply(this, context);
                    } catch (Exception ex) {
                        logger.error("Error applying event {}", e.getType(), ex);
                    }
                    hasMoreToProcess = true; // Re-loop if applied (may have added more at same tick)
                }
            } while (hasMoreToProcess);

            // Create tick-specific context (globals + current_tick)
            Map<String, Object> tickContext = new HashMap<>(globals);
            tickContext.put("current_tick", getCurrentTick());

            // Update derived attributes after all processing in the tick
            for (BaseEntity entity : entities.values()) {
                try {
                    entity.updateDerivedAttributes(tickContext);
                } catch (Exception ex) {
                    logger.error("Error updating derived attrs for {}", entity.getId(), ex);
                }
            }

            // Update states after derived (transitions may depend on updated attrs)
            for (BaseEntity entity : entities.values()) {
                try {
                    entity.updateStates(tickContext, this);
                } catch (Exception ex) {
                    logger.error("Error updating states for {}", entity.getId(), ex);
                }
            }

            if (loopCount > 1000) {
                throw new RuntimeException("Possible infinite loop detected in run() at tick " + currentTick);
            }

            // Advance to next tick: min of next event time or +1, capped at maxTicks + 1
            if (eventQueue.isEmpty()) {
                currentTick++;
            } else {
                long nextEventTime = eventQueue.peek().getTime();
                currentTick = Math.min(nextEventTime, maxTicks + 1);
            }
            logger.debug("Advanced to tick {}", currentTick);
        }
        logger.info("Simulation completed at tick: {}", currentTick);
    }

    private Map<String, Object> buildContext(Event event) {
        Map<String, Object> context = new HashMap<>(globals);
        context.put("current_tick", getCurrentTick());

        // bind every entity by its type (lower-case) so JS can use "payroll", "farmer", …
        for (BaseEntity ent : entities.values()) {
            context.put(ent.getType().toLowerCase(), ent);
        }

        // for salary events we also need the amount and bind attr to payroll
        if ("Farmer.SalaryDue".equals(event.getType())) {
            BaseEntity farmer = entities.values().stream()
                    .filter(e -> "Farmer".equalsIgnoreCase(e.getType()))
                    .findFirst().orElse(null);
            BaseEntity payroll = entities.values().stream()
                    .filter(e -> "PayRoll".equalsIgnoreCase(e.getType()))
                    .findFirst().orElse(null);
            if (farmer != null && payroll != null) {
                context.put("amount", farmer.getAttribute("salary"));
                context.put("attr", payroll); // Bind attr to PayRoll entity
            }
        }

        logger.debug("Context keys for {}: {}", event.getType(), context.keySet());
        return context;
    }

    private boolean checkSmartEvents() {
        logger.debug("Checking smart events at tick {}", currentTick);
        boolean addedAny = false;
        Map<String, Object> tickContext = new HashMap<>(globals);
        tickContext.put("current_tick", currentTick);
        for (BaseEntity entity : entities.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actions = (List<Map<String, Object>>) entity.getAttribute("actions");
            if (actions == null) {
                continue;
            }
            for (Map<String, Object> action : actions) {
                String trigger = (String) action.get("trigger");
                if (!"tick".equals(trigger)) {
                    continue;
                }
                String conditionExpr = (String) action.get("condition");
                if (conditionExpr != null) {
                    Map<String, Object> evalContext = new HashMap<>(tickContext);
                    evalContext.putAll(entity.getAttributes());
                    evalContext.put("attr", entity);  // Bind for attr.getAttribute calls
                    Object cond = ExpressionEvaluator.evaluate(conditionExpr, evalContext);
                    if (!(cond instanceof Boolean) || !(Boolean) cond) {
                        continue;
                    }
                }
                String effectExpr = (String) action.get("effect");
                if (effectExpr != null) {
                    Map<String, Object> evalContext = new HashMap<>(tickContext);
                    evalContext.putAll(entity.getAttributes());
                    evalContext.put("attr", entity);
                    evalContext.put("simulator", this);  // Bind for enqueueEvent calls in JS
                    ExpressionEvaluator.evaluate(effectExpr, evalContext);
                    addedAny = true;
                }
                // Update last_triggered if action has cycle_length
                Long cycleLength = (Long) action.get("cycle_length");
                if (cycleLength != null) {
                    entity.setAttribute("last_triggered", currentTick);
                }
            }
        }
        return addedAny;
    }
}