package com.christophe.simulator.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEvaluatorTest {

    @ParameterizedTest
    @CsvSource({
            "'$a + $b', 5, 3, '8.0'",
            "'$a * 2 > $b', 10, 15, 'true'",
            "'Math.max($a, $b)', 7, 12, '12.0'"
    })
    void testEvaluateSuccess(String expr, double varA, double varB, String expected) throws ScriptException {
        Map<String, Object> context = new HashMap<>();
        context.put("a", varA);
        context.put("b", varB);
        Object result = ExpressionEvaluator.evaluate(expr, context);
        assertEquals(expected, result.toString());
    }

    @Test
    void testEvaluateMissingVar() {
        Map<String, Object> context = new HashMap<>();
        ScriptException exception = assertThrows(ScriptException.class, () -> {
            ExpressionEvaluator.evaluate("$missing + 1", context);
        });
        assertTrue(exception.getMessage().contains("ReferenceError"));
    }

    @Test
    void testEvaluateInvalidExpr() {
        Map<String, Object> context = new HashMap<>();
        ScriptException exception = assertThrows(ScriptException.class, () -> {
            ExpressionEvaluator.evaluate("1 + ", context);  // Actual syntax error for SyntaxError
        });
        assertTrue(exception.getMessage().contains("SyntaxError"));
    }
}