package com.christophe.simulator.utils;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Evaluator for JS-like expressions using GraalJS Polyglot Context for full Java interop.
 * Supports math, logic, $vars from context, and calling public methods on bound Java objects.
 * Later extend for dates (e.g., bind java.time classes).
 */
public class ExpressionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(ExpressionEvaluator.class);
    private static final Context context;

    static {
        context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)  // Allow all Java method calls
                .allowHostClassLookup((Predicate<String>) s -> true)  // Allow Java.type() for any class
                .build();
    }

    /**
     * Evaluates an expression with variable substitution from context.
     * @param expression the JS-like string (e.g., "$a + $b * 2 > 10")
     * @param evalContext map of var names to values (without $ prefix)
     * @return evaluated result (Number, Boolean, etc.)
     * @throws RuntimeException if eval fails (e.g., syntax or reference error)
     */
    public static Object evaluate(String expression, Map<String, Object> evalContext) {
        logger.debug("Evaluating expression: '{}' with context keys: {}", expression, evalContext.keySet());
        // Strip $ from expression for JS vars
        String resolvedExpr = expression.replaceAll("\\$(\\w+)", "$1");

        // Bind each entry directly to JS global scope
        for (Map.Entry<String, Object> entry : evalContext.entrySet()) {
            context.getBindings("js").putMember(entry.getKey(), entry.getValue());
        }

        try {
            Value result = context.eval("js", resolvedExpr);
            return result.as(Object.class);  // Convert to Java type
        } catch (Exception e) {
            logger.error("Failed to evaluate expression: {}", expression, e);
            throw e;
        } finally {
            // Clean up bindings to avoid leaks
            evalContext.keySet().forEach(key -> context.getBindings("js").removeMember(key));
        }
    }
}