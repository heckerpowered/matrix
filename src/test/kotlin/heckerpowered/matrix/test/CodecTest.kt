/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.test

import heckerpowered.matrix.common.persistent.serialization.NbtCodec
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.minecraft.util.Rarity
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
data class SpellSlot(
    val name: String,
    val cost: Int = 10,
    val tags: List<String> = listOf("basic"),
    val props: Map<String, Int> = mapOf("level" to 1),
    val rarity: Rarity = Rarity.RARE,
    val icon: Char = '★',
    val note: String? = null,
)

@Serializable
sealed interface MagicCfg {
    val id: String
}

@Serializable
@SerialName("damage")
data class DamageCfg(override val id: String, val amount: Int) : MagicCfg

@Serializable
@SerialName("teleport")
data class TeleportCfg(override val id: String, val range: Int) : MagicCfg

class CodecTest {
    @Test
    fun `round-trip`() {
        val original = SpellSlot(
            name = "Fireball",
            cost = 50,
            tags = listOf("aoe", "fire"),
            props = mapOf("radius" to 4, "ignite" to 1),
            rarity = Rarity.EPIC,
            icon = '♨',
            note = null
        )

        val tag = NbtCodec.encode(original)
        val back = NbtCodec.decode<SpellSlot>(tag)
        assertEquals(original, back)
    }

    @Test
    fun `default values and missing fields`() {
        // cost=10, tags=["basic"], props={"level":1}, rarity=RARE, icon='★', note=null
        val original = SpellSlot(name = "IceShard")
        val tag = NbtCodec.encode(original)
        val back = NbtCodec.decode<SpellSlot>(tag)
        assertEquals(original, back)
    }

    @Test
    fun `list with null should fail`() {
        @Serializable
        data class BadList(val xs: List<String?>)

        val value = BadList(xs = listOf("ok", null, "still"))
        val ex = assertThrows<IllegalStateException> {
            NbtCodec.encode(value)
        }
        assertEquals(ex.message?.contains("List does not support null"), true)
    }

    @Test
    fun `polymorphic config round-trip`() {
        val module = SerializersModule {
            polymorphic(MagicCfg::class) {
                subclass(DamageCfg::class)
                subclass(TeleportCfg::class)
            }
        }
        val codec = NbtCodec(module)

        val items: List<MagicCfg> = listOf(
            DamageCfg("decisive_strike", 42),
            TeleportCfg("blink", 16)
        )

        val tag = NbtCodec.encode(items, codec, ListSerializer(PolymorphicSerializer(MagicCfg::class)))
        val back = NbtCodec.decode(tag, codec, ListSerializer(PolymorphicSerializer(MagicCfg::class)))

        assertEquals(items, back)
    }

    @Test
    fun `map key as enum name and value as object`() {
        val m = mapOf(
            "slotA" to SpellSlot("Zap", cost = 5),
            "slotB" to SpellSlot("Blink", cost = 1, tags = emptyList(), props = emptyMap(), rarity = Rarity.COMMON)
        )
        val tag = NbtCodec.encode(m, serializer = MapSerializer(String.serializer(), SpellSlot.serializer()))
        val back = NbtCodec.decode(tag, deserializer = MapSerializer(String.serializer(), SpellSlot.serializer()))
        assertEquals(m, back)
    }

    @Serializable
    data class Inner(val x: Int, val tags: List<String>)

    @Serializable
    data class Outer(val name: String, val inner: Inner, val items: List<Inner>)

    @Test
    fun `object with nested list and map`() {
        val v = Outer(
            name = "pack",
            inner = Inner(1, listOf("a", "b")),
            items = listOf(Inner(2, listOf("x")), Inner(3, emptyList()))
        )
        val tag = NbtCodec.encode(v)
        val back = NbtCodec.decode<Outer>(tag)
        assertEquals(v, back)
    }

