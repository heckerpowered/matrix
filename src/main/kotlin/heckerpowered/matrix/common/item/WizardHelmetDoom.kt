package heckerpowered.matrix.common.item

import net.minecraft.util.Rarity

object WizardHelmetDoom : WizardHelmet(
    9.0,
    Settings()
        .fireproof()
        .rarity(Rarity.COMMON)
        .component(MatrixComponents.maxLoad, 10.0)
)