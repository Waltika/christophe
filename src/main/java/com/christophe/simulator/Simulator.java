package com.christophe.simulator;

import com.christophe.simulator.entities.BaseEntity;
import com.christophe.simulator.entities.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Simulator {
    private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

    private final PriorityQueue<Event> eventQueue = new PriorityQueue<>();
    final Map<String, BaseEntity> entities = new HashMap<>();  // ID to entity
    private long currentTick = 0;

    // Stub globals (from Globals.xlsx later)
    private final Map<String, Object> globals = new HashMap<>();

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

    public void run(long maxTicks) {
        while (!eventQueue.isEmpty() && currentTick <= maxTicks) {
            Event e = eventQueue.poll();
            if (e.getTime() > currentTick) {
                currentTick = e.getTime();
            }
            Map<String, Object> context = new HashMap<>(globals);
            context.put("current_tick", currentTick);
            // Stub notified entity (from event payload; future from Actions)
            BaseEntity notified = entities.get("e1");  // Stub for test; replace with event.getNotifiedId()
            if (notified != null) {
                context.putAll(notified.getAttributes());  // Merge attrs for $vars
                context.put("notified_entity", notified);
            }
            try {
                e.apply(this, context);
            } catch (Exception ex) {
                logger.error("Error applying event {}", e.getType(), ex);
            }
            checkSmartEvents();
        }
    }

    private void checkSmartEvents() {
        // Stub: for each entity, check Actions for cycles (e.g., if (currentTick - start) % cycle_length == 0, enqueue)
    }
}