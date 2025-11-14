package com.christophe.simulator;

import com.christophe.simulator.entities.BaseEntity;
import com.christophe.simulator.loader.SheetsLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimulatorTest {
    private Simulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new Simulator();
    }

    @Test
    void testRunWithEvent() throws Exception {
        SheetsLoader loader = new SheetsLoader();
        loader.load(simulator);
        simulator.run(90);
        BaseEntity slaughter = simulator.entities.get("s1");
        List<String> queue = (List<String>) slaughter.getAttribute("queue");
        assertEquals(1, queue.size());  // Assuming one Cow matured
        assertEquals("c1", queue.get(0));
        BaseEntity payRoll = simulator.entities.get("p1");
        assertEquals(9000.0, payRoll.getAttribute("expenses"));
        List<Object> entries = (List<Object>) payRoll.getAttribute("entries");
        assertEquals(3, entries.size());
        assertEquals(3000.0, entries.get(0));
        assertEquals(4200.0, payRoll.getAttribute("net_profit"));  // Derived after updates
    }

    @Test
    void testDestroyEntity() {
        BaseEntity entity = new BaseEntity("e1", "Test");
        simulator.addEntity(entity);
        simulator.destroyEntity("e1");
        assertNull(simulator.entities.get("e1"));
    }

}