    @Test
    fun `list of maps and map of lists`() {
        val mapOfLists = mapOf("a" to listOf(1, 2), "b" to emptyList())
        val listOfMaps = listOf(mapOf("k1" to 1), mapOf(), mapOf("k2" to 2))
        val tag1 = NbtCodec.encode(mapOfLists, serializer = MapSerializer(String.serializer(), ListSerializer(Int.serializer())))
        val back1 = NbtCodec.decode(tag1, deserializer = MapSerializer(String.serializer(), ListSerializer(Int.serializer())))
        assertEquals(mapOfLists, back1)

        val tag2 = NbtCodec.encode(listOfMaps, serializer = ListSerializer(MapSerializer(String.serializer(), Int.serializer())))
        val back2 = NbtCodec.decode(tag2, deserializer = ListSerializer(MapSerializer(String.serializer(), Int.serializer())))
        assertEquals(listOfMaps, back2)
    }

    @Test
    fun `polymorphic in list and in map`() {
        val module = SerializersModule {
            polymorphic(MagicCfg::class) {
                subclass(DamageCfg::class)
                subclass(TeleportCfg::class)
            }
        }
        val codec = NbtCodec(module)

        val list = listOf(DamageCfg("a", 1), TeleportCfg("b", 2))
        val tagL = NbtCodec.encode(list, codec, ListSerializer(PolymorphicSerializer(MagicCfg::class)))
        val backL = NbtCodec.decode(tagL, codec, ListSerializer(PolymorphicSerializer(MagicCfg::class)))
        assertEquals(list, backL)

        val map = mapOf("A" to DamageCfg("x", 9), "B" to TeleportCfg("y", 8))
        val ser = MapSerializer(String.serializer(), PolymorphicSerializer(MagicCfg::class))
        val tagM = NbtCodec.encode(map, codec, ser)
        val backM = NbtCodec.decode(tagM, codec, ser)
        assertEquals(map, backM)
    }

    @Test
    fun `empty collections & defaults`() {
        val s = SpellSlot("Empty", cost = 0, tags = emptyList(), props = emptyMap(), rarity = Rarity.COMMON)
        val tag = NbtCodec.encode(s)
        val back = NbtCodec.decode<SpellSlot>(tag)
        assertEquals(s, back)

        val emptyMapTag = NbtCodec.encode(emptyMap<String, Int>(), serializer = MapSerializer(String.serializer(), Int.serializer()))
        val backMap = NbtCodec.decode(emptyMapTag, deserializer = MapSerializer(String.serializer(), Int.serializer()))
        assertTrue(backMap.isEmpty())
    }

    @JvmInline
    @Serializable
    value class Meter(val v: Int)

    @Serializable
    data class WithInline(val id: String, val len: Meter, @SerialName("nick") val name: String, val ch: Char)

    @Test
    fun `inline value, SerialName and unicode char`() {
        val v = WithInline("x", Meter(12), "昵称-✓", 'x')
        val tag = NbtCodec.encode(v)
        val back = NbtCodec.decode<WithInline>(tag)
        assertEquals(v, back)
    }

    @Serializable
    data class Floats(val a: Float, val b: Double)

    @Test
    fun `float specials`() {
        val v = Floats(Float.NaN, Double.POSITIVE_INFINITY)
        val tag = NbtCodec.encode(v)
        val back = NbtCodec.decode<Floats>(tag)
        assertTrue(back.a.isNaN())
        assertEquals(Double.POSITIVE_INFINITY, back.b)
    }

    @Test
    fun `root list round-trip`() {
        val xs = listOf("a", "b", "c")
        val tag = NbtCodec.encode(xs, serializer = ListSerializer(String.serializer()))
        val back = NbtCodec.decode(tag, deserializer = ListSerializer(String.serializer()))
        assertEquals(xs, back)
    }

    @Test
    fun `root map round-trip named`() {
        val m = mapOf("k1" to 1, "k2" to 2)
        val ser = MapSerializer(String.serializer(), Int.serializer())
        val tag = NbtCodec.encode(m, serializer = ser)
        val back = NbtCodec.decode(tag, deserializer = ser)
        assertEquals(m, back)
    }

    @Test
    fun `unknown field tolerated`() {
        val s = SpellSlot("Fireball", 50, listOf("aoe"), mapOf("radius" to 3), Rarity.EPIC, '♨')
        val tag = NbtCodec.encode(s)
        tag.putInt("extra_field", 123)
        val back = NbtCodec.decode<SpellSlot>(tag)
        assertEquals("Fireball", back.name)
    }
}