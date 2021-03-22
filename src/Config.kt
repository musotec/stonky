import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.io.InputStream

object ConfigVars {
    const val PORT = 8080
    const val PATH_TEST_API = "/"
}

@Serializable
data class Config(
    val server: ServerConfig,
    val `api-keys`: ApiConfig?,
) {
    companion object {
        fun defaultConfig(): Config {
            return loadFromFile("config.yaml")
        }
        fun loadFromFile(path: String): Config {
            val inputStream: InputStream = File(path).inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            return Yaml.default.decodeFromString(serializer(), inputString)
        }
    }
}

@Serializable
data class ServerConfig(
    val port: Int,
    val host: String,
    val endpoint: String,
)

@Serializable
data class ApiConfig(
    val tda: TdaConfig?
)

@Serializable
data class TdaConfig(
    val `refresh-token`: String,
    val `consumer-key`: String
)

fun main() {
    println(Config.defaultConfig())
}