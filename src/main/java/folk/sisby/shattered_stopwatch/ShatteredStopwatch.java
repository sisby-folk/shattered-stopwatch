package folk.sisby.shattered_stopwatch;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShatteredStopwatch implements ModInitializer {
	public static final String ID = "shattered_stopwatch";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static final StopwatchItem STOPWATCH = Registry.register(Registries.ITEM, Identifier.of(ID, "stopwatch"), new StopwatchItem(new Item.Settings().maxCount(1)));

	@Override
	public void onInitialize() {
		LOGGER.info("[Shattered Stopwatch] left field out of yesterday");
	}
}
