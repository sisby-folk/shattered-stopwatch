package folk.sisby.shattered_stopwatch.client;

import folk.sisby.shattered_stopwatch.ShatteredStopwatch;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class ShatteredStopwatchClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelPredicateProviderRegistry.register(ShatteredStopwatch.STOPWATCH, Identifier.of(ShatteredStopwatch.ID, "stopwatch_active"),
			(stack, world, entity, i) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F
		);
	}
}
