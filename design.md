# TODO:
Currently, the WebSocket endpoint for the server does not parse any text other than to look for the string "CLOSE". 
When "CLOSE" is sent by any client, the server is shut down.

## Below are notes for the planned design

Format:
```
{
    "action": "listen",
    "data": {
        "streams": ["trade_updates"]
    }
}
```

Options for possible streams:
- `"trade_updates"`
    - Unused, but follows the Alpaca API. Intended for future extension.
- `"candle_replay"`
    - Replay the candle history by sending requests for the next candle.
- `"candle_simulate"`
    - Simulate the next candle following a normal/Gaussian distribution (assumption of Black-Scholes).
- `"candle_live_replay"`
    - Replay the candle history at the provided rate.

