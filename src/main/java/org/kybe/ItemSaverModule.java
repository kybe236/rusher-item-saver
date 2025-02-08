package org.kybe;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.accessors.packet.IMixinServerboundInteractPacket;
import org.rusherhack.client.api.events.network.EventPacket;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NullSetting;

import java.util.Optional;


public class ItemSaverModule extends ToggleableModule {
	// Attack
	NullSetting attack = new NullSetting("Attack");
	BooleanSetting attackSword = new BooleanSetting("Sword", true);
	BooleanSetting attackAxe = new BooleanSetting("Axe", true);
	BooleanSetting attackMace = new BooleanSetting("Mace", true);
	BooleanSetting attackBow = new BooleanSetting("Bow", true);
	BooleanSetting attackCrossbow = new BooleanSetting("Crossbow", true);

	// Interact
	NullSetting interact = new NullSetting("Interact");
	BooleanSetting interactShears = new BooleanSetting("Shears", true);
	BooleanSetting interactFishingRod = new BooleanSetting("Fishing Rod", true);
	BooleanSetting autoDisableAutoFish = new BooleanSetting("Auto Disable Auto Fish", true);

	// Pathing
	NullSetting pathing = new NullSetting("Pathing");
	BooleanSetting pathingShovel = new BooleanSetting("Shovel", true);
	BooleanSetting pathingAxe = new BooleanSetting("Axe", true);
	BooleanSetting pathingHoe = new BooleanSetting("Hoe", true);

	// Mining
	NullSetting mining = new NullSetting("Mining");
	BooleanSetting miningPickaxe = new BooleanSetting("Pickaxe", true);
	BooleanSetting miningAxe = new BooleanSetting("Axe", true);
	BooleanSetting miningShovel = new BooleanSetting("Shovel", true);
	BooleanSetting miningHoe = new BooleanSetting("Hoe", true);


	public ItemSaverModule() {
		super("Item Saver", "Stops you from breaking tools", ModuleCategory.CLIENT);

		// DONE
		attack.addSubSettings(
				attackSword, attackAxe, attackMace, attackBow, attackCrossbow
		);

		// DONE
		interact.addSubSettings(
				interactShears, interactFishingRod, autoDisableAutoFish
		);


		pathing.addSubSettings(
				pathingShovel, pathingAxe, pathingHoe
		);

		mining.addSubSettings(
				miningPickaxe, miningAxe, miningShovel, miningHoe
		);

		this.registerSettings(
				attack,
				interact,
				pathing,
				mining
		);
	}

	@Subscribe
	private void onPacketSend(EventPacket.Send event) {
		if (event.getPacket() instanceof ServerboundInteractPacket packet) {
			if (mc.player == null || mc.level == null) return;

			ItemStack stack = mc.player.getMainHandItem();
			Item selectedItem = stack.getItem();

			if (packet.isUsingSecondaryAction()) return;

			// Hitting a boat doesnt decrease durability
			Entity entity = mc.level.getEntity(((IMixinServerboundInteractPacket) packet).getEntityId());
			if (entity == null || entity instanceof Boat) return;

			if (selectedItem instanceof AxeItem axeItem && attackAxe.getValue()) {
				if (!isGonnaBreak(stack, 2)) return;
				event.setCancelled(true);
				notify(selectedItem.getName(stack).getString());
			}

			if (!isGonnaBreak(stack, 1)) return;

			switch (selectedItem) {
				case SwordItem swordItem when attackSword.getValue() -> {
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				case MaceItem maceItem when attackMace.getValue() -> {
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				case ShearsItem shearsItem when interactShears.getValue() -> {
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				case FishingRodItem fishingRodItem when interactFishingRod.getValue() -> {
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				default -> {
				}
			}
		} else if (event.getPacket() instanceof ServerboundUseItemPacket packet) {
			if (mc.player == null || mc.level == null) return;

			ItemStack stack = mc.player.getItemInHand(packet.getHand());
			Item selectedItem = stack.getItem();

			if (selectedItem instanceof CrossbowItem crossbowItem && attackCrossbow.getValue()) {
				if (!isGonnaBreak(stack, 1)) return;
				event.setCancelled(true);
				notify(selectedItem.getName(stack).getString());
			} else if (selectedItem instanceof BowItem bowItem && attackBow.getValue()) {
				if (!isGonnaBreak(stack, 1)) return;
				event.setCancelled(true);
				notify(selectedItem.getName(stack).getString());
			} else if (selectedItem instanceof FishingRodItem rod && interactFishingRod.getValue()) {
				if (mc.player.fishing != null) {
					Entity entity = mc.player.fishing.getHookedIn();
					if (entity == null) { // Only casting requires min 1 durability
						if (!isGonnaBreak(stack, 1)) return;
					} else if (entity instanceof ItemEntity) { // Reeling in requires min 3 durability
						if (!isGonnaBreak(stack, 3)) return;
					} else { // Reeling in requires min 5 durability
						if (!isGonnaBreak(stack, 5)) return;
					}
				} else { // Casting requires min 1 durability
					if (!isGonnaBreak(stack, 1)) return;
				}

				event.setCancelled(true);
				notify(selectedItem.getName(stack).getString());

				Optional<IModule> autoFish = RusherHackAPI.getModuleManager().getFeature("AutoFish");
				if (autoFish.isPresent() && autoDisableAutoFish.getValue()) {
					ToggleableModule autoFishModule = (ToggleableModule) autoFish.get();
					autoFishModule.setToggled(false);
				}
			}
		} else if (event.getPacket() instanceof ServerboundUseItemOnPacket packet) {
			if (mc.player == null || mc.level == null) return;

			ItemStack stack = mc.player.getItemInHand(packet.getHand());
			Item selectedItem = stack.getItem();

			if (!isGonnaBreak(stack, 1)) return;

			switch (selectedItem) {
				case ShovelItem shovelItem when pathingShovel.getValue() -> {
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				case AxeItem axeItem when pathingAxe.getValue() -> {
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				case HoeItem hoeItem when pathingHoe.getValue() -> {
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				default -> {
				}
			}
		} else if (event.getPacket() instanceof ServerboundPlayerActionPacket packet) {
			if (mc.player == null || mc.level == null) return;

			ItemStack stack = mc.player.getMainHandItem();
			Item selectedItem = stack.getItem();

			if (!isGonnaBreak(stack, 1)) return;
			if (packet.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return;

			switch (selectedItem) {
				case PickaxeItem pickaxeItem when miningPickaxe.getValue() -> {
					mc.gameMode.stopDestroyBlock();
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				case AxeItem axeItem when miningAxe.getValue() -> {
					mc.gameMode.stopDestroyBlock();
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				case ShovelItem shovelItem when miningShovel.getValue() -> {
					mc.gameMode.stopDestroyBlock();
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				case HoeItem hoeItem when miningHoe.getValue() -> {
					mc.gameMode.stopDestroyBlock();
					event.setCancelled(true);
					notify(selectedItem.getName(stack).getString());
				}
				default -> {
				}
			}
		}
	}

	public void notify(String item) {
		RusherHackAPI.getNotificationManager().info("Item Saver Cancelled use of " + item + " to prevent breaking");
	}

	public boolean isGonnaBreak(ItemStack stack, int possibleDmg) {
		return stack.getDamageValue() + possibleDmg >= stack.getMaxDamage();
	}
}
