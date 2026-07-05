---
title: "How to Make a Currency"
sidebar_position: 1
---

A **currency** is one entry in the `currencies` list inside EcoBits' `config.yml`. Each entry defines an **ID**, how the balance is **formatted**, and which **commands** players use to interact with it. This page takes you from an empty list to a working, spendable currency.

## Quick start

1. Open `/plugins/EcoBits/config.yml`.
2. Under `currencies:`, copy the bundled `crystals` entry as your starting point.
3. Change `id`, `name`, and `symbol` to match your new currency.
4. Set the `commands` list to the aliases players should type (e.g. `gems`).
5. Save the file and run `/ecobits reload`.
6. Run `/<your currency> balance` in-game to confirm it loaded and shows a balance.

## Naming and IDs

The `id` field is the currency's identity. It is the name you pass to the [Price](https://hub.auxilor.io/wiki/eco/the-price-lookup-system) system in other plugins and the `<id>` in every `%ecobits_<id>%` placeholder, so keep it stable once players have balances.

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the currency will not load.
:::

## The structure of a currency

A currency entry breaks into five parts.

| Part | What it controls |
| --- | --- |
| **Identity** | The ID, display name, and symbol the currency is referenced by. |
| **Balance limits** | The starting balance and the maximum a player can hold. |
| **Behaviour** | Paying, decimals, Vault registration, and cross-server syncing. |
| **Formatting** | How balances render in chat and placeholders. |
| **Commands** | The per-currency command aliases. |

Here is one complete entry with every part in place:

```yaml
currencies:
  # === Identity: how the currency is referenced and labelled ===
  - id: crystals # The currency ID; used by the Price system and in %ecobits_<id>% placeholders
    name: "Crystals" # The display name shown in messages and the %currency% placeholder
    symbol: "❖" # The symbol exposed as %symbol% in the format strings

    # === Balance limits: starting and maximum balances ===
    default: 0 # The balance every player starts with
    max: -1 # The maximum balance a player can hold; set to -1 for no limit

    # === Behaviour: what players can do with the currency ===
    payable: false # If players can send this currency with /<currency> pay
    decimal: true # If decimal amounts are allowed in commands, not just whole numbers
    max-decimals: 2 # How many decimal places players may type in commands
    vault: false # If this currency registers with Vault; only one currency can, and it needs a restart
    local: false # If true, the balance does not sync between servers
    balance-shorthand: false # If /<currency> with no arguments shows the balance instead of the help menu

    # === Formatting: how balances are displayed ===
    format: "&b%symbol%&a%amount% &b%currency%" # Full balance format; placeholders %currency%, %amount%, %symbol%
    format-short: "&b%symbol% %amount%" # Shortened balance format (e.g. 1.2k); same placeholders
    decimal-format: "#,##0.00" # Java DecimalFormat pattern for the full amount
    decimal-format-short: "#,##0.00" # Java DecimalFormat pattern for the shortened amount

    # === Commands: the per-currency command aliases ===
    commands: # Dedicated commands for this currency (balance, pay, give, and so on)
      - crystals
      - ecocrystals
```

### Identity

The lead fields name the currency and decide how other plugins reach it.

```yaml
- id: crystals # Referenced by the Price system and %ecobits_<id>% placeholders
  name: "Crystals" # Display name; fills the %currency% placeholder
  symbol: "❖" # Fills the %symbol% placeholder in format strings
```

### Balance limits

These set where balances start and where they stop.

```yaml
default: 0 # Starting balance for every player
max: -1 # Maximum balance; -1 means unlimited
```

### Behaviour

These toggles control how players and other plugins interact with the currency.

```yaml
payable: false # Enables /<currency> pay between players
decimal: true # Allows fractional amounts in commands
max-decimals: 2 # Caps how many decimal places players may type
vault: false # Registers this currency with Vault (one currency only)
local: false # Stops the balance syncing between servers when true
balance-shorthand: false # Makes bare /<currency> print the balance
```

:::warning
Setting `vault: true` on a currency needs a full server restart, not just `/ecobits reload`, and only one currency can hold the Vault registration at a time.
:::

### Formatting

The format fields decide how a balance appears in chat and placeholders. `format` is the full display; `format-short` is the abbreviated one (e.g. `1.2k`). The `decimal-format` patterns are standard Java DecimalFormat strings.

```yaml
format: "&b%symbol%&a%amount% &b%currency%" # Full display
format-short: "&b%symbol% %amount%" # Abbreviated display
decimal-format: "#,##0.00" # Number pattern for the full amount
decimal-format-short: "#,##0.00" # Number pattern for the short amount
```

### Commands

Each alias here becomes a working command, so players reach the currency directly instead of through `/ecobits`.

```yaml
commands: # Every alias registers as a /<alias> command
  - crystals
  - ecocrystals
```

:::info
Command aliases are registered when the server starts. After adding or changing the `commands` list, restart the server so the new `/<alias>` commands register; a reload alone will not.
:::

## Internal placeholders

These placeholders are available inside the `format` and `format-short` strings:

| Placeholder | What it inserts |
| --- | --- |
| `%amount%` | The player's balance amount. |
| `%symbol%` | The currency's configured symbol. |
| `%currency%` | The currency's display name. |

:::tip Troubleshooting
- **Currency not loading?** Check the `id` uses only lowercase letters, numbers, and underscores, then run `/ecobits reload`.
- **`/<currency>` says unknown command?** Aliases register on startup; add the alias under `commands` and restart the server.
- **Vault plugins don't see the balance?** Only one currency can register with Vault; set `vault: true` on a single currency and restart.
:::

<hr/>

## Where to go next

- **Spend it elsewhere:** use the currency `id` in the [Price](https://hub.auxilor.io/wiki/eco/the-price-lookup-system) system across your other eco plugins.
- **Commands and permissions:** the full list on the [Commands and Permissions](commands-and-permissions) page.
- **Display balances:** every placeholder on the [PlaceholderAPI](placeholderapi) page.
- **Global settings:** leaderboards and shortcuts in the [Plugin Config](plugin-config) reference.
