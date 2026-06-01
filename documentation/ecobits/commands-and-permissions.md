---
title: "Commands and Permissions"
sidebar_position: 3
---

These are every EcoBits command and the permission node each one needs. `<currency>` stands for any command alias you set in a currency's `commands` list, so a `crystals` currency gives you `/crystals pay`, `/crystals top`, and the rest.

| Command | Description | Permission |
| --- | --- | --- |
| `/ecobits reload` | Reload the plugin. | `ecobits.command.reload` |
| `/<currency>` | Access the currency commands below. | `ecobits.command.<currency>` |
| `/<currency> pay` | Pay a player. | `ecobits.command.pay` |
| `/<currency> balance` | Get your balance. | `ecobits.command.balance` |
| `/<currency> get` | Get another player's balance. | `ecobits.command.get` |
| `/<currency> give` | Give a currency. | `ecobits.command.give` |
| `/<currency> givesilent` | Give a currency silently. | `ecobits.command.givesilent` |
| `/<currency> reset` | Reset a player's balance. | `ecobits.command.reset` |
| `/<currency> set` | Set a player's balance. | `ecobits.command.set` |
| `/<currency> take` | Take a currency. | `ecobits.command.take` |
| `/<currency> takesilent` | Take a currency silently. | `ecobits.command.takesilent` |
| `/<currency> top` | Show the leaderboard for the currency. | `ecobits.command.top` |

<hr/>

## Where to go next

- **Set up the aliases:** the `commands` list in the [How to Make a Currency](how-to-make-a-currency) walkthrough.
- **Display balances:** every placeholder on the [PlaceholderAPI](placeholderapi) page.
- **Global settings:** the [Plugin Config](plugin-config) reference.
