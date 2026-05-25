---
title: "PlaceholderAPI"
sidebar_position: 2
---

| Placeholder                                               | Description                                                                                                                             |
|-----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `%ecobits_<id>%`                                          | The balance/amount of a specific currency for a player                                                                                  |
| `%ecobits_<id>_short%`                                    | The balance of a currency, shortened (e.g. 1.2K)                                                                                        |
| `%ecobits_<id>_formatted%`                                | The formatted balance of a currency (uses configured formatting)                                                                        |
| `%ecobits_<id>_formatted_short%`                          | The formatted balance, shortened if applicable                                                                                          |
| `%ecobits_<id>_commas%`                                   | The balance of a currency using comma separators                                                                                        |
| `%ecobits_<id>_integer%`                                  | The balance rounded/truncated to an integer                                                                                             |
| `%ecobits_<id>_raw%`                                      | The raw numeric balance value (no formatting)                                                                                           |
| `%ecobits_<id>_max%`                                      | The maximum amount for the currency (from config.yml)                                                                                   |
| `%ecobits_<id>_default%`                                  | The default balance for the currency (from config.yml)                                                                                  |
| `%ecobits_<id>_name%`                                     | The display name of the currency (as in config.yml)                                                                                     |
| `%ecobits_<id>_symbol%`                                   | The currency symbol (if configured)                                                                                                     |
| `%ecobits_<id>_leaderboard_rank%`                         | The player's rank/position on the leaderboard for that currency                                                                         |
| `%ecobits_top_<currency>_<place>_<name>%`                 | The player name at the given leaderboard position for the currency                                                                      |
| `%ecobits_top_<currency>_<place>_<amount>%`               | The raw/amount value for the player at that leaderboard position                                                                        |
| `%ecobits_top_<currency>_<place>_<amount>_<format-type>%` | The amount for that leaderboard position using the specified format type (e.g. commas, short, formatted, formatted_short, integer, raw) |
