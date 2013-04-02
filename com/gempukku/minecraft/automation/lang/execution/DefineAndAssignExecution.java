package com.gempukku.minecraft.automation.lang.execution;

import com.gempukku.minecraft.automation.lang.*;

public class DefineAndAssignExecution implements Execution {
    private String _name;
    private ExecutableStatement _value;

    private boolean _defined;
    private boolean _stackedValue;
    private boolean _assignedValue;

    public DefineAndAssignExecution(String name, ExecutableStatement value) {
        _name = name;
        _value = value;
    }

    @Override
    public boolean hasNextExecution(ExecutionContext executionContext) {
        if (!_defined)
            return true;
        if (!_stackedValue)
            return true;
        if (!_assignedValue)
            return true;
        return false;
    }

    @Override
    public ExecutionProgress executeNextStatement(ExecutionContext executionContext) throws ExecutionException {
        if (!_defined) {
            executionContext.peekCallContext().defineVariable(_name);
            _defined = true;
            return new ExecutionProgress(100);
        }
        if (!_stackedValue) {
            executionContext.stackExecution(_value.createExecution());
            _stackedValue = true;
            return new ExecutionProgress(100);
        }
        if (!_assignedValue) {
            executionContext.peekCallContext().setVariableValue(_name, executionContext.getContextValue().getValue());
            _assignedValue = true;
            return new ExecutionProgress(100);
        }
        return null;
    }
}