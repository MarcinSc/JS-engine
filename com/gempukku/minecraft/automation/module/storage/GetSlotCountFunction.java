package com.gempukku.minecraft.automation.module.storage;

import com.gempukku.minecraft.BoxSide;
import com.gempukku.minecraft.automation.computer.JavaFunctionExecutable;
import com.gempukku.minecraft.automation.computer.ServerComputerData;
import com.gempukku.minecraft.automation.lang.ExecutionException;
import com.gempukku.minecraft.automation.lang.Variable;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;

import java.util.Map;

public class GetSlotCountFunction extends JavaFunctionExecutable {
    @Override
    protected int getDuration() {
        return 100;
    }

    @Override
    public String[] getParameterNames() {
        return new String[]{"side"};
    }

    @Override
    protected Object executeFunction(ServerComputerData computer, World world, Map<String, Variable> parameters) throws ExecutionException {
        final Variable sideParam = parameters.get("side");
        final String functionName = "getSlotCount";
        final IInventory inventory = StorageModuleUtils.getInventoryAtFace(computer, world, sideParam, functionName);
        if (inventory == null)
            return 0;

        return StorageModuleUtils.getInventorySize(inventory, BoxSide.getOpposite(StorageModuleUtils.getComputerFacingSide(computer, sideParam, functionName)));
    }
}