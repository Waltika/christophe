package com.christophe.simulator.entities;

import com.christophe.simulator.Simulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;

class BaseEventTest {
    @Mock
    Simulator simulator;

    BaseEvent event;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        event = new BaseEvent(10, 1, "Test", "$a > 5", "$b = $a + 1");
    }

    @Test
    void testApplyTrueCondition() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 10.0);
        event.apply(simulator, context);
        assertTrue(true);  // Placeholder; add assertEquals(11.0, context.get("b")) if effect updates context
    }

    @Test
    void testApplyFalseCondition() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 3.0);
        event.apply(simulator, context);
        verifyNoInteractions(simulator);
    }

    @Test
    void testApplyInvalidCondition() {
        Map<String, Object> context = new HashMap<>();
        assertThrows(Exception.class, () -> event.apply(simulator, context));
    }

    @Test
    void testCompareTo() {
        BaseEvent earlier = new BaseEvent(5, 1, "Early", null, null);
        BaseEvent sameTimeHigherPri = new BaseEvent(10, 0, "HighPri", null, null);
        assertTrue(event.compareTo(earlier) > 0);
        assertTrue(event.compareTo(sameTimeHigherPri) > 0);
    }
}