package com.gempukku.minecraft.automation.computer.computer;

import com.gempukku.minecraft.automation.AutomationUtils;
import com.gempukku.minecraft.automation.block.ComputerTileEntity;
import com.gempukku.minecraft.automation.computer.ComputerCallback;
import com.gempukku.minecraft.automation.computer.JavaFunctionExecutable;
import com.gempukku.minecraft.automation.computer.bind.SlotBindingObjectDefinition;
import com.gempukku.minecraft.automation.lang.ExecutionException;
import com.gempukku.minecraft.automation.lang.Variable;
import net.minecraft.world.World;

import java.util.Map;

public class BindModuleFunction extends JavaFunctionExecutable {
	@Override
	public String[] getParameterNames() {
		return new String[]{"slot"};
	}

	@Override
	protected int getDuration() {
		return 100;
	}

	@Override
	protected Object executeFunction(int line, World world, ComputerCallback computer, Map<String, Variable> parameters) throws ExecutionException {
		final Variable slot = parameters.get("slot");
		if (slot.getType() != Variable.Type.NUMBER)
			throw new ExecutionException(line, "Expected slot number in bindModule()");
		int slotNo = ((Number) slot.getValue()).intValue();

		final ComputerTileEntity computerTileEntity = AutomationUtils.getComputerEntitySafely(world, computer);
		if (computerTileEntity == null)
			return null;

		if (slotNo < 0 || slotNo >= computerTileEntity.getModuleSlotsCount())
			throw new ExecutionException(line, "Slot number outside of permitted range in bindModule()");

		return new SlotBindingObjectDefinition(slotNo);
	}
}
