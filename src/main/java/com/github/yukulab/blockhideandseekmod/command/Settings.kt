package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod
import com.github.yukulab.blockhideandseekmod.config.Config
import dev.uten2c.cmdlib.CommandBuilder
import dev.uten2c.cmdlib.CommandContext
import io.github.redstoneparadox.paradoxconfig.config.CollectionConfigOption
import io.github.redstoneparadox.paradoxconfig.config.ConfigCategory
import io.github.redstoneparadox.paradoxconfig.config.ConfigOption
import io.github.redstoneparadox.paradoxconfig.config.RangeConfigOption
import net.minecraft.text.Text

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
                    MutableCollection::class -> registerAsCollection(
                        key,
                        option as CollectionConfigOption<String, MutableCollection<String>>
                    )
                    else -> BlockHideAndSeekMod.LOGGER.warn("Config:${key}の型(${option.getKClass()})は現在コマンドで対応していません")
                }
            }
        }
        configCategory.getSubcategories().forEach { register(it, "$parentKey.${it::class.simpleName}") }
    }

    private val rangeField = RangeConfigOption::class.java.getDeclaredField("range").also { it.isAccessible = true }

    @Suppress("UNCHECKED_CAST")
    private fun CommandBuilder.registerAsInt(key: String, option: ConfigOption<Int>) {
        val executes: CommandBuilder.() -> Unit = {
            createSuggest(option)
            executes {
                val int = getInteger(key)
                option.set(int)
                sendChangeMessage(key, int)
                Config.save()
            }
        }
        val child: CommandBuilder.() -> Unit = if (option is RangeConfigOption<Int>) {
            {
                val range = rangeField.get(option) as ClosedRange<Int>
                integer(key, range.start, range.endInclusive, executes)
            }
        } else {
            {
                integer(key, child = executes)
            }
        }

        child()
    }

    @Suppress("UNCHECKED_CAST")
    private fun CommandBuilder.registerAsDouble(key: String, option: ConfigOption<Double>) {
        val executes: CommandBuilder.() -> Unit = {
            createSuggest(option)
            executes {
                val int = getDouble(key)
                option.set(int)
                sendChangeMessage(key, int)
                Config.save()
            }
        }
        val child: CommandBuilder.() -> Unit = if (option is RangeConfigOption<Double>) {
            {
                val range = rangeField.get(option) as ClosedRange<Double>
                double(key, range.start, range.endInclusive, executes)
            }
        } else {
            {
                integer(key, child = executes)
            }
        }

        child()
    }

    private fun CommandBuilder.registerAsBoolean(key: String, option: ConfigOption<Boolean>) {
        boolean(key) {
            createSuggest(option)
            executes {
                val boolean = getBoolean(key)
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
            string(key) {
                executes {
                    val value = getString(key)
                    (option.get() as MutableCollection<String>).add(value)
                    source.sendFeedback(BHASCommands.bhasMessage(Text.of("${key}に${value}を追加しました")), true)
                    Config.save()
                }
            }
        }
        literal("remove") {
            string(key) {
                suggests { _, builder ->
                    (option.get() as MutableCollection<*>).forEach { builder.suggest(it.toString()) }
                    builder.buildFuture()
                }
                executes {
                    val value = getString(key)
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
