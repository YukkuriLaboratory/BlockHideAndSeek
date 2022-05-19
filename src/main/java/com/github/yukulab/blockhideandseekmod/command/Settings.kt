package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod
import com.github.yukulab.blockhideandseekmod.config.Config
import com.github.yukulab.blockhideandseekmod.util.HideController
import dev.uten2c.strobo.command.CommandBuilder
import dev.uten2c.strobo.command.CommandContext
import dev.uten2c.strobo.command.argument.ArgumentGetter
import io.github.redstoneparadox.paradoxconfig.config.CollectionConfigOption
import io.github.redstoneparadox.paradoxconfig.config.ConfigCategory
import io.github.redstoneparadox.paradoxconfig.config.ConfigOption
import io.github.redstoneparadox.paradoxconfig.config.RangeConfigOption
import net.minecraft.command.CommandSource
import net.minecraft.text.Text
import net.minecraft.util.registry.Registry
import kotlin.reflect.full.isSubclassOf

object Settings : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("settings") {
            requires { it.hasPermissionLevel(it.server.opPermissionLevel) }
            register()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun CommandBuilder.register(
        configCategory: ConfigCategory = Config,
        parentKey: String = "${Config::class.simpleName}"
    ) {
        val field = ConfigOption::class.java.getDeclaredField("key")
        field.isAccessible = true
        configCategory.getOptions().forEach { option ->
            val key = field.get(option).toString()
            literal("$parentKey.${key}") {
                executes {
                    source.sendFeedback(BHASCommands.bhasMessage(Text.of("$key: ${option.comment}")), false)
                }

                when (option.getKClass()) {
                    Int::class -> registerAsInt(key, option as ConfigOption<Int>)
                    Double::class -> registerAsDouble(key, option as ConfigOption<Double>)
                    Boolean::class -> registerAsBoolean(key, option as ConfigOption<Boolean>)
                    else -> {
                        if (option.getKClass().isSubclassOf(MutableCollection::class)) {
                            registerAsCollection(
                                key,
                                option as CollectionConfigOption<String, MutableCollection<String>>
                            )
                            return@literal
                        }
                        BlockHideAndSeekMod.LOGGER.warn("Config:${key}の型(${option.getKClass()})は現在コマンドで対応していません")
                    }
                }
            }
        }
        configCategory.getSubcategories().forEach { register(it, "$parentKey.${it::class.simpleName}") }
    }

    private val rangeField = RangeConfigOption::class.java.getDeclaredField("range").also { it.isAccessible = true }

    @Suppress("UNCHECKED_CAST")
    private fun CommandBuilder.registerAsInt(key: String, option: ConfigOption<Int>) {
        val executes: CommandBuilder.(ArgumentGetter<Int>) -> Unit = {
            createSuggest(option)
            executes {
                val int = it()
                option.set(int)
                sendChangeMessage(key, int)
                Config.save()
            }
        }
        val child: CommandBuilder.() -> Unit = if (option is RangeConfigOption<Int>) {
            {
                val range = rangeField.get(option) as ClosedRange<Int>
                integer(range.start, range.endInclusive, executes)
            }
        } else {
            {
                integer(child = executes)
            }
        }

        child()
    }

    @Suppress("UNCHECKED_CAST")
    private fun CommandBuilder.registerAsDouble(key: String, option: ConfigOption<Double>) {
        val executes: CommandBuilder.(ArgumentGetter<Double>) -> Unit = {
            createSuggest(option)
            executes {
                val double = it()
                option.set(double)
                sendChangeMessage(key, double)
                Config.save()
            }
        }
        val child: CommandBuilder.() -> Unit = if (option is RangeConfigOption<Double>) {
            {
                val range = rangeField.get(option) as ClosedRange<Double>
                double(range.start, range.endInclusive, executes)
            }
        } else {
            {
                double(child = executes)
            }
        }

        child()
    }

    private fun CommandBuilder.registerAsBoolean(key: String, option: ConfigOption<Boolean>) {
        boolean { getBool ->
            createSuggest(option)
            executes {
                val boolean = getBool()
                option.set(boolean)
                sendChangeMessage(key, boolean)
                Config.save()
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun CommandBuilder.registerAsCollection(
        key: String,
        option: CollectionConfigOption<String, MutableCollection<String>>
    ) {
        literal("add") {
            string { getString ->
                if (key == "ExcludeBlocks") {
                    suggests { _, builder ->
                        listOf(
                            HideController.interfaces.map { "Interface.${it.key}" },
                            HideController.materials.map { "Material.$it" },
                            Registry.BLOCK.ids.map { "Block.${it.path}" }
                        ).flatten().let {
                            CommandSource.suggestMatching(it, builder)
                        }
                    }
                }
                executes {
                    val value = getString()
                    (option.get() as MutableCollection<String>).add(value)
                    source.sendFeedback(BHASCommands.bhasMessage(Text.of("${key}に${value}を追加しました")), true)
                    Config.save()
                }
            }
        }
        literal("remove") {
            string { getString ->
                suggests { _, builder ->
                    (option.get() as MutableCollection<*>).forEach { builder.suggest(it.toString()) }
                    builder.buildFuture()
                }
                executes {
                    val value = getString()
                    (option.get() as MutableCollection<String>).remove(value)
                    source.sendFeedback(BHASCommands.bhasMessage(Text.of("${key}から${value}を削除しました")), true)
                    Config.save()
                }
            }
        }
        literal("list") {
            executes {
                source.sendFeedback(BHASCommands.bhasMessage(Text.of("$key: ${option.get()}")), false)
            }
        }
    }

    private fun CommandContext.sendChangeMessage(key: String, any: Any) {
        source.sendFeedback(BHASCommands.bhasMessage(Text.of("${key}を${any}に変更しました")), true)
    }

    private fun CommandBuilder.createSuggest(option: ConfigOption<*>) {
        suggests { _, builder ->
            builder.suggest(option.get().toString())
            builder.buildFuture()
        }
    }
}
