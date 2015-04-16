package com.gempukku.lang;

import com.gempukku.lang.parser.ScriptParser;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ProgramTest {
    @Test
    public void helloWorld() {
        assertEquals("Hello world", executeUntilCompletionNoException("return \"Hello world\";"));
    }

    @Test
    public void adding() {
        assertEquals(5f, executeUntilCompletionNoException("return 2+3;"));
    }

    @Test
    public void operatorPrecedence() {
        assertEquals(8f, executeUntilCompletionNoException("return 2+3*2;"));
    }

    @Test
    public void numberToString() {
        assertEquals("a=3.0", executeUntilCompletionNoException("return \"a=\"+3;"));
    }

    @Test
    public void function() {
        assertEquals("a", executeUntilCompletionNoException("function echo(toReturn) { return toReturn; } return echo(\"a\");"));
    }

    @Test
    public void functionCalledTwice() {
        assertEquals("a=3.0", executeUntilCompletionNoException("function echo(toReturn) { return toReturn; } return echo(\"a=\")+echo(3);"));
    }

    @Test
    public void comparison() {
        assertFalse((Boolean) executeUntilCompletionNoException("return 123<122.5;"));
    }

    private Object executeUntilCompletionNoException(String script) {
        try {
            ScriptParser parser = new ScriptParser();
            ScriptExecutable exec = parser.parseScript(new StringReader(script));

            CallContext context = new CallContext(null, false, true);
            ExecutionContext executionContext = new ExecutionContext(new TestExecutionCostConfiguration());
            executionContext.stackExecutionGroup(context, exec.createExecution(context));

            while (!executionContext.isFinished())
                executionContext.executeNext();

            return executionContext.getContextValue().getValue();
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }
}
