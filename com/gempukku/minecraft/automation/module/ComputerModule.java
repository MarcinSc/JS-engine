package com.gempukku.minecraft.automation.module;

import com.gempukku.minecraft.automation.computer.ServerComputerData;
import com.gempukku.minecraft.automation.lang.FunctionExecutable;
import net.minecraft.world.IBlockAccess;

/**
 * Interface to implement when defining a ComputerModule.
 */
public interface ComputerModule {
    /**
     * Returns extra storage space provided by the module inside the computer.
     *
     * @return Storage space.
     */
    public int getStorageSlots();

    /**
     * Returns a string representing the module type. It is recommended to user Camel-Case string, with no spaces,
     * starting with a capital letter, i.e. "SampleModuleDiggingHoles".
     *
     * @return Module type.
     */
    public String getModuleType();

    /**
     * Checks if this module can be placed in an existing computer.
     * This method is for modules that should not be placed in multiples, or in combinations with other modules, as
     * it allows modules to control the configuration of a computer.
     *
     * @param computerData Computer this module is being placed in.
     * @return True, if it's ok to place this module in the computer passed as a parameter, false otherwise.
     */
    public boolean canBePlacedInComputer(ServerComputerData computerData);

    /**
     * Checks if this module, that is already placed in the computer, is ok with adding a new module to the computer.
     * This method is for modules that should not be placed in multiples, or in combinations with other modules, as
     * it allows modules to control the configuration of a computer.
     *
     * @param computerData   Computer this module is being placed in.
     * @param computerModule New computer module that is being placed into the computer.
     * @return True, if it's ok to place the module in the computer passed as a parameter, false otherwise.
     */
    public boolean acceptsNewModule(ServerComputerData computerData, ComputerModule computerModule);

    /**
     * Returns a function with the specified name. To make the implementation easier, you can subclass
     * JavaFunctionExecutable class, that defines an easy to use interface for Java developers.
     *
     * @param name Name of the function this module supports.
     * @return Function that will be executed, when invoked by the program, or null if there is no function with this
     *         name.
     */
    public FunctionExecutable getFunctionByName(String name);

    /**
     * Returns strength of the weak redstone signal on the specified side of the computer.
     *
     * @param computerData Computer that is queried for returning the signal.
     * @param input        The input strength power that might have been generated by the computer itself, or any other module.
     * @param blockAccess  Access to blocks in the world.
     * @param side         Side of the computer that is queried for redstone signal
     * @return The signal strength (if any). If no signal should be returned - return 0, otherwise the strength (1-15).
     *         If this ComputerModule has nothing to do with Redstone signal, it should just return input value.
     */
    public int getWeakRedstoneSignalStrengthOnSide(ServerComputerData computerData, int input, IBlockAccess blockAccess, int side);

    /**
     * Returns strength of the strong redstone signal on the specified side of the computer.
     *
     * @param computerData Computer that is queried for returning the signal.
     * @param input        The input strength power that might have been generated by the computer itself, or any other module.
     * @param blockAccess  Access to blocks in the world.
     * @param side         Side of the computer that is queried for redstone signal
     * @return The signal strength (if any). If no signal should be returned - return 0, otherwise the strength (1-15).
     *         If this ComputerModule has nothing to do with Redstone signal, it should just return input value.
     */
    public int getStrongRedstoneSignalStrengthOnSide(ServerComputerData computerData, int input, IBlockAccess blockAccess, int side);
}
