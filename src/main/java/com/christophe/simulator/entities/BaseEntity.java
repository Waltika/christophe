package com.christophe.simulator.entities;

import com.christophe.simulator.Simulator;
import com.christophe.simulator.utils.ExpressionEvaluator;
import org.graalvm.polyglot.HostAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of Entity. Uses a map for dynamic attributes.
 */
public class BaseEntity implements Entity {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntity.class);

    private final String id;
    private final String type;
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Constructor for BaseEntity.
     * @param id unique identifier
     * @param type entity type (e.g., "Cow")
     */
    public BaseEntity(String id, String type) {
        this.id = id;
        this.type = type;
    }

    @HostAccess.Export
    @Override
    public String getId() {
        return id;
    }

    @HostAccess.Export
    @Override
    public String getType() {
        return type;
    }

    @HostAccess.Export
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @HostAccess.Export
    public Object getAttributeOrDefault(String name, Object defaultValue) {
        return attributes.getOrDefault(name, defaultValue);
    }

    @HostAccess.Export
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @HostAccess.Export
    @Override
    public void updateAttribute(String attributeName, String expression) throws Exception {
        try {
            Object result = ExpressionEvaluator.evaluate(expression, attributes);
            setAttribute(attributeName, result);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid expression: " + expression, e);
        }
    }

    @HostAccess.Export
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @HostAccess.Export
    public void setAttributes(Map<String, Object> newAttributes) {
        attributes.clear();
        attributes.putAll(newAttributes);
    }

    @HostAccess.Export
    @SuppressWarnings("unchecked")
    public void addToList(String key, Object item) {
        attributes.compute(key, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            ((List<Object>) v).add(item);
            return v;
        });
    }

    @HostAccess.Export
    public void updateDerivedAttributes(Map<String, Object> globalContext) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> derived = (Map<String, String>) getAttribute("derived_attributes");
        if (derived == null) {
            return;
        }
        Map<String, Object> evalContext = new HashMap<>(globalContext);
        evalContext.putAll(getAttributes());  // Entity attrs override globals if same key
        for (Map.Entry<String, String> entry : derived.entrySet()) {
            String attrName = entry.getKey();
            String expr = entry.getValue();
            logger.debug("Updating derived attr {} with expr: {}", attrName, expr);
            Object result = ExpressionEvaluator.evaluate(expr, evalContext);
            setAttribute(attrName, result);
        }
    }

    @HostAccess.Export
    public void updateStates(Map<String, Object> globalContext, Simulator simulator) throws Exception {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> states = (List<Map<String, Object>>) getAttribute("states");
        if (states == null) {
            return;
        }
        String currentState = (String) getAttribute("current_state");
        if (currentState == null) {
            // Set initial if not set
            for (Map<String, Object> state : states) {
                if (Boolean.TRUE.equals(state.get("initial"))) {
                    currentState = (String) state.get("stateName");
                    setAttribute("current_state", currentState);
                    String notification = (String) state.get("notificationEvent");
                    if (notification != null) {
                        enqueueNotification(notification, simulator);
                    }
                    break;
                }
            }
            return;
        }

        // Find current state map
        Map<String, Object> currentStateMap = null;
        for (Map<String, Object> state : states) {
            if (currentState.equals(state.get("stateName"))) {
                currentStateMap = state;
                break;
            }
        }
        if (currentStateMap == null) {
            return;
        }

        String nextStates = (String) currentStateMap.get("nextStates");
        String conditionExpr = (String) currentStateMap.get("conditionToNext");
        if (nextStates == null || conditionExpr == null) {
            return;  // No transition
        }

        // Eval condition (use globals + entity attrs + current_tick)
        Map<String, Object> evalContext = new HashMap<>(globalContext);
        evalContext.putAll(getAttributes());
        Object cond = ExpressionEvaluator.evaluate(conditionExpr, evalContext);
        if (Boolean.TRUE.equals(cond)) {
            // Transition (assume single next for now; split if comma-separated later)
            setAttribute("current_state", nextStates);
            String notification = (String) currentStateMap.get("notificationEvent");
            if (notification != null) {
                enqueueNotification(notification, simulator);
            }
            logger.debug("Transitioned {} from {} to {}", getId(), currentState, nextStates);
        }
    }

    private void enqueueNotification(String eventType, Simulator simulator) {
        Map<String, Object> params = new HashMap<>();
        params.put("entity_id", getId());  // For notified_entity_id
        BaseEvent notificationEvent = new BaseEvent(simulator.getCurrentTick(), 1, eventType, null, null, params);
        simulator.enqueueEvent(notificationEvent);
    }

    @HostAccess.Export
    public void handleActions(Event event, Simulator simulator) throws Exception {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) getAttribute("actions");
        if (actions == null) {
            return;
        }
        for (Map<String, Object> action : actions) {
            String trigger = (String) action.get("trigger");
            if (!event.getType().equals(trigger)) {
                continue;
            }
            String conditionExpr = (String) action.get("condition");
            if (conditionExpr != null) {
                Map<String, Object> evalContext = new HashMap<>(simulator.globals);
                evalContext.put("current_tick", simulator.getCurrentTick());
                evalContext.putAll(getAttributes());  // Entity attrs
                evalContext.put("notified_entity_id", event.getParams().get("entity_id"));
                // Add more event params as needed
                Object cond = ExpressionEvaluator.evaluate(conditionExpr, evalContext);
                if (!(cond instanceof Boolean) || !(Boolean) cond) {
                    continue;
                }
            }
            String effectExpr = (String) action.get("effect");
            if (effectExpr != null) {
                Map<String, Object> evalContext = new HashMap<>(simulator.globals);
                evalContext.put("current_tick", simulator.getCurrentTick());
                evalContext.putAll(getAttributes());
                evalContext.put("notified_entity_id", event.getParams().get("entity_id"));
                evalContext.put("attr", this);  // Bind attr to self for effect
                ExpressionEvaluator.evaluate(effectExpr, evalContext);
            }
            // Stub NewEntityType/MapAttributes/RemoveSource (impl createNewEntity/destroy later)
            logger.debug("Handled action {} for event {}", action.get("actionName"), event.getType());
        }
    }
}