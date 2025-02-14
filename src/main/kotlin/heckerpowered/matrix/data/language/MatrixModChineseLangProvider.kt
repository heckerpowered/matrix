package heckerpowered.matrix.data.language

import heckerpowered.matrix.common.effect.*
import heckerpowered.matrix.common.enchantment.witherArmorEnchantmentKey
import heckerpowered.matrix.common.item.*
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture


class MatrixModChineseLangProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricLanguageProvider(dataOutput, "zh_cn", registryLookup) {
    override fun generateTranslations(
        registryLookup: RegistryWrapper.WrapperLookup,
        translationBuilder: TranslationBuilder,
    ) {
        translationBuilder.add(MatrixLanguage.mana.key, "法力值")

        translationBuilder.add(MatrixLanguage.magicTargetPositioning.key, "目标定位")
        translationBuilder.add(
            MatrixLanguage.magicTargetPositioningDescription.key,
            "高亮目标及其周围24米内的生物，持续10秒。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicDecisiveStrike.key, "毁灭打击")
        translationBuilder.add(
            MatrixLanguage.magicDecisiveStrikeDescription.key,
            "对目标造成6点伤害，基于玩家基础攻击力造成额外伤害，附带目标最大生命值14%的伤害。\n\n引导队列中所有法术的每点占用增加1%伤害，最多至400%。\n\n移除目标的受击无敌时间。\n\n可被追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicManaOverload.key, "法力过载")
        translationBuilder.add(
            MatrixLanguage.magicManaOverloadDescription.key,
            "标记一个目标，持续10秒。标记持续期间内击杀目标回复20点法力值。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicHealthSteal.key, "生命偷取")
        translationBuilder.add(
            MatrixLanguage.magicHealthStealDescription.key,
            "将目标最大生命值的50%转为自身的额外生命值。\n\n转换值的50%用于恢复生命值、饥饿值和饱和度。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicExplosion.key, "原地爆炸")
        translationBuilder.add(
            MatrixLanguage.magicExplosionDescription.key,
            "在选定目标的位置产生一次爆炸。\n\n可被追踪。"
        )

        translationBuilder.add(MatrixLanguage.killMagic.key, "彻底抹除")
        translationBuilder.add(
            MatrixLanguage.killMagicDescription.key, "立即击杀目标。\n\n可被追踪。"
        )

        translationBuilder.add(MatrixLanguage.sculkCatalystMagic.key, "幽匿催发")
        translationBuilder.add(
            MatrixLanguage.sculkCatalystMagicDescription.key,
            "对目标造成20点伤害，若目标死亡，则自动对附近的下一个目标引导此法术。"
        )

        translationBuilder.add(MatrixLanguage.magicMemoryErase.key, "记忆擦除")
        translationBuilder.add(
            MatrixLanguage.magicMemoryEraseDescription.key,
            "强迫目标解除正在锁定的目标。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicIgniteMagic.key, "点燃")
        translationBuilder.add(
            MatrixLanguage.magicIgniteMagicDescription.key,
            "点燃目标10秒，造成持续伤害。\n\n如果目标处于中毒状态则引爆敌人。\n\n产生爆炸时可被追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicBreakingBad.key, "绝命毒师")
        translationBuilder.add(
            MatrixLanguage.magicBreakingBadDescription.key,
            "使目标中毒、失明，持续10秒。\n\n如果敌人正在点燃，则会引爆敌人。\n\n产生爆炸时可被追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicSpread.key, "连带传染")
        translationBuilder.add(
            MatrixLanguage.magicSpreadDescription.key,
            "排在此技能后的所有技能会传播给24米内的生物。\n\n无法传播。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicCrippleMovement.key, "阻碍移动")
        translationBuilder.add(
            MatrixLanguage.magicCrippleMovementDescription.key,
            "禁用目标的传送能力、阻碍目标移动，持续10秒。\n\n对抗玩家时，此技能的法力消耗会提升，效果也会更弱。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicSystemCrash.key, "系统崩溃")
        translationBuilder.add(
            MatrixLanguage.magicSystemCrashDescription.key,
            "仅对玩家有效，使目标玩家系统崩溃。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicLightningBoltMagic.key, "闪电旋风劈")
        translationBuilder.add(
            MatrixLanguage.magicLightningBoltMagicDescription.key,
            "在目标位置召唤闪电。\n\n不可追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicArmorPenetrationMagic.key, "护甲穿透")
        translationBuilder.add(
            MatrixLanguage.magicArmorPenetrationMagicDescription.key,
            "减少目标40%的护甲值，持续10秒。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicTeleport.key, "传送")
        translationBuilder.add(
            MatrixLanguage.magicTeleportDescription.key,
            "隐身10秒并传送到目标位置。\n\n无法追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicSonicBoom.key, "音波尖啸")
        translationBuilder.add(
            MatrixLanguage.magicSonicBoomDescription.key,
            "引导一次音波尖啸，对目标造成10点伤害。\n\n音波尖啸无视护甲值、任何可减伤的魔咒（如保护）和盾牌的阻挡，不能触发荆棘魔咒，且对女巫造成的伤害降低85%。\n\n凋零护甲可减免音波尖啸造成的伤害。\n\n可被追踪。"
        )

        translationBuilder.add(MatrixLanguage.magicBruteForce.key, "大力出奇迹")
        translationBuilder.add(MatrixLanguage.magicBruteForceDescription.key, "使目标进入危险状态，持续10秒。\n\n危险状态将使目标受到的伤害增加100%，每一效果等级进一步增加100%\n\n无法追踪。")

        translationBuilder.add(MatrixLanguage.magicBloodPact.key, "血之契约")
        translationBuilder.add(MatrixLanguage.magicBloodPactDescription.key, "诅咒目标，使你所受到的50%伤害转移到目标身上，转移的伤害没有上限。\n\n若目标无法承受转移的伤害，则转移的数额会降低。\n\n若有多个目标被诅咒，则所有目标共同承受转移的伤害。\n\n只要诅咒存在，效果就会持续。\n\n")

        translationBuilder.add(MatrixLanguage.overclockMagic.key, "超频或降频魔法")
        translationBuilder.add(MatrixLanguage.overclockMana.key, "超频或降频法力")
        translationBuilder.add(MatrixLanguage.switchClock.key, "切换超频或降频")
        translationBuilder.add(MatrixLanguage.systemCrashing.key, "即将发生系统崩溃")

        translationBuilder.add(MatrixLanguage.magicAvailable.key, "就绪")
        translationBuilder.add(MatrixLanguage.magicAvailableManaNotEnough.key, "可用法力不足")
        translationBuilder.add(MatrixLanguage.magicUnavailable.key, "无法引导法术")
        translationBuilder.add(MatrixLanguage.magicChannelQueueFull.key, "队列已满")
        translationBuilder.add(MatrixLanguage.magicChannelQueueLocked.key, "队列已锁定")
        translationBuilder.add(MatrixLanguage.magicTargetMissing.key, "目标缺失")
        translationBuilder.add(MatrixLanguage.magicTargetImmune.key, "目标免疫")

        translationBuilder.add(ArmorPenetrationEffect, "护甲穿透")
        translationBuilder.add(ManaOverloadEffect, "法力过载")
        translationBuilder.add(CrippleMovementEffect, "阻碍移动")
        translationBuilder.add(WitherArmorChargedEffect, "凋灵护甲充能")
        translationBuilder.add(WitherArmorEffect, "凋灵护甲")
        translationBuilder.add(AngeredEffect, "狂暴")

        translationBuilder.add(witherArmorEnchantmentKey, "凋灵护甲")

        translationBuilder.add(WardenChestplateItem, "幽匿“坚守”胸甲")
        translationBuilder.add(
            MatrixLanguage.wardenChestplateDescription.key,
            """
                §7狂暴时：§r
                对§9伤害§r、§9击退§r、§9火焰§r、§9移速惩罚§r免疫。
                立即清除负面效果并在持续时间内免疫任何负面效果。
                造成的伤害提升§9100%§r。
                -§9100%§r近战武器攻击蓄力时间。
                移动速度获得提升。
            """.trimIndent()
        )

        translationBuilder.add(itemGroupKey, "Matrix")

        translationBuilder.add(MatrixLanguage.redstoneSuitPower.key, "电力: ")

        translationBuilder.add(RedstoneHelmetItem, "红石头盔")
        translationBuilder.add(
            MatrixLanguage.redstoneHelmetDescription.key,
            "电解呼吸: 在水下时消耗电量来呼吸，每§95§r秒消耗§91§r单位电力。"
        )

        translationBuilder.add(RedstoneChestplateItem, "红石胸甲")
        translationBuilder.add(
            MatrixLanguage.redstoneChestplateDescription.key,
            "消耗电量减少受到的伤害，每1单位电力可减少4点伤害，每次至多减免40%。"
        )
        translationBuilder.add(RedstoneLeggingsItem, "红石护腿")
        translationBuilder.add(
            MatrixLanguage.redstoneLeggingsDescription.key,
            "受到伤害时有概率对附近的生物造成伤害。"
        )
        translationBuilder.add(RedstoneBootsItem, "红石靴子")
        translationBuilder.add(RedstoneSwordItem, "红石剑")
        translationBuilder.add(RedstonePickaxeItem, "红石镐")
        translationBuilder.add(RedstoneAxeItem, "红石斧")
        translationBuilder.add(RedstoneShovelItem, "红石铲")
        translationBuilder.add(RedstoneHoeItem, "红石锄")
        translationBuilder.add(MatrixLanguage.redstoneSwordDescription.key, "攻击时消耗1电力造成额外2点伤害。")
        translationBuilder.add(
            MatrixLanguage.redstoneMiningToolDescription.key,
            "挖掘速度提升40%，破坏方块消耗1EMU电力。"
        )

        translationBuilder.add(LapisLazuliHelmetItem, "青金石头盔")
        translationBuilder.add(LapisLazuliChestplateItem, "青金石头盔")
        translationBuilder.add(LapisLazuliLeggingsItem, "青金石护腿")
        translationBuilder.add(LapisLazuliBootsItem, "青金石靴子")
        translationBuilder.add(LapisLazuliSwordItem, "青金石剑")
        translationBuilder.add(LapisLazuliPickaxeItem, "青金石镐")
        translationBuilder.add(LapisLazuliAxeItem, "青金石斧")
        translationBuilder.add(LapisLazuliShovelItem, "青金石铲")
        translationBuilder.add(LapisLazuliHoeItem, "青金石锄")

        translationBuilder.add(EmeraldHelmetItem, "绿宝石头盔")
        translationBuilder.add(EmeraldChestplateItem, "绿宝石胸甲")
        translationBuilder.add(EmeraldLeggingsItem, "绿宝石护腿")
        translationBuilder.add(EmeraldBootsItem, "绿宝石靴子")
        translationBuilder.add(EmeraldSwordItem, "绿宝石剑")
        translationBuilder.add(EmeraldPickaxeItem, "绿宝石镐")
        translationBuilder.add(EmeraldAxeItem, "绿宝石斧")
        translationBuilder.add(EmeraldShovelItem, "绿宝石铲")
        translationBuilder.add(EmeraldHoeItem, "绿宝石锄")

        translationBuilder.add(CoalHelmetItem, "煤炭头盔")
        translationBuilder.add(CoalChestplateItem, "煤炭胸甲")
        translationBuilder.add(CoalLeggingsItem, "煤炭护腿")
        translationBuilder.add(CoalBootsItem, "煤炭靴子")
        translationBuilder.add(CoalSwordItem, "煤炭剑")
        translationBuilder.add(CoalPickaxeItem, "煤炭镐")
        translationBuilder.add(CoalAxeItem, "煤炭斧")
        translationBuilder.add(CoalShovelItem, "煤炭铲")
        translationBuilder.add(CoalHoeItem, "煤炭锄")

        translationBuilder.add(StoneHelmetItem, "石头盔")
        translationBuilder.add(StoneChestplateItem, "石胸甲")
        translationBuilder.add(StoneLeggingsItem, "石护腿")
        translationBuilder.add(StoneBootsItem, "石靴子")

        translationBuilder.add(WoodenHelmetItem, "木头盔")
        translationBuilder.add(WoodenChestplateItem, "木胸甲")
        translationBuilder.add(WoodenLeggingsItem, "木护腿")
        translationBuilder.add(WoodenBootsItem, "木靴子")

        // Potions
        translationBuilder.add("item.minecraft.potion.effect.angered", "狂暴药水")
        translationBuilder.add("item.minecraft.slash_potion.effect.angered", "喷溅型狂暴药水")
        translationBuilder.add("item.minecraft.lingering_potion.effect.angered", "滞留型狂暴药水")
    }
}