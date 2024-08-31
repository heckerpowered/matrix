package heckerpowered.matrix.client

import heckerpowered.matrix.client.network.MatrixClientPlayNetworking
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.magics.DecisiveStrikeMagic
import heckerpowered.matrix.common.magics.HealthStealMagic
import heckerpowered.matrix.common.magics.ManaOverloadMagic
import heckerpowered.matrix.common.magics.TargetPositioningMagic
import net.fabricmc.api.ClientModInitializer


class MatrixClient : ClientModInitializer {
    override fun onInitializeClient() {
        MatrixHud.onInitialize()
        MatrixClientPlayNetworking.onInitialize()
    }

    companion object {
        fun getPlayerMagics(): List<Magic> {
            return listOf(
                TargetPositioningMagic(),
                DecisiveStrikeMagic(),
                HealthStealMagic(),
                ManaOverloadMagic()
            )
        }
    }
}