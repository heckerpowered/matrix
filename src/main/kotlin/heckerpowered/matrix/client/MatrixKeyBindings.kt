package heckerpowered.matrix.client

import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object MatrixKeyBindings {
    val useMagic = KeyBinding(
        "key.matrix.use_magic",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_F,
        "key.categories.matrix"
    )

    val nextMagic = KeyBinding(
        "key.matrix.use_magic",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_DOWN,
        "key.categories.matrix"
    )

    val previousMagic = KeyBinding(
        "key.matrix.use_magic",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_UP,
        "key.categories.matrix"
    )

    val overclockMagic = KeyBinding(
        "key.matrix.overclock_magic",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        "key.categories.matrix"
    )

    val overclockMana = KeyBinding(
        "key.matrix.overclock_mana",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        "key.categories.matrix"
    )
}