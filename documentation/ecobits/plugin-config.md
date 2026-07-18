---
title: "Plugin Config"
sidebar_position: 4
---

This is the main `config.yml` for EcoBits, found at `/plugins/EcoBits/config.yml`. It holds your currency definitions plus the global settings for the leaderboard and number shortcuts. Edit it, then run `/ecobits reload` to apply your changes.

:::warning
Toggling `leaderboard.enabled` and changing a currency's `vault` setting both need a full server restart, not just `/ecobits reload`, before they take effect.
:::

## Default config.yml

```yaml
# Want to use these currencies in other plugins?
# Use the price system and the name of the currency as the ID,
# read more here: https://plugins.auxilor.io/all-plugins/prices

# Placeholders:
# %ecobits_<id>% - The balance of a certain currency
# %ecobits_<id>_short% - The balance of a certain currency, shortened
# %ecobits_<id>_formatted% - The formatted balance of a certain currency
# %ecobits_<id>_formatted_short% - The formatted balance of a certain currency, shortened
# %ecobits_<id>_commas% - The balance with commas of a certain currency
# %ecobits_<id>_integer% - The balance as integer of a certain currency
# %ecobits_<id>_raw% - The raw balance of a certain currency
# %ecobits_<id>_max% - The max balance of a certain currency
# %ecobits_<id>_default% - The default balance of a certain currency
# %ecobits_<id>_name% - The display name of a certain currency
# %ecobits_<id>_symbol% - The symbol of a certain currency
# %ecobits_<id>_leaderboard_rank% - The player's rank of a certain currency
# %ecobits_top_<currency>_<place>_<name/amount>% - The name or balance of the player in a leaderboard position
# %ecobits_top_<currency>_<place>_<name/amount>_<format-type>% - The name or formatted balance of the player in a leaderboard position

# Format types:
# short - Shortened balance
# formatted - Formatted balance (currency.format option)
# formatted_short - Shortened formatted balance (currency.format_short option)
# integer - Integer balance (ex. a balance of 123.45 becomes 123)
# raw - Raw balance
# commas - Deprecated formatted balance

server-id: "main" # Server ID for local currencies over MySQL/MongoDB

leaderboard:
  enabled: true # If the top leaderboard is built; toggling needs a full server restart
  cache-lifetime: 60 # Lifetime of the leaderboard cache, in seconds

shortcuts: ["", "k", "M", "B", "T", "P", "E"] # Suffixes for the short format, ordered by magnitude

currencies:
  - id: crystals # The currency ID; used by the Price system and %ecobits_<id>% placeholders
    name: "Crystals" # The display name shown in messages and the %currency% placeholder
    symbol: "❖" # The symbol exposed as %symbol% in the format strings
    default: 0 # The balance every player starts with
    max: -1 # The maximum balance a player can hold; set to -1 for no limit
    payable: false # If players can send this currency with /<currency> pay
    decimal: true # If decimal amounts are allowed in commands, not just whole numbers
    max-decimals: 2 # How many decimal places players may type in commands
    vault: false # If this currency registers with Vault; only one can, and it needs a restart
    local: false # If true, the balance does not sync between servers
    balance-shorthand: false # If /<currency> with no arguments shows the balance instead of the help menu
    format: "&b%symbol%&a%amount% &b%currency%" # Full balance format; placeholders %currency%, %amount%, %symbol%
    format-short: "&b%symbol% %amount%" # Shortened balance format; same placeholders
    decimal-format: "#,##0.00" # Java DecimalFormat pattern for the full amount
    decimal-format-short: "#,##0.00" # Java DecimalFormat pattern for the shortened amount
    commands: # Dedicated commands for this currency (balance, pay, give, and so on)
      - crystals
      - ecocrystals
```

<hr/>

## Where to go next

- **Add a currency:** the step-by-step [How to Make a Currency](how-to-make-a-currency) walkthrough.
- **Display balances:** every placeholder on the [PlaceholderAPI](placeholderapi) page.
- **Spend it elsewhere:** the [Price](https://plugins.auxilor.io/all-plugins/prices) system for using currencies in other eco plugins.
