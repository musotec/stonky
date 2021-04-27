import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class Drawings(val symbol: String, val drawings: List<Drawing> = mutableListOf()) {
    @Serializable
    data class Drawing(
        val type: String,
        val time: Int,
        val points: FloatArray,
        val label: String,
        val color: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Drawing

            if (type != other.type) return false
            if (time != other.time) return false
            if (!points.contentEquals(other.points)) return false
            if (label != other.label) return false
            if (color != other.color) return false

            return true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + time
            result = 31 * result + points.contentHashCode()
            result = 31 * result + label.hashCode()
            result = 31 * result + color
            return result
        }
    }

    constructor(symbol: String, drawing: Drawing) : this(symbol, listOf(drawing))

    companion object {
        fun Drawings.toJson(): String =
            JsonObject(
                mapOf(
                    symbol to Json.encodeToJsonElement(drawings),
                )
            ).toString()

        const val path = "/tester"
    }
}

