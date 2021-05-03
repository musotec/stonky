import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import java.nio.file.Path

/**
 * Generate the GeneratedConfig.kt file based on the configuration YAML.
 */
fun main() {
    // read in the config yaml
    val config = YamlConfigLoader.defaultConfig()

    val _port = PropertySpec.builder("port", Int::class)
        .initializer("%L", config.server.port)
        .build()

    val _host = PropertySpec.builder("host", String::class)
        .initializer("%S", config.server.host)
        .build()

    val _endpoint = PropertySpec.builder("endpoint", String::class)
        .initializer("%S", config.server.endpoint)
        .build()

    // TODO-1: API keys should be loaded on the fly from config.yaml
    // generate Config.ApiKeys.*
    val apiKeys = TypeSpec.objectBuilder("ApiKeys").run {

        // generate Config.ApiKeys.Tda.*
        config.`api-keys`?.tda?.let {
            // TODO: extract these out into their own classes when we add more APIs
            val tda = TypeSpec.objectBuilder("TDAmeritrade")
                .addProperty(
                    PropertySpec.builder("refresh-token", String::class)
                        .initializer("%S", it.`refresh-token`)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("consumer-key", String::class)
                        .initializer("%S", it.`consumer-key`)
                        .build()
                )
                .build()

            addType(tda)
        }

        // finally build our Config.ApiKeys.*
        build()
    }

    // generate Config.Server.*
    val serverConfig = TypeSpec.objectBuilder("server")
        .addProperties(listOf(_port,_host, _endpoint))
        .build()

    val yamlConfig = TypeSpec.objectBuilder("Config")
        .addType(serverConfig)
        .addType(apiKeys)
        .build()

    val generatedKotlinFile = FileSpec.builder(
        packageName = "tech.muso.stonky.config",
        fileName = "GeneratedConfig"
    ).addType(yamlConfig).build()

    generatedKotlinFile.writeTo(File("../build/generated/src/main"))
}