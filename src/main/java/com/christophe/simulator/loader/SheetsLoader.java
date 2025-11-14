package com.christophe.simulator.loader;

import com.christophe.simulator.Simulator;
import com.christophe.simulator.entities.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stub loader for v0.1 (hardcoded data; later use Google Drive/Sheets API).
 * Loads globals, entities with attrs/derived/states/actions.
 */
public class SheetsLoader {
    private static final Logger logger = LoggerFactory.getLogger(SheetsLoader.class);

    public void load(Simulator simulator) {
        logger.info("Stubbing entity loading...");

        // Stub Globals from "Globals.xlsx"
        Map<String, Object> globals = new HashMap<>();
        globals.put("tax_rate", 0.2);
        simulator.globals.putAll(globals);
        logger.info("Loaded globals: {}", globals);

        // Stub Farmer entity (Inputs: salary=3000, cycle_length=30; Actions: salary_due if (current_tick - start) % cycle_length == 0, enqueue "Farmer.SalaryDue" with amount=$salary
        BaseEntity farmer = new BaseEntity("f1", "Farmer");
        Map<String, Object> farmerAttrs = new HashMap<>();
        farmerAttrs.put("salary", 3000.0);
        farmerAttrs.put("cycle_length", 30L);
        farmerAttrs.put("start_tick", 0L);
        farmerAttrs.put("last_triggered", -30L);  // Initial to allow first cycle
        farmer.setAttributes(farmerAttrs);
        simulator.addEntity(farmer);

        // Stub PayRoll entity (react to "Farmer.SalaryDue": expenses += $amount, add to entries list
        BaseEntity payRoll = new BaseEntity("p1", "PayRoll");
        Map<String, Object> payRollAttrs = new HashMap<>();
        payRollAttrs.put("expenses", 0.0);
        payRollAttrs.put("entries", new ArrayList<Double>());
        payRollAttrs.put("revenue", 15000.0);  // Stub input for derived
        // Stub DerivedAttributes tab: Map of attrName to expr
        Map<String, String> payRollDerived = new HashMap<>();
        payRollDerived.put("net_profit", "$revenue - $expenses * (1 + $tax_rate)");
        payRollAttrs.put("derived_attributes", payRollDerived);
        logger.info("Loaded PayRoll with expenses: {}, entries: {}, derived: {}", payRollAttrs.get("expenses"), payRollAttrs.get("entries"), payRollDerived);
        payRoll.setAttributes(payRollAttrs);
        simulator.addEntity(payRoll);

        // Stub Cow entity (Inputs: initial_weight=100, growth_rate=10, age=0; Derived: age = $current_tick / 30, weight = $initial_weight + $growth_rate * $age; States: Calf (initial), Mature (condition $age > 2 for test, notify "Cow.Maturity")
        BaseEntity cow = new BaseEntity("c1", "Cow");
        Map<String, Object> cowAttrs = new HashMap<>();
        cowAttrs.put("initial_weight", 100.0);
        cowAttrs.put("growth_rate", 10.0);
        cowAttrs.put("age", 0.0);  // Derived, but stub initial
        // Stub DerivedAttributes tab: Map of attrName to expr
        Map<String, String> cowDerived = new HashMap<>();
        cowDerived.put("age", "$current_tick / 30");  // Months approx
        cowDerived.put("weight", "$initial_weight + $growth_rate * $age");
        cowAttrs.put("derived_attributes", cowDerived);

        // Stub States tab as List<Map>
        List<Map<String, Object>> states = new ArrayList<>();
        Map<String, Object> calfState = new HashMap<>();
        calfState.put("stateName", "Calf");
        calfState.put("initial", true);
        calfState.put("nextStates", "Mature");
        calfState.put("conditionToNext", "$age > 2");  // Lowered for test (age=3 at tick 90)
        calfState.put("notificationEvent", null);
        states.add(calfState);

        Map<String, Object> matureState = new HashMap<>();
        matureState.put("stateName", "Mature");
        matureState.put("initial", false);
        matureState.put("nextStates", null);  // End state
        matureState.put("conditionToNext", null);
        matureState.put("notificationEvent", "Cow.Maturity");  // On entry
        states.add(matureState);

        cowAttrs.put("states", states);
        cowAttrs.put("current_state", "Calf");  // Initial based on tab
        logger.info("Loaded Cow with states: {}", states);
        cow.setAttributes(cowAttrs);
        simulator.addEntity(cow);

        // Stub Slaughterhouse entity (Inputs: max_capacity=10; Actions: on "Cow.Maturity", condition "attr.getAttribute('queue').size() < $max_capacity", effect "attr.addToList('queue', $notified_entity_id)"
        BaseEntity slaughterhouse = new BaseEntity("s1", "Slaughterhouse");
        Map<String, Object> slaughterAttrs = new HashMap<>();
        slaughterAttrs.put("max_capacity", 10L);
        slaughterAttrs.put("queue", new ArrayList<String>());  // List of cow IDs

        // Stub Actions tab as List<Map>
        List<Map<String, Object>> actions = new ArrayList<>();
        Map<String, Object> maturityAction = new HashMap<>();
        maturityAction.put("actionName", "QueueMatureCow");
        maturityAction.put("trigger", "Cow.Maturity");
        maturityAction.put("condition", "attr.getAttribute('queue').size() < $max_capacity");  // JS expr with size()
        maturityAction.put("effect", "attr.addToList('queue', $notified_entity_id)");  // JS calling addToList
        maturityAction.put("newEntityType", null);  // No creation
        maturityAction.put("mapAttributes", null);  // No mapping
        maturityAction.put("removeSource", false);  // No destroy
        actions.add(maturityAction);

        // Stub processing action (generic via tick trigger)
        Map<String, Object> processAction = new HashMap<>();
        processAction.put("actionName", "ProcessQueue");
        processAction.put("trigger", "tick");
        processAction.put("condition", "attr.getAttribute('queue').size() > 0");
        processAction.put("effect", "var id = attr.getAttribute('queue').remove(0); var weight = simulator.entities.get(id).getAttribute('weight'); simulator.destroyEntity(id); var carcass = simulator.createNewEntity('Carcass', id + '_carcass'); carcass.setAttribute('weight', weight)");
        processAction.put("cycle_length", 100L);  // High for test to not process in 90 ticks
        actions.add(processAction);

        slaughterAttrs.put("actions", actions);
        logger.info("Loaded Slaughterhouse with actions: {}", actions);
        slaughterhouse.setAttributes(slaughterAttrs);
        simulator.addEntity(slaughterhouse);

        // Stub initial event or smart check for cycles
        logger.info("Loaded entities: Farmer, PayRoll, Cow, Slaughterhouse");
    }
}