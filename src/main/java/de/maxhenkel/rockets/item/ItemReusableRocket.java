package de.maxhenkel.rockets.item;

import de.maxhenkel.rockets.Main;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class ItemReusableRocket extends Item {

    private final Supplier<Integer> maxDuration;
    private final Supplier<Integer> maxUses;

    public ItemReusableRocket(Supplier<Integer> maxUses, Supplier<Integer> maxDuration) {
        super(new Properties().stacksTo(1));
        this.maxDuration = maxDuration;
        this.maxUses = maxUses;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
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
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
        } else if (player.isFallFlying() && getUsesLeft(stack) > 0) {
            if (!Main.SERVER_CONFIG.allowRocketSpamming.get() && isGettingBoosted(player)) {
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            if (!world.isClientSide) {
                int usesLeft = getUsesLeft(stack);
                int duration = Math.min(getFlightDuration(stack), usesLeft);
                world.addFreshEntity(new FireworkRocketEntity(world, createDummyFirework((byte) duration), player));
                setUsesLeft(stack, Math.max(0, usesLeft - duration));
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
        }
        return InteractionResultHolder.fail(player.getItemInHand(hand));

    }

    protected byte getFlightDuration(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Duration")) {
            return tag.getByte("Duration");
        }
        return 1;
    }

    protected void setFlightDuration(ItemStack stack, byte duration) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putByte("Duration", duration);
    }

    public void setUsesLeft(ItemStack stack, int usesLeft) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("UsesLeft", usesLeft);
    }

    public int getUsesLeft(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("UsesLeft")) {
            return tag.getInt("UsesLeft");
        }
        return 0;
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
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reusable_rockets.flight_duration", getFlightDuration(stack), maxDuration.get()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.reusable_rockets.uses", getUsesLeft(stack), maxUses.get()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.reusable_rockets.sneak_to_change").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, world, tooltip, flag);
    }

    protected ItemStack createDummyFirework(byte flightDuration) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.getOrCreateTagElement("Fireworks").putByte("Flight", flightDuration);
        return stack;
    }

    public boolean isGettingBoosted(Player player) {
        return player.level.getEntitiesOfClass(FireworkRocketEntity.class, player.getBoundingBox().inflate(2D), rocket -> {
            return rocket.attachedToEntity == player;
        }).stream().findAny().isPresent();
    }

}
