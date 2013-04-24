package com.gempukku.minecraft.automation.computer.os;

import com.gempukku.minecraft.automation.lang.Variable;

import java.util.ArrayList;
import java.util.List;

public class AnyResultAwaitingCondition implements ResultAwaitingCondition {
	private List<ResultAwaitingCondition> _awaitingConditions;
	private int _metConditionIndex = -1;

	public AnyResultAwaitingCondition(List<ResultAwaitingCondition> awaitingConditions) {
		_awaitingConditions = awaitingConditions;
	}

	@Override
	public boolean isMet() {
		if (_metConditionIndex != -1)
			return true;

		final int size = _awaitingConditions.size();
		for (int i = 0; i < size; i++) {
			final ResultAwaitingCondition awaitingCondition = _awaitingConditions.get(i);
			if (awaitingCondition.isMet()) {
				_metConditionIndex = i;
				return true;
			}
		}

		return false;
	}

	@Override
	public Variable getReturnValue() {
		List<Variable> result = new ArrayList<Variable>();
		result.add(new Variable(_metConditionIndex));
		result.add(_awaitingConditions.get(_metConditionIndex).getReturnValue());
		return new Variable(result);
	}
}