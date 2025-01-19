package heckerpowered.matrix.data.language

import heckerpowered.matrix.common.effect.*
import heckerpowered.matrix.common.enchantment.witherArmorEnchantmentKey
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture


class MatrixModChineseLangProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricLanguageProvider(dataOutput, "zh_cn", registryLookup) {
    override fun generateTranslations(
        registryLookup: RegistryWrapper.WrapperLookup,
        translationBuilder: TranslationBuilder
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

        translationBuilder.add(witherArmorEnchantmentKey, "凋灵护甲")
    }
}