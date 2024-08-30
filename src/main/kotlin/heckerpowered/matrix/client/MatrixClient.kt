package heckerpowered.matrix.client

import heckerpowered.matrix.common.Magic
import net.fabricmc.api.ClientModInitializer
import net.minecraft.text.Text


class MatrixClient : ClientModInitializer {
    override fun onInitializeClient() {
        MatrixHud.onInitialize()
    }

    companion object {
        fun getPlayerMagics(): List<Magic> {
            return listOf(
                Magic(Text.literal("目标定位"), 2),
                Magic(Text.literal("杀妈之术"), 6),
                Magic(Text.literal("除你武器"), 10),
                Magic(Text.literal("顷刻炼化"), 10),
                Magic(Text.literal("毁灭打击"), 10),
                Magic(Text.literal("缓慢药水"), 2),
                Magic(Text.literal("瞬间伤害"), 10),
            )
        }
    }
}