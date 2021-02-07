package de.maxhenkel.rockets.item;

import de.maxhenkel.rockets.Main;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;
import java.util.function.Supplier;

public class ItemReusableRocket extends Item {

    private final Supplier<Integer> maxDuration;
    private final Supplier<Integer> maxUses;

    public ItemReusableRocket(String name, Supplier<Integer> maxUses, Supplier<Integer> maxDuration) {
        super(new Properties().maxStackSize(1).group(ItemGroup.MISC));
        this.maxDuration = maxDuration;
        this.maxUses = maxUses;
        setRegistryName(new ResourceLocation(Main.MODID, name));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            byte duration = getFlightDuration(stack);
            duration++;
            if (duration > maxDuration.get()) {
                duration = 1;
            }
            setFlightDuration(stack, duration);
            player.sendStatusMessage(new TranslationTextComponent("message.reusable_rockets.set_flight_duration", duration, maxDuration.get()), true);
            player.playSound(SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, 1F, 1F);
            return ActionResult.func_233538_a_(player.getHeldItem(hand), world.isRemote());
        } else if (player.isElytraFlying() && getUsesLeft(stack) > 0) {
            if (!Main.SERVER_CONFIG.allowRocketSpamming.get() && isGettingBoosted(player)) {
                return ActionResult.resultFail(player.getHeldItem(hand));
            }
            if (!world.isRemote) {
                int usesLeft = getUsesLeft(stack);
                int duration = Math.min(getFlightDuration(stack), usesLeft);
                world.addEntity(new FireworkRocketEntity(world, createDummyFirework((byte) duration), player));
                setUsesLeft(stack, Math.max(0, usesLeft - duration));
            }
            return ActionResult.func_233538_a_(player.getHeldItem(hand), world.isRemote());
        }
        return ActionResult.resultFail(player.getHeldItem(hand));

    }

    protected byte getFlightDuration(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (tag.contains("Duration")) {
            return tag.getByte("Duration");
        }
        return 1;
    }

    protected void setFlightDuration(ItemStack stack, byte duration) {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putByte("Duration", duration);
    }

    public void setUsesLeft(ItemStack stack, int usesLeft) {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putInt("UsesLeft", usesLeft);
    }

    public int getUsesLeft(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (tag.contains("UsesLeft")) {
            return tag.getInt("UsesLeft");
        }
        return 0;
    }

    public int getMaxUses() {
        return maxUses.get();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - Math.max(Math.min((double) getUsesLeft(stack) / maxUses.get().doubleValue(), 1D), 0D);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return TextFormatting.DARK_RED.getColor();
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("tooltip.reusable_rockets.flight_duration", getFlightDuration(stack), maxDuration.get()).mergeStyle(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("tooltip.reusable_rockets.uses", getUsesLeft(stack), maxUses.get()).mergeStyle(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("tooltip.reusable_rockets.sneak_to_change").mergeStyle(TextFormatting.GRAY));
        super.addInformation(stack, world, tooltip, flag);
    }

    protected ItemStack createDummyFirework(byte flightDuration) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.getOrCreateChildTag("Fireworks").putByte("Flight", flightDuration);
        return stack;
    }

    public boolean isGettingBoosted(PlayerEntity player) {
        return player.world.getEntitiesWithinAABB(FireworkRocketEntity.class, player.getBoundingBox().grow(2D), rocket -> {
            LivingEntity entity = null;
            try {
                entity = ObfuscationReflectionHelper.getPrivateValue(FireworkRocketEntity.class, rocket, "field_191513_e");
            } catch (Exception e) {
            }
            return entity == player;
        }).stream().findAny().isPresent();
    }

}
