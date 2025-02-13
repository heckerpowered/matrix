package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.LivingDamageCallback
import heckerpowered.matrix.common.event.LivingDamageEvent
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting

object RedstoneLeggingsItem : ArmorItem(
    redstoneArmorMaterial,
    Type.LEGGINGS,
    Settings()
        .maxDamage(Type.LEGGINGS.getMaxDamage(24))
        .component(redstoneSuitMaxPowerComponent, 20)
        .component(redstoneSuitPowerComponent, 0)
), RedstoneSuit {

    init {
        LivingDamageCallback.event.register(this::onLivingDamage)
    }

    private fun onLivingDamage(event: LivingDamageEvent): ActionResult {
        val entity = event.entity
        val leggings = entity.getEquippedStack(EquipmentSlot.LEGS)
        if (!leggings.isRedstoneSuit() || leggings.redstoneSuitPower <= 0) {
            return ActionResult.PASS
        }

        --leggings.redstoneSuitPower
        if ((0..100).random() in 0..33) {
            powerLeakage(entity)
        }

        return ActionResult.SUCCESS
    }

    private fun powerLeakage(entity: LivingEntity) {
        entity.world.getOtherEntities(entity, entity.boundingBox.expand(6.0)).forEach {
            it.damage(entity.damageSources.mobAttack(entity), 1.0f)
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType,
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        RedstoneSuit.appendTooltip(stack, context, tooltip, type)
        tooltip.add(MatrixLanguage.redstoneLeggingsDescription.copy().formatted(Formatting.GRAY, Formatting.ITALIC))
    }
}