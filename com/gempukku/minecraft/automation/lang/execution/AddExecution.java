package com.gempukku.minecraft.automation.lang.execution;

import com.gempukku.minecraft.automation.lang.*;

public class AddExecution implements Execution {
    private ExecutableStatement _left;
    private ExecutableStatement _right;

    private boolean _stackedLeft;
    private boolean _resolvedLeft;
    private boolean _stackedRight;
    private boolean _resolvedAndAssignedSum;

    private Variable _leftValue;

    public AddExecution(ExecutableStatement left, ExecutableStatement right) {
        _left = left;
        _right = right;
    }

    @Override
    public boolean hasNextExecution(ExecutionContext executionContext) {
        if (!_stackedLeft)
            return true;
        if (!_resolvedLeft)
            return true;
        if (!_stackedRight)
            return true;
        if (!_resolvedAndAssignedSum)
            return true;
        return false;
    }

    @Override
    public ExecutionProgress executeNextStatement(ExecutionContext executionContext) throws ExecutionException {
        if (!_stackedLeft) {
            executionContext.stackExecution(_left.createExecution());
            _stackedLeft = true;
            return new ExecutionProgress(100);
        }
        if (!_resolvedLeft) {
            _leftValue = executionContext.getContextValue();
            _resolvedLeft = true;
            return new ExecutionProgress(100);
        }
        if (!_stackedRight) {
            executionContext.stackExecution(_right.createExecution());
            _stackedRight = true;
            return new ExecutionProgress(100);
        }
        if (!_resolvedAndAssignedSum) {
            Variable rightValue = executionContext.getContextValue();
            if (_leftValue.getType() == Variable.Type.STRING) {
                executionContext.setContextValue(new Variable(convertToString(_leftValue) + convertToString(rightValue)));
            } else if (rightValue.getType() == Variable.Type.NUMBER && _leftValue.getType() == Variable.Type.NUMBER) {
                executionContext.setContextValue(new Variable(((Number) _leftValue.getValue()).floatValue() +
                        ((Number) rightValue.getValue()).floatValue()));
            } else {
                throw new ExecutionException("Unable to add two values of types " + _leftValue.getType() + " and " + rightValue.getType());
            }
            _resolvedAndAssignedSum = true;
            return new ExecutionProgress(100);
        }
        return null;
    }

    private String convertToString(Variable variable) {
        if (variable.getType() == Variable.Type.STRING)
            return (String) variable.getValue();
        else if (variable.getType() == Variable.Type.NUMBER)
            return String.valueOf(((Number) variable.getValue()).floatValue());
        else if (variable.getType() == Variable.Type.NULL)
            return "null";
        else if (variable.getType() == Variable.Type.BOOLEAN)
            return ((Boolean) variable.getValue()) ? "true" : "false";
        else
            return "";
    }
}