package folk.sisby.shattered_stopwatch;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
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
import java.util.List;

public class StopwatchItem extends Item {
	public StopwatchItem(Settings settings) {
		super(settings);
	}

	@Override
	public Text getName(ItemStack stack) {
		return stack.contains(ShatteredStopwatch.ACTIVE_STOPWATCH) ? Text.translatable("item.shattered_stopwatch.stopwatch.active").setStyle(super.getName(stack).getStyle()) : super.getName(stack);
	}

	public boolean isValid(ItemStack stack, PlayerEntity user) {
		ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
		return asc != null && (user.getStackInHand(Hand.MAIN_HAND) == stack || user.getStackInHand(Hand.OFF_HAND) == stack) && user.getWorld().getRegistryKey().equals(stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH).startDimension());
	}

	public void start(ItemStack stack, PlayerEntity user) {
		if (!user.isOnGround()) {
			if (user.getWorld().isClient()) {
				user.playSound(SoundEvents.BLOCK_LEVER_CLICK, 0.7F, 2.0F);
			}
			return;
		}
		boolean reflection = EnchantmentHelper.hasAnyEnchantmentsIn(stack, ShatteredStopwatch.REFLECTION);
		stack.set(ShatteredStopwatch.ACTIVE_STOPWATCH, new ActiveStopwatchComponent(user.getWorld().getRegistryKey(), user.getPos(), user.getYaw(), user.getPitch(), user.fallDistance, user.getWorld().getTime(), 0, new ArrayList<>(), new ArrayList<>()));
		user.playSound(SoundEvents.BLOCK_ANVIL_USE, 2.0F, 1.5F);
		if (user.getWorld().isClient()) {
			user.sendMessage(Text.translatable(
				"tooltip.shattered_stopwatch.stopwatch.lap",
				Text.translatable("tooltip.shattered_stopwatch.stopwatch.lap.ticker").formatted(reflection ? Formatting.LIGHT_PURPLE : Formatting.DARK_RED).formatted(Formatting.OBFUSCATED),
				Text.translatable("action.shattered_stopwatch.start" + (reflection ? ".reflection." + user.getRandom().nextInt(10) : "")).formatted(Formatting.WHITE).formatted(Formatting.ITALIC),
				Text.translatable("tooltip.shattered_stopwatch.stopwatch.lap.ticker").formatted(reflection ? Formatting.LIGHT_PURPLE : Formatting.DARK_RED).formatted(Formatting.OBFUSCATED)
			), true);
		}
	}

	private void lap(ItemStack stack, PlayerEntity user) {
		ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
		if (asc == null) return;
		boolean reflection = EnchantmentHelper.hasAnyEnchantmentsIn(stack, ShatteredStopwatch.REFLECTION);
		Vec3d echoPos = user.getPos();
		user.fallDistance = 0;
		if (user.getWorld().isClient) {
			user.refreshPositionAndAngles(asc.startPosition(), asc.startYaw(), asc.startPitch());
		}
		user.setVelocity(Vec3d.ZERO);
		user.playSound(SoundEvents.ITEM_SPYGLASS_USE);
		if (user.getWorld().isClient()) {
			user.sendMessage(Text.translatable(
				"tooltip.shattered_stopwatch.stopwatch.lap",
				Text.translatable("tooltip.shattered_stopwatch.stopwatch.lap.ticker").formatted(reflection ? Formatting.LIGHT_PURPLE : Formatting.DARK_RED).formatted(Formatting.OBFUSCATED),
				Text.translatable("tooltip.shattered_stopwatch.stopwatch.lap.count", asc.lap() + 2).formatted(Formatting.WHITE),
				Text.translatable("tooltip.shattered_stopwatch.stopwatch.lap.ticker").formatted(reflection ? Formatting.LIGHT_PURPLE : Formatting.DARK_RED).formatted(Formatting.OBFUSCATED)
			), true);
		}
		boolean multi = EnchantmentHelper.hasAnyEnchantmentsIn(stack, ShatteredStopwatch.REFLECTION);
		stack.apply(ShatteredStopwatch.ACTIVE_STOPWATCH, null, c -> c.withLap(echoPos, multi ? 2 : 1));
	}

	public void stop(ItemStack stack, PlayerEntity user) {
		boolean reflection = EnchantmentHelper.hasAnyEnchantmentsIn(stack, ShatteredStopwatch.REFLECTION);
		ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
		if (asc == null) return;
		user.playSound(SoundEvents.BLOCK_GLASS_BREAK);
		long seconds = (user.getWorld().getTime() - asc.startTick()) / 20;
		if (user.getWorld().isClient()) {
			user.sendMessage(Text.translatable(
				"action.shattered_stopwatch.stop",
				Text.translatable("action.shattered_stopwatch.stop.shattered").formatted(reflection ? Formatting.LIGHT_PURPLE : Formatting.DARK_RED),
				Text.translatable("action.shattered_stopwatch.stop.laps" + (asc.lap() == 0 ? ".single" : ""), asc.lap() + 1).formatted(Formatting.WHITE),
				Text.translatable("action.shattered_stopwatch.stop.seconds" + (seconds == 1 ? ".single" : ""), seconds).formatted(Formatting.WHITE)
			).formatted(Formatting.GRAY), true);
		}
		stack.remove(ShatteredStopwatch.ACTIVE_STOPWATCH);
	}

	public void bounce(ItemStack stack, PlayerEntity user, Vec3d echo) {
		if (echo == null) return;
		stack.apply(ShatteredStopwatch.ACTIVE_STOPWATCH, null, c -> c.withoutLap(echo));
		user.fallDistance = 0;
		user.addVelocity(0, 1 - user.getVelocity().getY(), 0);
		user.playSound(SoundEvents.BLOCK_LARGE_AMETHYST_BUD_BREAK, 1.0F, 1.0F);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof PlayerEntity user)) return;
		if (!isValid(stack, user)) {
			stop(stack, user);
			return;
		} else {
			ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
			Multiset<Vec3d> echoes = HashMultiset.create(asc.lapPositions());
			// Particles
			world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), asc.startPosition().x, asc.startPosition().y + entity.getHeight() / 2, asc.startPosition().z, 0, 0, 0);
			for (Vec3d echo : echoes.elementSet()) {
				if (asc.touchedThisLap().contains(echo)) continue;
				boolean multiRemaining = echoes.count(echo) > 1;
				world.addParticle(new DustParticleEffect(new Vector3f(1.0F, multiRemaining ? 0.3F : 0.0F, multiRemaining ? 1.0F : 0.0F), 1.0F), echo.x, echo.y + entity.getHeight() / 2, echo.z, 0, 0, 0);
			}
			// Bounce
			for (Vec3d echo : asc.lapPositions()) {
				if (asc.touchedThisLap().contains(echo)) continue;
				if (echo.isInRange(user.getPos(), 1.0)) {
					bounce(stack, user, echo);
					break;
				}
			}
		}
		super.inventoryTick(stack, world, entity, slot, selected);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		user.getItemCooldownManager().set(stack.getItem(), 20);
		if (user.isSneaking()) {
			stop(stack, user);
		} else {
			if (stack.contains(ShatteredStopwatch.ACTIVE_STOPWATCH)) {
				if (isValid(stack, user)) {
					lap(stack, user);
				} else {
					stop(stack, user);
				}
			} else {
				start(stack, user);
			}
		}
		return new TypedActionResult<>(ActionResult.SUCCESS_NO_ITEM_USED, stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		ActiveStopwatchComponent asc = stack.get(ShatteredStopwatch.ACTIVE_STOPWATCH);
		boolean reflection = EnchantmentHelper.hasAnyEnchantmentsIn(stack, ShatteredStopwatch.REFLECTION);
		if (asc != null) {
			tooltip.add(Text.translatable("tooltip.shattered_stopwatch.stopwatch.active" + (reflection ? ".reflection" : "")).formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
			tooltip.add(Text.translatable(
				"tooltip.shattered_stopwatch.stopwatch.lap",
				Text.translatable("tooltip.shattered_stopwatch.stopwatch.lap.ticker").formatted(reflection ? Formatting.LIGHT_PURPLE : Formatting.DARK_RED).formatted(Formatting.OBFUSCATED),
				Text.translatable("tooltip.shattered_stopwatch.stopwatch.lap.count", asc.lap() + 1).formatted(Formatting.WHITE),
				Text.translatable("tooltip.shattered_stopwatch.stopwatch.lap.ticker").formatted(reflection ? Formatting.LIGHT_PURPLE : Formatting.DARK_RED).formatted(Formatting.OBFUSCATED)
			).formatted(Formatting.ITALIC));
		} else {
			tooltip.add(Text.translatable("tooltip.shattered_stopwatch.stopwatch.inactive" + (reflection ? ".reflection" : "")).formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
		}
		super.appendTooltip(stack, context, tooltip, type);
	}
}
