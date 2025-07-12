package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.AccumulateAttributeValueCallback
import heckerpowered.matrix.common.event.LivingDeathCallback
import heckerpowered.matrix.common.item.MatrixComponents.BORROWED_TIME_CHARGE
import heckerpowered.matrix.common.item.MatrixComponents.BORROWED_TIME_MAX_CHARGE
import heckerpowered.matrix.common.item.MatrixComponents.BORROWED_TIME_STATE
import heckerpowered.matrix.common.network.ClientboundBorrowedTimePayload
import heckerpowered.matrix.common.network.TeleportPayload
import heckerpowered.matrix.core.Accumulator
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity
import net.minecraft.world.World

/**
 * Lightning Chestplate 1 'Warp Dancer'
 */
object LightningChestplate1 : ArmorItem(
    lightningArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .fireproof()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(37))
        .rarity(Rarity.EPIC)
        .component(BORROWED_TIME_CHARGE, 0)
        .component(BORROWED_TIME_MAX_CHARGE, 4000)
        .component(BORROWED_TIME_STATE, false)
) {
    init {
        LivingDeathCallback.EVENT.register(::onLivingDeath)
        AccumulateAttributeValueCallback.EVENT.register(::getAttributeValue)
    }

    fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator) {
        if (attribute == EntityAttributes.GENERIC_MOVEMENT_SPEED && entity is PlayerEntity && entity.isPhaseWalking) {
            accumulator.multiplier += 1
        }
    }

    private fun onLivingDeath(entity: LivingEntity, damageSource: DamageSource): ActionResult {
        val attacker = damageSource.attacker
        if (attacker !is PlayerEntity) {
            return ActionResult.PASS
        }

        val chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST)
        if (chestplate.item is LightningChestplate1) {
            val charge = chestplate.components.getOrDefault(BORROWED_TIME_CHARGE, 0)
            val maxCharge = chestplate.components.getOrDefault(BORROWED_TIME_MAX_CHARGE, 4000)
            chestplate.set(BORROWED_TIME_CHARGE, (charge + maxCharge / 10).coerceAtMost(maxCharge))
        }
        return ActionResult.PASS
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        if (entity !is PlayerEntity) {
            return
        }
        if (entity.getEquippedStack(EquipmentSlot.CHEST) != stack) {
            stack.set(BORROWED_TIME_STATE, false)
        }

        val charge = stack.components.getOrDefault(BORROWED_TIME_CHARGE, 0)
        val maxCharge = stack.components.getOrDefault(BORROWED_TIME_MAX_CHARGE, 4000)
        if (stack.components.getOrDefault(BORROWED_TIME_STATE, false)) {
            stack.set(BORROWED_TIME_CHARGE, (charge - 25).coerceAtLeast(0))
        } else {
            stack.set(BORROWED_TIME_CHARGE, (charge + 8).coerceAtMost(maxCharge))
        }

        if (world.isClient) {
            return
        }
        if (charge == 0L) {
            stack.set(BORROWED_TIME_STATE, false)
            if (entity is ServerPlayerEntity) {
                ServerPlayNetworking.send(entity, ClientboundBorrowedTimePayload(false))
                entity.server.playerManager.playerList.forEach {
                    ServerPlayNetworking.send(it, TeleportPayload(entity))
                }
            }
        }
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        super.appendTooltip(stack, context, tooltip, type)

        val charge = stack.components.getOrDefault(BORROWED_TIME_CHARGE, 0)
        val maxCharge = stack.components.getOrDefault(BORROWED_TIME_MAX_CHARGE, 4000)
        val percentage = (charge.toDouble() / maxCharge.toDouble()) * 100.0
        tooltip.add(MatrixLanguage.borrowedTimeChargeDescription.copy().append("${percentage.toLong()}%"))
    }

    @JvmStatic
    val PlayerEntity.isPhaseWalking: Boolean
        get() = getEquippedStack(EquipmentSlot.CHEST).components.getOrDefault(BORROWED_TIME_STATE, false)

    @JvmStatic
    val PlayerEntity.isBorrowedTime: Boolean
        get() = false // isPhaseWalking && world.server is IntegratedServer
}