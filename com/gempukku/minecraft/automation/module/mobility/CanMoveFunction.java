package com.gempukku.minecraft.automation.module.mobility;

import com.gempukku.minecraft.BoxSide;
import com.gempukku.minecraft.automation.computer.JavaFunctionExecutable;
import com.gempukku.minecraft.automation.computer.ServerComputerData;
import com.gempukku.minecraft.automation.lang.ExecutionException;
import com.gempukku.minecraft.automation.lang.Variable;
import net.minecraft.block.material.Material;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

import java.util.Map;

public class CanMoveFunction extends JavaFunctionExecutable {
    @Override
    protected int getDuration() {
        return 100;
    }

    @Override
    public String[] getParameterNames() {
        return new String[]{"direction"};
    }

    @Override
    protected Object executeFunction(ServerComputerData computer, World world, Map<String, Variable> parameters) throws ExecutionException {
        final int facing = computer.getFacing();
        Variable sideVar = parameters.get("direction");

        if (sideVar.getType() != Variable.Type.STRING)
            throw new ExecutionException("Expected forward, up or down in move function");

        String side = (String) sideVar.getValue();
        if (!side.equals("forward") || !side.equals("up") || !side.equals("down"))
            throw new ExecutionException("Expected forward, up or down in move function");

        int direction = facing;
        if (side.equals("up"))
            direction = BoxSide.TOP;
        else if (side.equals("down"))
            direction = BoxSide.BOTTOM;

        final int newX = computer.getX() + Facing.offsetsXForSide[direction];
        final int newY = computer.getY() + Facing.offsetsYForSide[direction];
        final int newZ = computer.getZ() + Facing.offsetsZForSide[direction];

        final Material blockMaterial = world.getBlockMaterial(newX, newY, newZ);
        if (!blockMaterial.isReplaceable())
            return false;

        return true;
    }
}