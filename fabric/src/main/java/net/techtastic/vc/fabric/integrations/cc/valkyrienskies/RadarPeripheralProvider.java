package net.techtastic.vc.fabric.integrations.cc.valkyrienskies;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.techtastic.vc.ValkyrienComputersBlocks;
import net.techtastic.vc.ValkyrienComputersConfig;
import net.techtastic.vc.integrations.cc.valkyrienskies.RadarPeripheral;
import org.jetbrains.annotations.NotNull;

public class RadarPeripheralProvider implements IPeripheralProvider {
	@Override
	public IPeripheral getPeripheral(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull Direction direction) {
		if (
				ValkyrienComputersBlocks.INSTANCE.getRADAR() != null &&
						level.getBlockState(blockPos).is(ValkyrienComputersBlocks.INSTANCE.getRADAR().get()) &&
						!ValkyrienComputersConfig.SERVER.getComputerCraft().getDisableRadars()
		) {
			return new RadarPeripheral(level, blockPos);
		}
		return null;
	}
}