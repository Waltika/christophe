package com.christophe.simulator;

import com.christophe.simulator.entities.BaseEntity;
import com.christophe.simulator.entities.BaseEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorTest {
    private Simulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new Simulator();
    }

    @Test
    void testRunWithEvent() throws Exception {
        BaseEntity entity = new BaseEntity("e1", "Test");
        simulator.addEntity(entity);
        entity.setAttribute("a", 10.0);
        BaseEvent event = new BaseEvent(1, 1, "TestEvent", "$a > 5", "$weight = $a * 2");
        simulator.enqueueEvent(event);
        simulator.run(10);
        assertEquals(20.0, entity.getAttribute("weight"));
    }

    @Test
    void testDestroyEntity() {
        BaseEntity entity = new BaseEntity("e1", "Test");
        simulator.addEntity(entity);
        simulator.destroyEntity("e1");
        assertNull(simulator.entities.get("e1"));
    }
}