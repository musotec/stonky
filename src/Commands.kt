data class Command(val type: CommandType, val symbol: String)

enum class CommandType(val string: String) {
    TRADE_UPDATE("trade_updates"),
    CANDLE_REPLAY("candle_replay"),
    CANDLE_SIMULATE("candle_simulate"),
    CANDLE_LIVE_REPLAY("candle_live_replay")
}