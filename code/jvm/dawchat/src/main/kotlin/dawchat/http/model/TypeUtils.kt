package dawchat.http.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import dawchat.domain.channel.ChannelType
import dawchat.domain.channel.UserPermissions
import dawchat.domain.message.MessageTypes


class HttpTypeMappers {
    companion object {


        fun getStringValue(perm: UserPermissions): String {
            return when (perm) {
                UserPermissions.Read -> "read"
                UserPermissions.Write -> "write"
                UserPermissions.Invite -> "invite"
                UserPermissions.Rename -> "rename"
                UserPermissions.RemoveUsers -> "remove-users"
            }
        }

        fun getStringValue(type: ChannelType): String {
            return when (type) {
                ChannelType.Public -> "public"
                ChannelType.Private -> "private"
            }
        }

        fun getStringValue(type: MessageTypes): String {
            return when (type) {
                MessageTypes.ChannelCreated -> "channel-create"
                MessageTypes.ChannelRenamed -> "channel-rename"
                MessageTypes.Text -> "text"
            }
        }

        private val userPermToStr = UserPermissions.entries.map { it to getStringValue(it) }.toMap()
        private val strToUserPerm = userPermToStr.map { (k, v) -> v to k }.toMap()

        private val channelTypeToStr = ChannelType.entries.map { it to getStringValue(it) }.toMap()
        private val strToChannelType = channelTypeToStr.map { (k, v) -> v to k }.toMap()

        private val messTypeToStr = MessageTypes.entries.map { it to getStringValue(it) }.toMap()
        private val strToMessType = messTypeToStr.map { (k, v) -> v to k }.toMap()

        fun getUserPerm(value: String): UserPermissions? {
            return strToUserPerm[value]
        }

        fun getChannelType(value: String): ChannelType? {
            return strToChannelType[value]
        }

        fun getMessType(value: String): MessageTypes? {
            return strToMessType[value]
        }

    }

}

class UserPermsSerializer : JsonSerializer<Array<UserPermissions> >() {
    override fun serialize(values: Array<UserPermissions> , gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeArray(
            values.map { HttpTypeMappers.getStringValue(it) }.toTypedArray(),
            0,
            values.size
        )
    }
}

class UserPermsDeserializer : JsonDeserializer<Array<UserPermissions>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Array<UserPermissions> {
        val res = mutableListOf(UserPermissions.Read) //
        p.codec.readTree<JsonNode>(p).forEach {
            val s = it.textValue()
            val perm  = HttpTypeMappers.getUserPerm(s)
                ?: throw IllegalArgumentException()
            if(!res.contains(perm))
                res.add(perm)
        }
        return res.toTypedArray()
    }
}


class ChannelTypeSerializer : JsonSerializer<ChannelType>() {
    override fun serialize(value: ChannelType, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(HttpTypeMappers.getStringValue(value))
    }
}

class ChannelTypeDeserializer : JsonDeserializer<ChannelType>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ChannelType {
        return HttpTypeMappers.getChannelType(p.text)
            ?: throw IllegalArgumentException()
    }
}

class MessTypeSerializer : JsonSerializer<MessageTypes>() {
    override fun serialize(value: MessageTypes, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(HttpTypeMappers.getStringValue(value))
    }
}

class MessTypeDeserializer : JsonDeserializer<MessageTypes>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MessageTypes {
        return HttpTypeMappers.getMessType(p.text)
            ?: throw IllegalArgumentException()
    }
}

class MessTypesSerializer : JsonSerializer<Array<MessageTypes> >() {
    override fun serialize(values: Array<MessageTypes> , gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeArray(
            values.map { HttpTypeMappers.getStringValue(it) }.toTypedArray(),
            0,
            values.size
        )
    }
}


class MessTypesDeserializer : JsonDeserializer<Array<MessageTypes>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Array<MessageTypes> {
        val res = mutableListOf<MessageTypes>()//
        val node = p.codec.readTree<JsonNode>(p)
        if(!node.isArray)
            throw IllegalArgumentException()
        node.forEach {
            val s = it.textValue()
            val type  = HttpTypeMappers.getMessType(s)
                ?: throw IllegalArgumentException()
            if(!res.contains(type))
                res.add(type)
        }
        return res.toTypedArray()
    }
}




/*
@JsonComponent
class ChannelTypeSerializer : JsonSerializer<ChannelType>() {
    override fun serialize(value: ChannelType, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(HttpMappers2.getStringValue(value))
    }

    override fun handledType(): Class<ChannelType>? {
        return ChannelType::class.java
    }
}

@JsonComponent
class ChannelTypeDeserializer : JsonDeserializer<ChannelType>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): ChannelType {
        return HttpMappers2.getChannelType(p.text)
            ?: throw IllegalArgumentException()
    }

    override fun handledType(): Class<ChannelType>? {
        return ChannelType::class.java
    }
}


/*
@Component
class ToChannelTypeConverter : Converter<String, ChannelType> {
    override fun convert(source: String): ChannelType {
        return HttpMappers2.getChannelType(source)
            ?: throw IllegalArgumentException()
    }
}

@Component
class FromChannelTypeConverter : Converter<ChannelType, String> {
    override fun convert(source: ChannelType): String {
        return HttpMappers2.getStringValue(source)
    }
}
 */


/*
class ToChannelTypeConverter : StdConverter<String, ChannelType>() {
    override fun convert(value: String): ChannelType {
        return HttpMappers2.getChannelType(value)
                ?: throw IllegalArgumentException()
    }
}

class FromChannelTypeConverter : StdConverter<ChannelType, String>() {
    override fun convert(value: ChannelType): String {
        return HttpMappers2.getStringValue(value)
    }
}
 */


/*
class ChannelTypeSerializer: StdSerializer<ChannelType>(ChannelType::class.java) {
    override fun serialize(value: ChannelType, generator: JsonGenerator, arg2: SerializerProvider) {
        generator.writeString(HttpMappers2.getStringValue(value))
    }
}


class ChannelTypeDeserializer : StdDeserializer<ChannelType>(ChannelType::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ChannelType {
        return HttpMappers2.getChannelType(p.text.lowercase().trim())
            ?: throw IllegalArgumentException()
    }
}
 */