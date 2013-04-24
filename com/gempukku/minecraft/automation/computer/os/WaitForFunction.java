package com.gempukku.minecraft.automation.computer.os;

import com.gempukku.minecraft.automation.Automation;
import com.gempukku.minecraft.automation.computer.AbstractConditionCustomObject;
import com.gempukku.minecraft.automation.computer.MinecraftComputerExecutionContext;
import com.gempukku.minecraft.automation.computer.ServerComputerData;
import com.gempukku.minecraft.automation.lang.*;

public class WaitForFunction implements FunctionExecutable {
	@Override
	public CallContext getCallContext() {
		return new CallContext(null, false, false);
	}

	@Override
	public String[] getParameterNames() {
		return new String[]{"condition"};
	}

	@Override
	public Execution createExecution(ExecutionContext executionContext, CallContext callContext) {
		return new Execution() {
			private boolean _suspended;
			private boolean _retrievedResult;
			private ResultAwaitingCondition _condition;

			@Override
			public boolean hasNextExecution(ExecutionContext executionContext) {
				return !_retrievedResult;
			}

			@Override
			public ExecutionProgress executeNextStatement(ExecutionContext executionContext) throws ExecutionException {
				if (!_suspended) {
					final Variable conditionVar = executionContext.peekCallContext().getVariableValue("conditionVar");
					if (conditionVar.getType() != Variable.Type.CUSTOM_OBJECT || (!conditionVar.getValue().equals("CONDITION")))
						throw new ExecutionException("Expected CONDITION");

					final AbstractConditionCustomObject condition = (AbstractConditionCustomObject) conditionVar.getValue();

					final MinecraftComputerExecutionContext minecraftExecutionContext = (MinecraftComputerExecutionContext) executionContext;
					final ServerComputerData computerData = minecraftExecutionContext.getComputerData();

					_condition = condition.createAwaitingCondition();
					Automation.getServerProxy().getComputerProcessing().suspendProgramWithCondition(minecraftExecutionContext.getWorld(), computerData.getId(), _condition);
					_suspended = true;
					return new ExecutionProgress(condition.getCreationDelay());
				}
				if (!_retrievedResult) {
					executionContext.setContextValue(_condition.getReturnValue());
					_retrievedResult = true;
					return new ExecutionProgress(ExecutionTimes.SET_CONTEXT_VALUE);
				}
				return null;
			}
		};
	}
}