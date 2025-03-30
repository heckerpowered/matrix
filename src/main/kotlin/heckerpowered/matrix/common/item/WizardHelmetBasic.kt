package heckerpowered.matrix.common.item

import net.minecraft.util.Rarity

object WizardHelmetBasic : WizardHelmet(
    8.0,
    Settings()
        .fireproof()
        .rarity(Rarity.COMMON)
        .component(MatrixComponents.maxLoad, 5.0)
)