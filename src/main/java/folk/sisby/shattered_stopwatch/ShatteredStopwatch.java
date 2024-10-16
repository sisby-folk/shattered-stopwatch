package folk.sisby.shattered_stopwatch;

import net.fabricmc.api.ModInitializer;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShatteredStopwatch implements ModInitializer {
	public static final String ID = "shattered_stopwatch";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static final StopwatchItem STOPWATCH = Registry.register(Registries.ITEM, Identifier.of(ID, "stopwatch"), new StopwatchItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

	public static final ComponentType<ActiveStopwatchComponent> ACTIVE_STOPWATCH = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(ID, "active_stopwatch"),
		ComponentType.<ActiveStopwatchComponent>builder().codec(ActiveStopwatchComponent.CODEC).build()
	);

	@Override
	public void onInitialize() {
		LOGGER.info("[Shattered Stopwatch] left field out of yesterday");
	}
}
