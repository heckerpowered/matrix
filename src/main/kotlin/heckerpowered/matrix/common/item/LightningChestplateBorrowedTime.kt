package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.LivingDeathCallback
import heckerpowered.matrix.common.network.ClientboundBorrowedTimePayload
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity
import net.minecraft.world.World

val PlayerEntity.borrowedTimeActive: Boolean
    get() = getEquippedStack(EquipmentSlot.CHEST).components.getOrDefault(borrowedTimeStateComponent, false)

/**
 * Lightning 'Borrowed Time' Chestplate
 */
object LightningChestplateBorrowedTime : ArmorItem(
    lightningArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .fireproof()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(37))
        .rarity(Rarity.EPIC)
        .component(borrowedTimeChargeComponent, 0)
        .component(borrowedTimeMaxChargeComponent, 4000)
        .component(borrowedTimeStateComponent, false)
) {
    init {
        LivingDeathCallback.EVENT.register(::onLivingDeath)
    }

    private fun onLivingDeath(entity: LivingEntity, damageSource: DamageSource): ActionResult {
        val attacker = damageSource.attacker
        if (attacker !is PlayerEntity) {
            return ActionResult.PASS
        }

        val chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST)
        if (chestplate.item is LightningChestplateBorrowedTime && attacker.borrowedTimeActive) {
            val charge = chestplate.components.getOrDefault(borrowedTimeChargeComponent, 0)
            val maxCharge = chestplate.components.getOrDefault(borrowedTimeMaxChargeComponent, 4000)
            chestplate.set(borrowedTimeChargeComponent, (charge + 50).coerceAtMost(maxCharge))
        }
        return ActionResult.PASS
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        if (entity !is PlayerEntity) {
            return
        }
        if (entity.getEquippedStack(EquipmentSlot.CHEST) != stack) {
            stack.set(borrowedTimeStateComponent, false)
        }

        val charge = stack.components.getOrDefault(borrowedTimeChargeComponent, 0)
        val maxCharge = stack.components.getOrDefault(borrowedTimeMaxChargeComponent, 4000)
        if (stack.components.getOrDefault(borrowedTimeStateComponent, false)) {
            stack.set(borrowedTimeChargeComponent, (charge - 25).coerceAtLeast(0))
        } else {
            stack.set(borrowedTimeChargeComponent, (charge + 8).coerceAtMost(maxCharge))
        }

        if (world.isClient) {
            return
        }
        if (charge == 0L) {
            stack.set(borrowedTimeStateComponent, false)
            if (entity is ServerPlayerEntity) {
                ServerPlayNetworking.send(entity, ClientboundBorrowedTimePayload(false))
            }
        }
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        super.appendTooltip(stack, context, tooltip, type)

        val charge = stack.components.getOrDefault(borrowedTimeChargeComponent, 0)
        val maxCharge = stack.components.getOrDefault(borrowedTimeMaxChargeComponent, 4000)
        val percentage = (charge.toDouble() / maxCharge.toDouble()) * 100.0
        tooltip.add(MatrixLanguage.borrowedTimeChargeDescription.copy().append("${percentage.toLong()}%"))
    }
}