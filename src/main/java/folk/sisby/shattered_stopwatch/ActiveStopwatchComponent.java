package folk.sisby.shattered_stopwatch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public record ActiveStopwatchComponent(RegistryKey<World> startDimension, Vec3d startPosition, float startYaw, float startPitch, float startFallDistance, long startTick, int lap, List<Vec3d> lapPositions) {
	public static final Codec<ActiveStopwatchComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("startDimension").forGetter(ActiveStopwatchComponent::startDimension),
		Vec3d.CODEC.fieldOf("startPosition").forGetter(ActiveStopwatchComponent::startPosition),
		Codec.FLOAT.fieldOf("startYaw").forGetter(ActiveStopwatchComponent::startYaw),
		Codec.FLOAT.fieldOf("startPitch").forGetter(ActiveStopwatchComponent::startPitch),
		Codec.FLOAT.fieldOf("startFallDistance").forGetter(ActiveStopwatchComponent::startFallDistance),
		Codec.LONG.fieldOf("startTick").forGetter(ActiveStopwatchComponent::startTick),
		Codec.INT.fieldOf("lap").forGetter(ActiveStopwatchComponent::lap),
		Codec.list(Vec3d.CODEC).fieldOf("lapPositions").forGetter(ActiveStopwatchComponent::lapPositions)
	).apply(instance, ActiveStopwatchComponent::new));

	public ActiveStopwatchComponent withLap(Vec3d lapPosition) {
		List<Vec3d> newLaps = new ArrayList<>(lapPositions);
		newLaps.add(lapPosition);
		return new ActiveStopwatchComponent(startDimension, startPosition, startYaw, startPitch, startFallDistance, startTick, lap + 1, newLaps);
	}

	public ActiveStopwatchComponent withoutLap(Vec3d usedLap) {
		List<Vec3d> newLaps = new ArrayList<>(lapPositions);
		newLaps.remove(usedLap);
		return new ActiveStopwatchComponent(startDimension, startPosition, startYaw, startPitch, startFallDistance, startTick, lap, newLaps);
	}
}
