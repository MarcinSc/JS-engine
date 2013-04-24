package com.gempukku.minecraft.automation.program;

public class SuspendedProgram {
	private RunningProgram _runningProgram;
	private AwaitingCondition _awaitingCondition;

	public SuspendedProgram(RunningProgram runningProgram, AwaitingCondition awaitingCondition) {
		_runningProgram = runningProgram;
		_awaitingCondition = awaitingCondition;
	}

	public AwaitingCondition getAwaitingCondition() {
		return _awaitingCondition;
	}

	public RunningProgram getRunningProgram() {
		return _runningProgram;
	}
}