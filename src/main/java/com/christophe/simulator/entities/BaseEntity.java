package com.christophe.simulator.entities;

import com.christophe.simulator.utils.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.Collections;
import java.util.HashMap;
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
     *
     * @param id   unique identifier
     * @param type entity type (e.g., "Cow")
     */
    public BaseEntity(String id, String type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    // ... (keep existing imports, add import com.christophe.simulator.utils.ExpressionEvaluator;)

    @Override
    public void updateAttribute(String attributeName, String expression) throws Exception {
        try {
            Object result = ExpressionEvaluator.evaluate(expression, attributes);
            setAttribute(attributeName, result);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Invalid expression: " + expression, e);
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}