---
title: "PlaceholderAPI"
sidebar_position: 2
---

EcoBits exposes every currency through PlaceholderAPI, so you can show balances, leaderboards, and currency details anywhere placeholders work. Swap `<id>` for a currency ID from your `config.yml`, and `<currency>`, `<place>`, and `<format-type>` for the leaderboard values you want. PlaceholderAPI must be installed for these to resolve.

| Placeholder | Description |
| --- | --- |
| `%ecobits_<id>%` | The balance of a specific currency for a player. |
| `%ecobits_<id>_short%` | The balance of a currency, shortened (e.g. 1.2k). |
| `%ecobits_<id>_formatted%` | The formatted balance of a currency (uses the configured format). |
| `%ecobits_<id>_formatted_short%` | The formatted balance, shortened if applicable. |
| `%ecobits_<id>_commas%` | The balance of a currency using comma separators. |
| `%ecobits_<id>_integer%` | The balance truncated to an integer. |
| `%ecobits_<id>_raw%` | The raw numeric balance, with no formatting. |
| `%ecobits_<id>_max%` | The maximum amount for the currency, from config.yml. |
| `%ecobits_<id>_default%` | The default balance for the currency, from config.yml. |
| `%ecobits_<id>_name%` | The display name of the currency, as in config.yml. |
| `%ecobits_<id>_symbol%` | The currency symbol, if configured. |
| `%ecobits_<id>_leaderboard_rank%` | The player's position on the leaderboard for that currency. |
| `%ecobits_top_<currency>_<place>_<name>%` | The player name at the given leaderboard position. |
| `%ecobits_top_<currency>_<place>_<amount>%` | The raw amount for the player at that leaderboard position. |
| `%ecobits_top_<currency>_<place>_<amount>_<format-type>%` | The amount at that position using a format type (commas, short, formatted, formatted_short, integer, or raw). |

<hr/>

## Where to go next

- **Define a currency:** the [How to Make a Currency](how-to-make-a-currency) walkthrough.
- **Commands and permissions:** the full list on the [Commands and Permissions](commands-and-permissions) page.
- **Global settings:** the leaderboard options in the [Plugin Config](plugin-config) reference.
