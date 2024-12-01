package folk.sisby.shattered_stopwatch.client;

import folk.sisby.shattered_stopwatch.ActiveStopwatchComponent;
import folk.sisby.shattered_stopwatch.ShatteredStopwatch;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class ShatteredStopwatchClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelPredicateProviderRegistry.register(ShatteredStopwatch.STOPWATCH, Identifier.of(ShatteredStopwatch.ID, "tick"),
			(stack, world, entity, i) -> {
				ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
				return asc != null && world != null ? ((world.getTime() - asc.startTick()) % 20) / 20.0F + 0.1F : 0F;
			}
		);
	}
}
