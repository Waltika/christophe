package com.christophe.simulator.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
