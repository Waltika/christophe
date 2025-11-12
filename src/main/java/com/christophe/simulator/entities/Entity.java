package com.christophe.simulator.entities;

import java.util.Map;

/**
 * Interface for entities in the simulator. All entities are dynamic, with attributes loaded from spreadsheets.
 */
public interface Entity {
    /**
     * Gets the unique ID of the entity.
     * @return the entity ID
     */
    String getId();

    /**
     * Gets the type (class) of the entity, e.g., "Cow" or "Herd".
     * @return the entity type
     */
    String getType();

    /**
     * Retrieves an attribute value by name.
     * @param name the attribute name
     * @return the attribute value, or null if not found
     */
    Object getAttribute(String name);

    /**
     * Sets an attribute value.
     * @param name the attribute name
     * @param value the value to set
     */
    void setAttribute(String name, Object value);

    /**
     * Updates an attribute based on an expression (e.g., from DerivedAttributes tab).
     * For now, stub to handle simple math; later integrate full ExpressionEvaluator.
     * @param attributeName the attribute to update
     * @param expression the expression string (e.g., "$initial_weight + 10")
     * @throws Exception if evaluation fails
     */
    void updateAttribute(String attributeName, String expression) throws Exception;

    /**
     * Gets all attributes as a map (for inspection or serialization).
     * @return unmodifiable map of attributes
     */
    Map<String, Object> getAttributes();
}