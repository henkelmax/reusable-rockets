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

public class ItemReusableRocket extends Item {

    private final int maxDuration;

    public ItemReusableRocket(String name, int uses, int maxDuration) {
        super(new Properties().maxStackSize(1).group(ItemGroup.MISC).maxDamage(uses));
        this.maxDuration = maxDuration;
        setRegistryName(new ResourceLocation(Main.MODID, name));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            byte duration = getFlightDuration(stack);
            duration++;
            if (duration > maxDuration) {
                duration = 1;
            }
            setFlightDuration(stack, duration);
            player.sendStatusMessage(new TranslationTextComponent("message.reusable_rockets.set_flight_duration", duration, maxDuration), true);
            player.playSound(SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, 1F, 1F);
            return ActionResult.func_233538_a_(player.getHeldItem(hand), world.isRemote());
        } else if (player.isElytraFlying() && stack.getDamage() < stack.getMaxDamage()) {
            if (!Main.SERVER_CONFIG.allowRocketSpamming.get() && isGettingBoosted(player)) {
                return ActionResult.resultFail(player.getHeldItem(hand));
            }
            if (!world.isRemote) {
                int duration = Math.min(getFlightDuration(stack), stack.getMaxDamage() - stack.getDamage());
                world.addEntity(new FireworkRocketEntity(world, createDummyFirework((byte) duration), player));
                stack.setDamage(Math.min(stack.getMaxDamage(), stack.getDamage() + duration));
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

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add((new TranslationTextComponent("item.minecraft.firework_rocket.flight")).appendString(" ").appendString(String.valueOf(getFlightDuration(stack))).mergeStyle(TextFormatting.GRAY));
        tooltip.add((new TranslationTextComponent("tooltip.reusable_rockets.sneak_to_change")).mergeStyle(TextFormatting.GRAY));
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
