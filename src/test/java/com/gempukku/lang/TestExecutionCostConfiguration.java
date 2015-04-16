package com.gempukku.lang;

public class TestExecutionCostConfiguration implements ExecutionCostConfiguration {
    @Override
    public int getGetContextValue() {
        return 1;
    }

    @Override
    public int getSetContextValue() {
        return 1;
    }

    @Override
    public int getGetReturnValue() {
        return 1;
    }

    @Override
    public int getSetReturnValue() {
        return 1;
    }

    @Override
    public int getBreakBlock() {
        return 2;
    }

    @Override
    public int getDefineVariable() {
        return 2;
    }

    @Override
    public int getSetVariable() {
        return 2;
    }

    @Override
    public int getStackExecution() {
        return 5;
    }

    @Override
    public int getStackGroupExecution() {
        return 8;
    }

    @Override
    public int getSumValues() {
        return 10;
    }

    @Override
    public int getOtherMathOperation() {
        return 10;
    }

    @Override
    public int getCompareValues() {
        return 10;
    }

    @Override
    public int getResolveMember() {
        return 10;
    }
}
