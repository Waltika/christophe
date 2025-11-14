package com.christophe.simulator.utils;

import com.christophe.simulator.entities.BaseEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEvaluatorTest {

    @ParameterizedTest
    @CsvSource({
            "'$a + $b', 5, 3, '8.0'",
            "'$a * 2 > $b', 10, 15, 'true'",
            "'Math.max($a, $b)', 7, 12, '12.0'"
    })
    void testEvaluateSuccess(String expr, double varA, double varB, String expected) {
        Map<String, Object> context = Map.of("a", varA, "b", varB);
        Object result = ExpressionEvaluator.evaluate(expr, context);
        assertEquals(expected, result.toString());
    }

    @Test
    void testEvaluateMissingVar() {
        Map<String, Object> context = Map.of();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                ExpressionEvaluator.evaluate("$missing + 1", context)
        );
        assertTrue(exception.getMessage().contains("ReferenceError"));
    }

    @Test
    void testEvaluateInvalidExpr() {
        Map<String, Object> context = Map.of();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                ExpressionEvaluator.evaluate("1 + ", context)  // Actual syntax error for SyntaxError
        );
        assertTrue(exception.getMessage().contains("SyntaxError"));
    }

    @Test
    void testJavaInteropWithBoundObject() {
        BaseEntity entity = new BaseEntity("test", "Test");
        entity.setAttribute("value", 42.0);
        Map<String, Object> context = Map.of("attr", entity, "increment", 8.0);
        Object result = ExpressionEvaluator.evaluate("attr.setAttribute('value', attr.getAttribute('value') + increment); attr.getAttribute('value')", context);
        assertEquals(50.0, result);
        assertEquals(50.0, entity.getAttribute("value"));
    }

    @Test
    void testJavaInteropWithCustomMethods() {
        BaseEntity entity = new BaseEntity("test", "Test");
        entity.setAttribute("expenses", 0.0);
        entity.setAttribute("entries", new ArrayList<>());
        Map<String, Object> ctx = Map.of("attr", entity, "amount", 3000.0);
        Object result = ExpressionEvaluator.evaluate("attr.setAttribute('expenses', attr.getAttribute('expenses') + amount); attr.addToList('entries', amount); attr.getAttribute('expenses')", ctx);
        assertEquals(3000.0, result);
        assertEquals(3000.0, entity.getAttribute("expenses"));
        List<?> entries = (List<?>) entity.getAttribute("entries");
        assertEquals(1, entries.size());
        assertEquals(3000.0, entries.get(0));
    }
}