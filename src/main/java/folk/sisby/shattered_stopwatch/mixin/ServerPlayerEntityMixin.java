package folk.sisby.shattered_stopwatch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import folk.sisby.shattered_stopwatch.ShatteredStopwatch;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@ModifyReturnValue(method = "isInTeleportationState", at = @At("RETURN"))
	private boolean teleportingWhileShattered(boolean original) {
		ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
		return original || self.getStackInHand(Hand.MAIN_HAND).contains(ShatteredStopwatch.ACTIVE_STOPWATCH) || self.getStackInHand(Hand.OFF_HAND).contains(ShatteredStopwatch.ACTIVE_STOPWATCH);
	}
}
