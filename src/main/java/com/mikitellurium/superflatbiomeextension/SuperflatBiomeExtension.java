package com.mikitellurium.superflatbiomeextension;

import com.mikitellurium.superflatbiomeextension.registry.ModRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.component.ComponentPredicateTypes;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperflatBiomeExtension implements ModInitializer {
	private static final String MOD_ID = "superflatbiomeextension";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModRegistries.register();
		ServerEntityEvents.ENTITY_LOAD.register(((entity, world) -> {
			if (entity instanceof ServerPlayerEntity player) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 50));
				ItemStack itemStack = new ItemStack(Items.DIAMOND_BOOTS);
				RegistryEntryLookup<Enchantment> entryLookup = player.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
				RegistryEntry<Enchantment> enchantment = entryLookup.getOrThrow(Enchantments.DEPTH_STRIDER);
				EnchantmentHelper.apply(itemStack, (builder) -> builder.add(enchantment, 3));
				player.equipStack(EquipmentSlot.FEET, itemStack);
			}
		}));
	}

	public static String modId() {
		return MOD_ID;
	}

	public static Logger logger() {
		return LOGGER;
	}
}