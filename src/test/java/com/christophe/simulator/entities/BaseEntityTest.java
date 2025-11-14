package com.christophe.simulator.entities;

import com.christophe.simulator.Simulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {
    private BaseEntity entity;

    @BeforeEach
    void setUp() {
        entity = new BaseEntity("entity-1", "TestType");
    }

    @Test
    void testGetIdAndType() {
        assertEquals("entity-1", entity.getId());
        assertEquals("TestType", entity.getType());
    }

    @Test
    void testSetAndGetAttribute() {
        entity.setAttribute("initial_weight", 100.0);
        assertEquals(100.0, entity.getAttribute("initial_weight"));
        assertNull(entity.getAttribute("non_existent"));
    }

    @Test
    void testUpdateAttributeSimpleExpr() throws Exception {
        entity.setAttribute("initial_weight", 100.0);
        entity.setAttribute("growth", 10.0);
        entity.updateAttribute("weight", "$initial_weight + $growth * 2");
        assertEquals(120.0, entity.getAttribute("weight"));  // Double
    }

    @Test
    void testUpdateAttributeMissingVar() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            entity.updateAttribute("weight", "$missing + 5");
        });
        assertTrue(exception.getMessage().contains("Invalid expression"));
        assertTrue(exception.getCause().getMessage().contains("ReferenceError"));
    }

    @Test
    void testUpdateAttributeInvalidExpr() {
        entity.setAttribute("a", 1.0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            entity.updateAttribute("b", "$a + invalid_syntax");
        });
        assertTrue(exception.getMessage().contains("Invalid expression"));
        assertTrue(exception.getCause().getMessage().contains("ReferenceError"));
    }

    @Test
    void testGetAttributesUnmodifiable() {
        entity.setAttribute("key", "value");
        Map<String, Object> attrs = entity.getAttributes();
        assertEquals("value", attrs.get("key"));
        assertThrows(UnsupportedOperationException.class, () -> attrs.put("new", "val"));
    }

    @Test
    void testHandleActions() throws Exception {
        Simulator sim = new Simulator();
        BaseEntity slaughter = new BaseEntity("s1", "Slaughterhouse");
        slaughter.setAttribute("max_capacity", 10L);
        slaughter.setAttribute("queue", new ArrayList<String>());
        List<Map<String, Object>> actions = new ArrayList<>();
        Map<String, Object> action = new HashMap<>();
        action.put("trigger", "Cow.Maturity");
        action.put("condition", "queue.size < $max_capacity");
        action.put("effect", "attr.addToList('queue', $notified_entity_id)");
        actions.add(action);
        slaughter.setAttribute("actions", actions);
        Map<String, Object> params = Map.of("entity_id", "c1");
        Event maturityEvent = new BaseEvent(10, 1, "Cow.Maturity", null, null, params);
        slaughter.handleActions(maturityEvent, sim);
        List<String> queue = (List<String>) slaughter.getAttribute("queue");
        assertEquals(1, queue.size());
        assertEquals("c1", queue.get(0));
    }
}
