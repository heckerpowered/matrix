package heckerpowered.matrix.data.language

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
        translationBuilder.add(MatrixLanguage.mana.string, "法力值")

        translationBuilder.add(MatrixLanguage.magicTargetPositioning.string, "目标定位")
        translationBuilder.add(MatrixLanguage.magicTargetPositioningDescription1.string, "高亮目标及")
        translationBuilder.add(MatrixLanguage.magicTargetPositioningDescription2.string, "其周围的生物。")

        translationBuilder.add(MatrixLanguage.magicDecisiveStrike.string, "毁灭打击")
        translationBuilder.add(MatrixLanguage.magicDecisiveStrikeDescription1.string, "对选定目标造")
        translationBuilder.add(MatrixLanguage.magicDecisiveStrikeDescription2.string, "成10点伤害，")
        translationBuilder.add(MatrixLanguage.magicDecisiveStrikeDescription3.string, "可叠加。")

        translationBuilder.add(MatrixLanguage.magicManaOverload.string, "法力过载")
        translationBuilder.add(MatrixLanguage.magicManaOverloadDescription1.string, "标记一个目标，")
        translationBuilder.add(MatrixLanguage.magicManaOverloadDescription2.string, "持续5秒。标")
        translationBuilder.add(MatrixLanguage.magicManaOverloadDescription3.string, "记持续期间内")
        translationBuilder.add(MatrixLanguage.magicManaOverloadDescription4.string, "击杀目标回复")
        translationBuilder.add(MatrixLanguage.magicManaOverloadDescription5.string, "20点法力值。")
        // 记持续期间内击杀目标回复20点法力值。

        translationBuilder.add(MatrixLanguage.magicHealthSteal.string, "生命偷取")
        translationBuilder.add(MatrixLanguage.magicHealthStealDescription1.string, "将目标的生命")
        translationBuilder.add(MatrixLanguage.magicHealthStealDescription2.string, "值转为自身的")
        translationBuilder.add(MatrixLanguage.magicHealthStealDescription3.string, "额外生命值。")
    }
}