package com.christophe.simulator.entities;

import com.christophe.simulator.Simulator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito .*;

class BaseEventTest {

    @Test
    void testApplyWithConditionTrue() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("value", 10.0);
        BaseEvent event = new BaseEvent(0, 0, "Test", "$value > 5", "value + 1", new HashMap<>());
        Simulator sim = mock(Simulator.class);
        event.apply(sim, context);
        // Add asserts if needed (e.g., for side effects)
    }

    @Test
    void testApplyWithConditionFalse() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("value", 3.0);
        BaseEvent event = new BaseEvent(0, 0, "Test", "$value > 5", "value + 1", new HashMap<>());
        Simulator sim = mock(Simulator.class);
        event.apply(sim, context);
        // Add asserts if needed
    }

    @Test
    void testApplyWithEffect() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("attr", mock(BaseEntity.class));
        context.put("amount", 5.0);
        BaseEvent event = new BaseEvent(0, 0, "Test", null, "attr.setAttribute('value', amount)", new HashMap<>());
        Simulator sim = mock(Simulator.class);
        event.apply(sim, context);
        // Verify attr.setAttribute called via mock if needed
    }

    @Test
    void testCompareTo() {
        BaseEvent event1 = new BaseEvent(10, 2, "Type1", null, null, new HashMap<>());
        BaseEvent event2 = new BaseEvent(10, 1, "Type2", null, null, new HashMap<>());
        BaseEvent event3 = new BaseEvent(5, 3, "Type3", null, null, new HashMap<>());
        assertTrue(event3.compareTo(event1) < 0);  // Earlier time
        assertTrue(event2.compareTo(event1) < 0);  // Same time, lower priority
        assertTrue(event1.compareTo(event2) > 0);
    }
}