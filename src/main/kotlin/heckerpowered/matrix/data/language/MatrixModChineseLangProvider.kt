/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.language

import heckerpowered.matrix.common.effect.*
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.BLOOD_PACT_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.BRUTAL_STRENGTH_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.GUARANTEED_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LAST_STAND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LIGHTNING_STRIKE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_QUEUE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_SHIELD_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_OVERFLOW_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_REGENERATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PEAK_OVERDRIVE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PROXIMATE_PROPAGATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.QUEUE_ACCELERATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.QUEUE_MASTERY_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.REVIVAL_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.SECOND_WIND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.WITHER_ARMOR_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.WIZARD_FORCE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.item.*
import heckerpowered.matrix.common.magic.*
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

        translationBuilder.add(TargetPositioningMagic.definition.name.key, "目标定位")
        translationBuilder.add(
            TargetPositioningMagic.definition.description.key,
            "高亮目标及其周围§a24§r米内的生物，持续§a10§r秒。\n\n无法追踪。"
        )

        translationBuilder.add(DecisiveStrikeMagic.definition.name.key, "毁灭打击")
        translationBuilder.add(
            DecisiveStrikeMagic.definition.description.key,
            "对目标造成§a6§r点伤害，基于玩家基础攻击力造成额外伤害，附带目标最大生命值§a14§r%的伤害。\n\n引导队列中所有法术的每点占用增加§a1§r%伤害，最多至§a400§r%。血之契约激活时，\n\n伤害增加100%。\n\n移除目标的受击无敌时间。\n\n可被追踪。"
        )

        translationBuilder.add(ManaOverloadMagic.definition.name.key, "法力过载")
        translationBuilder.add(
            ManaOverloadMagic.definition.description.key,
            "使用法力过载目标，使目标的法术能力失效。\n\n对受到影响的敌人造成的伤害+§a15§r%。\n\n§c压制§r末影人的§d传送§r及§d闪避§r能力。\n§c压制§r坚守者引导§9音波尖啸§r的能力。\n§c压制§r灾厄村民的§c所有法术§r。\n§c压制§r守卫者的§9激光攻击§r及远古守卫者赋予玩家§7挖掘疲劳§r效果的能力。\n使女巫§c无法丢出药水§r。\n\n§c压制§r会立即中断并在效果持续时间内阻止其使用相关能力。\n\n叠加§a2§r层时移除并屏蔽目标身上的有益效果。\n\n叠加§a3§r层时造成§9持续性法术伤害§r。\n\n叠加到§c最大§r时，对目标造成其§a100§r%当前生命值的§9法术伤害§r，效果结束前无法再叠加。\n\n可被追踪。"
        )

        translationBuilder.add(HealthStealMagic.definition.name.key, "生命偷取")
        translationBuilder.add(
            HealthStealMagic.definition.description.key,
            "将目标§c最大生命值§r的§a50§r%转为自身的§6额外生命值§r。\n\n转换值的§a50§r%用于恢复§c生命值§r、§e饥饿值§r和§e饱和度§r。\n\n通过此种方式获得的§6额外生命值§r不能超过你的§c最大生命值§r。\n\n无法追踪。"
        )

        translationBuilder.add(ExplosionMagic.definition.name.key, "原地爆炸")
        translationBuilder.add(
            ExplosionMagic.definition.description.key,
            "在选定目标的位置产生一次§c爆炸§r。\n\n§c爆炸§r的威力为§a4§r。\n\n§c爆炸§r造成的伤害视为§9法术伤害§r。\n\n§c爆炸§r不会破坏方块，也不会生成§c火焰§r。\n\n可被追踪。"
        )

        translationBuilder.add(KillMagic.definition.name.key, "彻底抹除")
        translationBuilder.add(
            KillMagic.definition.description.key,
            "立即击杀目标。\n\n可被追踪。"
        )

        translationBuilder.add(SculkCatalystMagic.definition.name.key, "幽匿催发")
        translationBuilder.add(
            SculkCatalystMagic.definition.description.key,
            "对目标造成致命伤害，在成功击杀目标时会散布到§a25§r米内的§a5§r名敌人上。每次散布时自动消耗法力，每次散布的引导时间会越来越短，消耗的法力越来越多，但只要你愿意付出血的代价，消耗也可以减半⋯\n\n可被追踪。\n\n§7§o“和我的代码说去吧”§r"
        )

        translationBuilder.add(MemoryWipeMagic.definition.name.key, "记忆擦除")
        translationBuilder.add(
            MemoryWipeMagic.definition.description.key,
            "强迫目标解除正在锁定的目标。\n\n排在此法术后面的法术将无法被追踪。\n\n排在记忆擦除后的法术造成的伤害不认为是你造成的，无法触发只对你有效的相关效果。\n\n无法追踪。"
        )

        translationBuilder.add(IgniteMagic.definition.name.key, "点燃")
        translationBuilder.add(
            IgniteMagic.definition.description.key,
            "§c点燃§r目标§a5§r秒，造成持续伤害。\n\n对已经受到§c点燃§r影响的敌人引导§c点燃§r会延长效果的持续时间至§a8§r秒。\n\n§c融化§r敌人的护甲，减少目标§a40§r%的护甲和韧性。\n\n如果目标处于§2中毒§r状态则§c引爆§r敌人。\n\n§c爆炸§r的威力为§a4§r。\n\n§c爆炸§r造成的伤害视为§9法术伤害§r。\n\n§c爆炸§r不会破坏方块，也不会生成§c火焰§r。\n\n可被追踪。"
        )

        translationBuilder.add(BreakingBadMagic.definition.name.key, "绝命毒师")
        translationBuilder.add(
            BreakingBadMagic.definition.description.key,
            "使目标§2中毒§r、失明，持续§a5§r秒。\n\n可以散布到§a8§r米内的§a4§r名敌人身上。\n\n如果敌人处于§c点燃§r状态，则会§c引爆§r敌人。\n\n§c爆炸§r的威力为§a4§r。\n\n§c爆炸§r造成的伤害视为§9法术伤害§r。\n\n§c爆炸§r不会破坏方块，也不会生成§c火焰§r。\n\n可被追踪。"
        )

        translationBuilder.add(SpreadMagic.definition.name.key, "连带传染")
        translationBuilder.add(
            SpreadMagic.definition.description.key,
            "排在此技能后的所有技能会传播给§a24§r米内的生物。\n\n传播时自动消耗法力。\n\n此技能本身无法被传播。\n\n无法追踪。"
        )

        translationBuilder.add(CrippleMovementMagic.definition.name.key, "阻碍移动")
        translationBuilder.add(
            CrippleMovementMagic.definition.description.key,
            "禁用目标的§d传送能力§r、阻碍目标移动，持续§a10§r秒。\n\n对抗玩家时，此技能的法力消耗会提升，效果也会更弱。\n\n无法追踪。"
        )

        translationBuilder.add(SystemCrashMagic.definition.name.key, "系统崩溃")
        translationBuilder.add(
            SystemCrashMagic.definition.description.key,
            "仅对玩家有效，使目标玩家系统崩溃。\n\n无法追踪。"
        )

        translationBuilder.add(AttractMagic.definition.name.key, "无形的大手")
        translationBuilder.add(
            AttractMagic.definition.description.key,
            "在目标位置创建一只§c无形的大手§r，持续§a6§r秒。\n\n持续牵引附近§a6§r米内的实体到其位置。\n\n此法术不会牵引施法者。\n\n无法追踪。"
        )

        translationBuilder.add(LightningBoltMagic.definition.name.key, "闪电旋风劈")
        translationBuilder.add(
            LightningBoltMagic.definition.description.key,
            """
        在目标位置召唤闪电，闪电可能具有不同颜色，不同颜色的闪电具有不同的效果。
        
        §4红色闪电§r对命中实体造成§a400§r%伤害。
        
        §c橙色闪电§r将移除命中实体所有护甲，持续§a10§r秒。
        
        §e黄色闪电§r将高亮命中的实体。无论距离目标多远，立即蓄力手持物品，并攻击目标一次。
        
        §a绿色闪电§r在命中时治疗你1颗心，并使命中目标§2中毒§r，持续§a10§r秒。
        
        §9青色闪电§r将为命中目标自动引导§9阻碍移动§r，不消耗额外法力。对于已处在此状态的目标，同时使其进入§c危险§r状态，持续§a10§r秒。
        
        §1蓝色闪电§r在命中时引导此魔法。
        
        §a紫色闪电§r的影响范围变大§a100§r%，在每次命中时造成小范围爆炸，并使目标进入“引雷”状态，持续§a180§r秒。
        
        白色闪电无特殊效果。
        
        无色闪电将立即击杀所有命中实体。
        
        无色闪电的抽取概率为§a0.6§r%，其他所有颜色抽取概率均分。
        由玩家引导的闪电与自然闪电有不同的行为，例如不会充能苦力怕，伤害不随难度变化，也不会触发成就。
        此魔法排在记忆擦除后会导致有关你的效果无效，例如无法治疗你。
        连续释放法力消耗-§a20§r%，可叠加，至多至§a80§r%。
        
        不可追踪。
    """.trimIndent()
        )

        translationBuilder.add(ArmorPenetrationMagic.definition.name.key, "护甲穿透")
        translationBuilder.add(
            ArmorPenetrationMagic.definition.description.key,
            "移除目标§a40§r%护甲和韧性，持续10秒。\n\n相关的效果可被牛奶清除。\n\n无法追踪。"
        )

        translationBuilder.add(TeleportMagic.definition.name.key, "传送")
        translationBuilder.add(
            TeleportMagic.definition.description.key,
            "传送到目标位置，并自动攻击附近3米内的生物。\n\n可被追踪。"
        )

        translationBuilder.add(SonicBoomMagic.definition.name.key, "音波尖啸")
        translationBuilder.add(
            SonicBoomMagic.definition.description.key,
            "引导一次音波尖啸，对目标造成10点伤害。\n\n音波尖啸无视护甲值、任何可减伤的魔咒（如保护）和盾牌的阻挡，不能触发荆棘魔咒，且对女巫造成的伤害降低85%。\n\n凋零护甲可减免音波尖啸造成的伤害。\n\n可被追踪。"
        )

        translationBuilder.add(BruteForceMagic.definition.name.key, "力大砖飞")
        translationBuilder.add(
            BruteForceMagic.definition.description.key,
            "使目标进入§c危险§r状态，持续§a10§r秒。\n\n危险状态将使目标受到的伤害增加100%，每一效果等级进一步增加100%\n\n相关效果可被牛奶清除。\n\n无法追踪。"
        )

        translationBuilder.add(LevitationMagic.definition.name.key, "装逼让你飞起来")
        translationBuilder.add(
            LevitationMagic.definition.description.key,
            "使目标进入漂浮状态，持续§a10§r秒。\n\n可叠加，叠加将提升效果等级并重置效果持续时间，没有上限。\n\n无法追踪。"
        )

        translationBuilder.add(AbsolvriftMagic.definition.name.key, "无赦界裂")
        translationBuilder.add(
            AbsolvriftMagic.definition.description.key,
            "对目标及其§a6m§r范围内的目标造成一次伤害，相当于你§a100%§r的攻击力。\n\n在接下来20秒内，自动攻击§a8§r米内至多§a5§r个目标，按距离排序，伤害相当于你§a100%§r的攻击力，每秒一次。\n\n自动攻击会使目标在接下来§a8§r秒内的生命上限减少§a2.5%§r，可叠加，至多至§a50%§r。\n\n造成伤害时自动追加一次攻击，伤害相当于你75%攻击力，视为自动攻击。\n\n可被追踪。"
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
        translationBuilder.add(MatrixLanguage.magicSculkCatalystIsAlreadyActive.key, "幽匿催发已经激活")

        translationBuilder.add(ArmorPenetrationEffect, "护甲穿透")
        translationBuilder.add(ManaOverloadEffect, "法力过载")
        translationBuilder.add(CrippleMovementEffect, "阻碍移动")
        translationBuilder.add(WitherArmorChargedEffect, "凋灵护甲充能")
        translationBuilder.add(WitherArmorEffect, "凋灵护甲")
        translationBuilder.add(AngeredEffect, "狂暴")
        translationBuilder.add(BloodPactEffect, "血之契约")
        translationBuilder.add(BorrowedTimeEffect, "时不我待")

        translationBuilder.add(WITHER_ARMOR_ENCHANTMENT_KEY, "凋灵护甲")
        translationBuilder.add(GUARANTEED_ENCHANTMENT_KEY, "稳操胜券")
        translationBuilder.add(LAST_STAND_ENCHANTMENT_KEY, "绝处逢生")
        translationBuilder.add(REVIVAL_ENCHANTMENT_KEY, "复苏")
        translationBuilder.add(SECOND_WIND_ENCHANTMENT_KEY, "复苏之风")
        translationBuilder.add(PROXIMATE_PROPAGATION_ENCHANTMENT_KEY, "抵近传播")
        translationBuilder.add(MAGIC_QUEUE_ENCHANTMENT_KEY, "魔法队列")
        translationBuilder.add(QUEUE_ACCELERATION_ENCHANTMENT_KEY, "队列加速")
        translationBuilder.add(QUEUE_MASTERY_ENCHANTMENT_KEY, "队列精通")
        translationBuilder.add(MANA_OVERFLOW_ENCHANTMENT_KEY, "法力溢出")
        translationBuilder.add(MANA_REGENERATION_ENCHANTMENT_KEY, "法力再生")
        translationBuilder.add(WIZARD_FORCE_ENCHANTMENT_KEY, "巫师神力")
        translationBuilder.add(BLOOD_PACT_ENCHANTMENT_KEY, "血之契约")
        translationBuilder.add(MAGIC_SHIELD_ENCHANTMENT_KEY, "法术护盾")
        translationBuilder.add(BRUTAL_STRENGTH_ENCHANTMENT_KEY, "所向无敌")
        translationBuilder.add(PEAK_OVERDRIVE_ENCHANTMENT_KEY, "大力神超")
        translationBuilder.add(LIGHTNING_STRIKE_ENCHANTMENT_KEY, "闪电五连鞭")

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

        translationBuilder.add(WizardHelmetHacker, "巫师9000型“黑客”")
        translationBuilder.add(WizardHelmet1, "巫师1型“基础巫师头盔”")
        translationBuilder.add(WizardHelmet2, "巫师2型“厄运”")
        translationBuilder.add(WizardHelmet3, "巫师3型“血铸湮灭”")
        translationBuilder.add(WizardHelmet4, "巫师4型“力气和手段”")
        translationBuilder.add(WizardHelmet5, "巫师5型“灭律构式”")
        translationBuilder.add(WizardHelmet13, "巫师13型“红溢盈契”")

        translationBuilder.add(LightningChestplate1, "闪电1型“实境扭曲”")

        translationBuilder.add(MagicTalismanItem, "魔法护符")
        translationBuilder.add(FinderArrowItem, "探测箭矢")
        translationBuilder.add(MetaBowItem, "超雄弓")

        // Potions
        translationBuilder.add("item.minecraft.potion.effect.angered", "狂暴药水")
        translationBuilder.add("item.minecraft.slash_potion.effect.angered", "喷溅型狂暴药水")
        translationBuilder.add("item.minecraft.lingering_potion.effect.angered", "滞留型狂暴药水")

        translationBuilder.add("key.categories.matrix", "Matrix")
        translationBuilder.add("key.matrix.use_magic", "引导魔法")
        translationBuilder.add("key.matrix.next_magic", "下一个魔法")
        translationBuilder.add("key.matrix.previous_magic", "上一个魔法")

        translationBuilder.add(MatrixLanguage.manaCostReduced.key, "法力消耗降低")
        translationBuilder.add(MatrixLanguage.manaCostIncreased.key, "法力消耗增加")

        translationBuilder.add(MatrixLanguage.borrowedTimeChargeDescription.key, "实境扭曲充能: ")
        translationBuilder.add(MatrixLanguage.wizardHelmetLoadDescription.key, "当前负载: ")
        translationBuilder.add(MatrixLanguage.wizardHelmetBloodPactConversionEfficiency.key, "血之契约兑换效率: ")
        translationBuilder.add(MatrixLanguage.wizardHelmetManaDeltaDescription.key, "已使用/回复的法力: ")
    }
}