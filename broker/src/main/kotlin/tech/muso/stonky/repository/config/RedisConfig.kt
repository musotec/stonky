package tech.muso.stonky.repository.config

import Candle
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
    fun redisCandleTemplate(): RedisTemplate<String, Candle> {
        val template: RedisTemplate<String, Candle> = RedisTemplate()
        template.setConnectionFactory(redisConnectionFactory())
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = object : RedisSerializer<Candle> {
            // TODO: compare CBOR/Proto/json/etc speed for serialization from kotlinx.serialization
            //   https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/formats.md#protobuf-experimental
            val serializer: KSerializer<Candle> = Candle.serializer()
            override fun serialize(t: Candle?): ByteArray = Cbor.encodeToByteArray(serializer, t!!)
            override fun deserialize(bytes: ByteArray?): Candle = Cbor.decodeFromByteArray(serializer, bytes!!)
        }

        return template
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