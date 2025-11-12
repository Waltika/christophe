package com.christophe.simulator.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * Evaluator for JS-like expressions using GraalJS ScriptEngine. Supports math, logic, $vars from context.
 * Later extend for dates (e.g., bind java.time classes).
 */
public class ExpressionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(ExpressionEvaluator.class);
    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");

    /**
     * Evaluates an expression with variable substitution from context.
     * @param expression the JS-like string (e.g., "$a + $b * 2 > 10")
     * @param context map of var names to values (without $ prefix)
     * @return evaluated result (Number, Boolean, etc.)
     * @throws ScriptException if eval fails (e.g., syntax or reference error)
     */
    public static Object evaluate(String expression, Map<String, Object> context) throws ScriptException {
        if (engine == null) {
            throw new ScriptException("No GraalJS engine available - ensure dependencies are added");
        }

        // Strip $ from expression for JS vars
        String resolvedExpr = expression.replaceAll("\\$(\\w+)", "$1");

        // Bind context to engine scope
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }

        try {
            Object result = engine.eval(resolvedExpr);
            logger.debug("Evaluated '{}' to {}", expression, result);
            return result;
        } catch (ScriptException e) {
            logger.error("Failed to evaluate expression: {}", expression, e);
            throw e;
        } finally {
            bindings.clear();  // Clean up to avoid leaks
        }
    }
}