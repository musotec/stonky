package tech.muso.stonky.repository.config

import Candle
import Trade
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.io.*


@Configuration
@EnableRedisRepositories
class RedisConfig(val messageListener: MessageListener) {

    @Value("\${spring.redis.host}")
    lateinit var redisHost: String

    @Value("\${spring.redis.port}")
    lateinit var redisPort: String

    @Value("\${spring.redis.topic}")
    lateinit var redisTopic: String

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val factory = LettuceConnectionFactory(RedisStandaloneConfiguration(redisHost, redisPort.toInt()))
        return factory
    }

//    @Bean
//    fun redisTemplate(): RedisTemplate<String, Any> {
//        val template: RedisTemplate<String, Any> = RedisTemplate()
//        template.setConnectionFactory(redisConnectionFactory())
//        template.keySerializer = StringRedisSerializer()
////        template.valueSerializer = KotlinSerializationJsonHttpMessageConverter()
//        template.valueSerializer = GenericJackson2JsonRedisSerializer()
////        template.valueSerializer = RedisSerializer.byteArray()
//        return template
//    }

    @OptIn(ExperimentalSerializationApi::class)
    @Bean
    fun redisCandleTemplate() = RedisTemplate<String, Candle>().apply {
        setConnectionFactory(redisConnectionFactory())
        keySerializer = StringRedisSerializer()
        valueSerializer = object : RedisSerializer<Candle> {
            // TODO: compare CBOR/Proto/json/etc speed for serialization from kotlinx.serialization
            //   https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/formats.md#protobuf-experimental
            val serializer: KSerializer<Candle> = Candle.serializer()
            override fun serialize(t: Candle?): ByteArray = Cbor.encodeToByteArray(serializer, t!!)
            override fun deserialize(bytes: ByteArray?): Candle = Cbor.decodeFromByteArray(serializer, bytes!!)
        }
//        valueSerializer = object : RedisSerializer<Candle> {
//            // TODO: compare CBOR/Proto/json/etc speed for serialization from kotlinx.serialization
//            //   https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/formats.md#protobuf-experimental
//            val serializer: KSerializer<Candle> = Candle.serializer()
//            override fun serialize(t: Candle?): ByteArray = ProtoBuf.encode
//            override fun deserialize(bytes: ByteArray?): Candle = Cbor.decodeFromByteArray(serializer, bytes!!)
//        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Bean
    fun redisTradesTemplate() = RedisTemplate<String, Trade>().apply {
        setConnectionFactory(redisConnectionFactory())   // TODO: change connection factory if Trades/Candles on different Redis nodes
        keySerializer = StringRedisSerializer()
//        valueSerializer = object : RedisSerializer<Trade> {
//            val serializer: KSerializer<Trade> = Trade.serializer()
//            override fun serialize(t: Trade?): ByteArray = Cbor.encodeToByteArray(serializer, t!!)
//            override fun deserialize(bytes: ByteArray?): Trade = Cbor.decodeFromByteArray(serializer, bytes!!)
//        }

//        valueSerializer = object : RedisSerializer<Trade> {
//            val serializer: KSerializer<Trade> = Trade.serializer()
//            override fun serialize(t: Trade?): ByteArray = Json.encodeToString(serializer, t!!).toByteArray(Charsets.UTF_8)
//            override fun deserialize(bytes: ByteArray?): Trade = Json.decodeFromString(serializer, bytes!!.toString(Charsets.UTF_8))
//        }

//        @SerialName("x") val exchange: String,
//        @SerialName("p") val price: Double,
//        @SerialName("s") val size: Int,
//        @SerialName("c") val conditions: ArrayList<String>, // TODO: can byte pack to an int.
//        @SerialName("i") val id: Long,
//        @SerialName("z") val tape: String
        valueSerializer = object : RedisSerializer<Trade> {
            override fun serialize(t: Trade?): ByteArray {
                // TODO: compare time & write more kotlin version
                val baos = ByteArrayOutputStream()
                val oos = ObjectOutputStream(baos)
                oos.writeLong(t!!.timestamp)
                oos.writeChar(t.exchange.first().toInt())
                oos.writeDouble(t.price)
                oos.writeInt(t.size)
                oos.writeLong(t.id)
                oos.writeChar(t.tape.first().toInt())
                oos.writeChar(t.conditions.getOrNull(0)?.firstOrNull()?.toInt() ?: ' '.toInt())
                oos.writeChar(t.conditions.getOrNull(1)?.firstOrNull()?.toInt() ?: ' '.toInt())
                oos.flush()
                val result = baos.toByteArray()
                baos.close()
                oos.close()
                return result
            }

            override fun deserialize(bytes: ByteArray?): Trade {

                val byteArrayInputStream = ByteArrayInputStream(bytes!!)
                val ins: ObjectInput = ObjectInputStream(byteArrayInputStream)
//                val result = objectInput.readObject() as T
                val timestamp = ins.readLong()
                val exchange = ins.readChar().toString()
                val price = ins.readDouble()
                val size = ins.readInt()
                val id = ins.readLong()
                val tape = ins.readChar().toString()
                val c0 = ins.readChar()
                val c1 = ins.readChar()
                val conditions = arrayListOf(c0.toString(), c1.toString())
                ins.close()
                byteArrayInputStream.close()
                return Trade(
                    timestamp = timestamp,
                    exchange = exchange,
                    price = price,
                    size = size,
                    conditions = conditions,
                    id = id,
                    tape = tape
                )
            }
        }
    }

    @Bean
    fun topic(): ChannelTopic = ChannelTopic(redisTopic)

    @Bean
    fun newMessageListener(): MessageListenerAdapter = MessageListenerAdapter(messageListener)

    @Bean
    fun redisContainer(): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(redisConnectionFactory())
        container.addMessageListener(newMessageListener(), topic())
        return container
    }

//    @Bean
//    fun keyValueMappingContext(): RedisMappingContext? {
//        return RedisMappingContext(
//            MappingConfiguration(IndexConfiguration(), MyKeyspaceConfiguration())
//        )
//    }
//
//    class MyKeyspaceConfiguration : KeyspaceConfiguration() {
//        override fun initialConfiguration(): Iterable<KeyspaceSettings> {
//            return Collections.singleton(KeyspaceSettings(BarCandle::class.java, "candles"))
//        }
//    }
}