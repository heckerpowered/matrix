package heckerpowered.matrix.common.magics

import net.minecraft.nbt.NbtCompound

open class MagicData(var isSpread: Boolean = false, var tag: NbtCompound = NbtCompound()) {
    companion object {
        const val IS_SPREAD_TAG = "IsSpread"
    }

    open fun readFromTag() {
        isSpread = tag.getBoolean(IS_SPREAD_TAG)
    }

    open fun writeToTag() {
        tag.putBoolean(IS_SPREAD_TAG, isSpread)
    }
}