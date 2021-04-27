import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.RuntimeException

@Serializable
internal data class YamlConfigLoader(
    val server: ServerConfig,
    val `api-keys`: ApiConfig?,
) {
    companion object {
        fun defaultConfig(): YamlConfigLoader {
            try {
                return loadFromFile("../../config.yaml")
            } catch (ex: FileNotFoundException) {
                throw RuntimeException("config.yaml not found in root project directory. Create it following the example.config.yaml before compiling.")
            }
        }
        fun loadFromFile(path: String): YamlConfigLoader {
            val inputStream: InputStream = File(path).inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            return Yaml.default.decodeFromString(serializer(), inputString)
        }
    }
}

@Serializable
internal data class ServerConfig(
    val port: Int,
    val host: String,
    val endpoint: String,
)


@Serializable
internal data class ApiConfig(
    val tda: TdaConfig?
)

@Serializable
internal data class TdaConfig(
    val `refresh-token`: String,
    val `consumer-key`: String
)
