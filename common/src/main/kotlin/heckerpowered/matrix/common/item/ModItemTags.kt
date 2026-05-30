/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object ModItemTags {
    val wizardHelmetTag = bind("wizard_helmet")

    val repairsWardenArmor = bind("repairs_warden_armor")
    val repairsRedstoneArmor = bind("repairs_redstone_armor")
    val repairsLapisLazuliArmor = bind("repairs_lapis_lazuli_armor")
    val repairsEmeraldArmor = bind("repairs_emerald_armor")
    val repairsCoalArmor = bind("repairs_coal_armor")
    val repairsStoneArmor = bind("repairs_stone_armor")
    val repairsWoodenArmor = bind("repairs_wooden_armor")
    val repairsWizardArmor = bind("repairs_wizard_armor")
    val repairsLightningArmor = bind("repairs_lightning_armor")

    private fun bind(id: String): TagKey<Item> {
        return TagKey.create(Registries.ITEM, Matrix.identifier(id))
    }
}
