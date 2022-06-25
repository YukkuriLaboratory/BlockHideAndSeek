package com.github.yukulab.blockhideandseekmod.data

import com.mojang.brigadier.StringReader
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.StringNbtReader
import net.minecraft.nbt.visitor.StringNbtWriter

object NbtListSerializer : KSerializer<NbtList> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()

    override fun deserialize(decoder: Decoder): NbtList {
        val element = StringNbtReader(StringReader(decoder.decodeString())).parseElement()
        if (element !is NbtList) {
            throw SerializationException("Cannot serialize to NbtList")
        }
        return element
    }

    override fun serialize(encoder: Encoder, value: NbtList) {
        val encodedNbtString = StringNbtWriter().apply(value)
        encoder.encodeString(encodedNbtString)
    }

}