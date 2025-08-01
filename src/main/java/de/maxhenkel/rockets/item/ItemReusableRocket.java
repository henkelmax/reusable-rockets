package de.maxhenkel.rockets.item;

import de.maxhenkel.rockets.ReusableRocketsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemReusableRocket extends Item {

    private final Supplier<Integer> maxDuration;
    private final Supplier<Integer> maxUses;

    public ItemReusableRocket(Properties properties, Supplier<Integer> maxUses, Supplier<Integer> maxDuration) {
        super(properties.stacksTo(1));
        this.maxDuration = maxDuration;
        this.maxUses = maxUses;
    }

    public RocketData createDefaultRocketData() {
        return new RocketData(maxDuration.get().byteValue(), maxUses.get());
    }

    public RocketData getRocketData(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemReusableRocket)) {
            throw new IllegalArgumentException("Not a reusable rocket");
        }
        RocketData rocketData = stack.get(ReusableRocketsMod.ROCKET_DATA_COMPONENT);
        if (rocketData == null) {
            rocketData = createDefaultRocketData();
            stack.set(ReusableRocketsMod.ROCKET_DATA_COMPONENT, rocketData);
        }
        return rocketData;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            byte duration = getFlightDuration(stack);
            duration++;
            if (duration > maxDuration.get()) {
                duration = 1;
            }
            setFlightDuration(stack, duration);
            player.displayClientMessage(Component.translatable("message.reusable_rockets.set_flight_duration", duration, maxDuration.get()), true);
            player.playSound(SoundEvents.STONE_BUTTON_CLICK_OFF, 1F, 1F);
            return InteractionResult.SUCCESS;
        } else if (player.isFallFlying() && getUsesLeft(stack) > 0) {
            if (!ReusableRocketsMod.SERVER_CONFIG.allowRocketSpamming.get() && isGettingBoosted(player)) {
                return InteractionResult.FAIL;
            }
            if (!world.isClientSide) {
                int usesLeft = getUsesLeft(stack);
                int duration = Math.min(getFlightDuration(stack), usesLeft);
                world.addFreshEntity(new FireworkRocketEntity(world, createDummyFirework((byte) duration), player));
                setUsesLeft(stack, Math.max(0, usesLeft - duration));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;

    }

    protected byte getFlightDuration(ItemStack stack) {
        return getRocketData(stack).getFlightDuration();
    }

    protected void setFlightDuration(ItemStack stack, byte duration) {
        RocketData rocketData = stack.get(ReusableRocketsMod.ROCKET_DATA_COMPONENT);
        if (rocketData == null) {
            rocketData = createDefaultRocketData();
        }
        stack.set(ReusableRocketsMod.ROCKET_DATA_COMPONENT, new RocketData(duration, rocketData.getUsesLeft()));
    }

    public void setUsesLeft(ItemStack stack, int usesLeft) {
        RocketData rocketData = stack.get(ReusableRocketsMod.ROCKET_DATA_COMPONENT);
        if (rocketData == null) {
            rocketData = createDefaultRocketData();
        }
        stack.set(ReusableRocketsMod.ROCKET_DATA_COMPONENT, new RocketData(rocketData.getFlightDuration(), usesLeft));
    }

    public int getUsesLeft(ItemStack stack) {
        return getRocketData(stack).getUsesLeft();
    }

    public int getMaxUses() {
        return maxUses.get();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ChatFormatting.DARK_RED.getColor();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13F - (float) (maxUses.get().doubleValue() - getUsesLeft(stack)) * 13F / maxUses.get().floatValue());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.translatable("tooltip.reusable_rockets.flight_duration", getFlightDuration(stack), maxDuration.get()).withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.translatable("tooltip.reusable_rockets.uses", getUsesLeft(stack), maxUses.get()).withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.translatable("tooltip.reusable_rockets.sneak_to_change").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipDisplay, consumer, flag);
    }

    protected ItemStack createDummyFirework(byte flightDuration) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        Fireworks fireworks = new Fireworks(flightDuration, Collections.emptyList());
        stack.set(DataComponents.FIREWORKS, fireworks);
        return stack;
    }

    public boolean isGettingBoosted(Player player) {
        return player.level().getEntitiesOfClass(FireworkRocketEntity.class, player.getBoundingBox().inflate(2D), rocket -> {
            return rocket.attachedToEntity == player;
        }).stream().findAny().isPresent();
    }

}
