---
title: How to make a Currency
sidebar_position: 1
---



## Creating a Currency

EcoBits lets you make as many currencies as you want, and you make each one by adding a new
entry to the `currencies` list. Simply add and remove currencies as you please.

## Example Currency Config

```yaml
currencies:
  - id: crystals # The ID of the currency.
    name: "Crystals" # The name of the currency.
    symbol: "❖"
    default: 0 # The default balance.
    max: -1 # The maximum balance, set to -1 if no max.
    payable: false # If players should be able to use /ecobits pay to pay other players
    decimal: true # If decimal amounts are allowed in commands, rather than just integer amounts.
    max-decimals: 2 # How many decimals should we allow the players to type in commands.
    vault: false # If this currency should be registered with vault. (Only one currency can be registered with vault, requires server restart)
    local: false # If this currency should not sync between servers.
    balance-shorthand: false # If this currency main command (/crystals) should act as balance command
    format: "&b%symbol%&a%amount% &b%currency%" # The formatted balance. (Placeholders: %currency%, %amount%, %symbol%)
    format-short: "&b%symbol% %amount%" # The formatted shortened balance. (Placeholders: %currency%, %amount%, %symbol%)
    # A tutorial/examples can be found at: https://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html
    decimal-format: "#,##0.00" # The decimal format
    decimal-format-short: "#,##0.00" # The decimal shortened format
    commands: # A list of commands dedicated to this currency (for easier paying, checking balance, etc.)
      - crystals
      - ecocrystals
```

## Using EcoBits currencies

You can use your EcoBits anywhere within effects using the [Price](https://plugins.auxilor.io/all-plugins/prices) system.
By setting your EcoBits currency as your Vault currency you can use your currency in other plugins too. Other plugins can also interact with EcoBits using the [placeholders](https://plugins.auxilor.io/ecobits/placeholderapi) and [commands](https://plugins.auxilor.io/ecobits/commands-and-permissions).

<hr/>

## Default configs

The default configs can be found [here](https://github.com/Auxilor/EcoBits/blob/master/eco-core/core-plugin/src/main/resources/config.yml).