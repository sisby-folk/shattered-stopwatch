package folk.sisby.shattered_stopwatch;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShatteredStopwatch implements ModInitializer {
	public static final String ID = "shattered_stopwatch";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[Shattered Stopwatch] left field out of yesterday");
	}
}
