package com.christophe.simulator.loader;

import com.christophe.simulator.Simulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SheetsLoaderTest {

    @Test
    void testLoad() {
        Simulator simulator = new Simulator();
        SheetsLoader loader = new SheetsLoader();
        loader.load(simulator);
        assertNotNull(simulator.entities.get("f1"));
        assertEquals(3000.0, simulator.entities.get("f1").getAttribute("salary"));
        assertNotNull(simulator.entities.get("p1"));
        assertEquals(0.0, simulator.entities.get("p1").getAttribute("expenses"));
    }
}