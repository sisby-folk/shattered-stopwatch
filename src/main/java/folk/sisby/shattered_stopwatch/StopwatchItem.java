package folk.sisby.shattered_stopwatch;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;

public class StopwatchItem extends Item {
	public StopwatchItem(Settings settings) {
		super(settings);
	}

	@Override
	public Text getName(ItemStack stack) {
		return stack.contains(ShatteredStopwatch.ACTIVE_STOPWATCH) ? Text.translatable("item.shattered_stopwatch.stopwatch.active").setStyle(super.getName(stack).getStyle()) : super.getName(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof PlayerEntity player)) return;
		if (stack.contains(ShatteredStopwatch.ACTIVE_STOPWATCH)) {
			if ((player.getStackInHand(Hand.MAIN_HAND) != stack && player.getStackInHand(Hand.OFF_HAND) != stack) || !world.getRegistryKey().equals(stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH).startDimension())) {
				ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
				player.playSound(SoundEvents.BLOCK_GLASS_BREAK);
				player.sendMessage(Text.translatable("action.shattered_stopwatch.stop", asc.lap() + 1, (world.getTime() - asc.startTick()) / 20).formatted(Formatting.RED), true);
				stack.remove(ShatteredStopwatch.ACTIVE_STOPWATCH);
				return;
			} else {
				ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
				// Particles
				Multiset<Vec3d> echoes = HashMultiset.create(asc.lapPositions());
				echoes.removeAll(asc.touchedThisLap());
				for (Vec3d lapPosition : echoes.elementSet()) {
					boolean multiRemaining = echoes.count(lapPosition) > 1;
					world.addParticle(new DustParticleEffect(new Vector3f(1.0F,  multiRemaining ? 0.3F : 0.0F, multiRemaining ? 1.0F : 0.0F), 1.0F), lapPosition.x, lapPosition.y + entity.getHeight() / 2, lapPosition.z, 0, 0, 0);
				}
				// Boosts
				Vec3d usedLap = null;
				for (Vec3d lapPosition : asc.lapPositions()) {
					if (lapPosition.isInRange(entity.getPos(), 1.0)) {
						usedLap = lapPosition;
						break;
					}
				}
				if (usedLap != null) {
					entity.fallDistance = 0;
					entity.addVelocity(0, 1 - entity.getVelocity().getY(), 0);
					entity.playSound(SoundEvents.BLOCK_LARGE_AMETHYST_BUD_BREAK, 1.0F, 1.0F);
					final Vec3d removeLap = usedLap;
					stack.apply(ShatteredStopwatch.ACTIVE_STOPWATCH, null, c -> c.withoutLap(removeLap));
				}
			}
		}
		super.inventoryTick(stack, world, entity, slot, selected);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		user.getItemCooldownManager().set(stack.getItem(), 20);
		if (stack.contains(ShatteredStopwatch.ACTIVE_STOPWATCH) && !world.getRegistryKey().equals(stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH).startDimension())) {
			stack.remove(ShatteredStopwatch.ACTIVE_STOPWATCH);
			return new TypedActionResult<>(ActionResult.FAIL, stack);
		}
		if (user.isSneaking()) {
			if (stack.contains(ShatteredStopwatch.ACTIVE_STOPWATCH)) { // Stop - replace with dropping later, it'd be cooler
				ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
				user.playSound(SoundEvents.BLOCK_GLASS_BREAK);
				user.sendMessage(Text.translatable("action.shattered_stopwatch.stop", asc.lap() + 1, (world.getTime() - asc.startTick()) / 20).formatted(Formatting.RED), true);
				stack.remove(ShatteredStopwatch.ACTIVE_STOPWATCH);
				return new TypedActionResult<>(ActionResult.SUCCESS_NO_ITEM_USED, stack);
			}
		} else {
			if (stack.contains(ShatteredStopwatch.ACTIVE_STOPWATCH)) { // Lap
				Vec3d echoPos = user.getPos();
				ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
				user.fallDistance = 0;
				if (world.isClient) {
					user.refreshPositionAndAngles(asc.startPosition(), asc.startYaw(), asc.startPitch());
				}
				user.setVelocity(Vec3d.ZERO);
				user.playSound(SoundEvents.ITEM_SPYGLASS_USE);
				user.sendMessage(Text.translatable("action.shattered_stopwatch.lap", asc.lap() + 2).formatted(Formatting.LIGHT_PURPLE), true);
				boolean multi = EnchantmentHelper.hasAnyEnchantmentsIn(stack, ShatteredStopwatch.REFLECTION);
				stack.apply(ShatteredStopwatch.ACTIVE_STOPWATCH, null, c -> c.withLap(echoPos,  multi ? 2 : 1));
			} else { // Start
				stack.set(ShatteredStopwatch.ACTIVE_STOPWATCH, new ActiveStopwatchComponent(world.getRegistryKey(), user.getPos(), user.getYaw(), user.getPitch(), user.fallDistance, world.getTime(), 0, new ArrayList<>(), new ArrayList<>()));
				user.playSound(SoundEvents.BLOCK_ANVIL_USE, 2.0F, 1.5F);
				user.sendMessage(Text.translatable("action.shattered_stopwatch.lap", 1).formatted(Formatting.AQUA), true);
			}
			return new TypedActionResult<>(ActionResult.SUCCESS_NO_ITEM_USED, stack);
		}
		return super.use(world, user, hand);
	}
}
