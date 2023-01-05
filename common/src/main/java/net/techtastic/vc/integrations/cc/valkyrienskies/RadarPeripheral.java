package net.techtastic.vc.integrations.cc.valkyrienskies;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.techtastic.vc.ValkyrienComputersBlocks;
import net.techtastic.vc.ValkyrienComputersConfig;
import net.techtastic.vc.ValkyrienComputersConfig.Server.COMPUTERCRAFT.RADARSETTINGS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.List;

public class RadarPeripheral implements IPeripheral {
	private Level level;
	private BlockPos pos;

	public RadarPeripheral(Level level, BlockPos worldPosition) {
		this.level = level;
		this.pos = worldPosition;
	}

	@LuaFunction
	public final Object[] scan(double radius) throws LuaException {
		if (ValkyrienComputersConfig.SERVER.getComputerCraft().getDisableRadars()) throw new LuaException("Disabled");
		return scanForShips(level, pos, radius);
	}

	@NotNull
	@Override
	public String getType() {
		return "radar";
	}

	@Override
	public boolean equals(@Nullable IPeripheral iPeripheral) {
		if (ValkyrienComputersBlocks.INSTANCE.getRADAR() != null) {
			return level.getBlockState(pos).is(ValkyrienComputersBlocks.INSTANCE.getRADAR().get());
		}
		return false;
	}

	public Object[] scanForShips(Level level, BlockPos position, double radius) {
		RADARSETTINGS settings = ValkyrienComputersConfig.SERVER.getComputerCraft().getRadarSettings();

		if (level == null || position == null) {
			return new Object[] {"booting"};
		}

		if (!level.isClientSide()) {
			// THROW EARLY RESULTS
			if (radius < 1.0) {
				return new Object[] {"radius too small"};
			} else if (radius > settings.getMaxRadarRadius()) {
				return new Object[] {"radius too big"};
			}
			if (!level.getBlockState(position).is(ValkyrienComputersBlocks.INSTANCE.getRADAR().get())) {
				return new Object[] {"no radar"};
			}

			// IF RADAR IS ON A SHIP, USE THE WORLD SPACE COORDINATES
			Ship test = VSGameUtilsKt.getShipManagingPos(level, position);
			if (test != null) {
				Vector3d newPos = VSGameUtilsKt.toWorldCoordinates(test, position);
				position = new BlockPos(newPos.x, newPos.y, newPos.z);
			}

			// GET A LIST OF SHIP POSITIONS WITHIN RADIUS
			List<Vector3d> ships = VSGameUtilsKt.transformToNearbyShipsAndWorld(level, position.getX(), position.getY(), position.getZ(), radius);
			Object[] results = new Object[ships.size()];

			// TESTING FOR NO SHIPS
			if (results.length == 0) {
				return new Object[] {"no ships"};
			}

			// Give results the ID, X, Y, and z of each Ship
			for (Vector3d vec : ships) {
				ServerShip data = VSGameUtilsKt.getShipManagingPos(((ServerLevel) level), vec.x, vec.y, vec.z);
				Vector3dc pos = data.getTransform().getPositionInWorld();

				HashMap<String, Object> result = new HashMap<>();
				// Give Name
				//if (settings.getRadarGetsName()) result.put("name", data.getName());

				// Give Position
				if (settings.getRadarGetsPosition()) result.put("position", new Object[] {pos.x(), pos.y(), pos.z()});

				// Give Mass
				if (settings.getRadarGetsMass()) result.put("mass", data.getInertiaData().getMass());

				// Give Rotation
				if (settings.getRadarGetsRotation()) {
					Quaterniondc rot = data.getTransform().getShipToWorldRotation();
					result.put("rotation", new Object[]{rot.x(), rot.y(), rot.z(), rot.w()});
				}

				// Give Velocity
				if (settings.getRadarGetsVelocity()) {
					Vector3dc vel = data.getVelocity();
					result.put("velocity", new Object[]{vel.x(), vel.y(), vel.z()});
				}

				// Give Distance
				if (settings.getRadarGetsDistance()) result.put("distance", VSGameUtilsKt.squaredDistanceBetweenInclShips(
						level,
						vec.x,
						vec.y,
						vec.z,
						pos.x(),
						pos.y(),
						pos.z()
				));

				// Give Size
				if (settings.getRadarGetsSize()) {
					AABBic aabb = data.getShipAABB();
					result.put("size", new Object[]{
							Math.abs(aabb.maxX() - aabb.minX()),
							Math.abs(aabb.maxY() - aabb.minY()),
							Math.abs(aabb.maxZ() - aabb.minZ())
					});
				}
				
				results[ships.indexOf(vec)] = result;
			}

			return results;
		}
		return new Object[0];
	}
}
