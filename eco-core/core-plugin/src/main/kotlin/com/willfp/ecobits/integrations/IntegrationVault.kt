package com.willfp.ecobits.integrations

import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.adjustBalance
import com.willfp.ecobits.currencies.formatWithExtension
import com.willfp.ecobits.currencies.getBalance
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.math.BigDecimal

@Suppress("DEPRECATION")
class IntegrationVault(
    private val currency: Currency
) : Economy {
    override fun isEnabled(): Boolean {
        return true
    }

    override fun getName(): String {
        return "EcoBits"
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun fractionalDigits(): Int {
        return -1
    }

    override fun format(amount: Double): String {
        return amount.formatWithExtension()
    }

    override fun currencyNamePlural(): String {
        return currencyNameSingular()
    }

    override fun currencyNameSingular(): String {
        return currency.name
    }

    override fun hasAccount(playerName: String): Boolean {
        val player = Bukkit.getOfflinePlayer(playerName)

        return player.hasPlayedBefore() || player.isOnline
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return player.hasPlayedBefore() || player.isOnline
    }

    override fun hasAccount(playerName: String, worldName: String): Boolean {
        return hasAccount(playerName)
    }

    override fun hasAccount(player: OfflinePlayer, worldName: String): Boolean {
        return hasAccount(player)
    }

    override fun getBalance(playerName: String): Double {
        return getBalance(Bukkit.getOfflinePlayer(playerName))
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return player.getBalance(currency)
    }

    private fun getDoubleValue(value: BigDecimal): Double {
        return value.toDouble()
    }

    override fun getBalance(playerName: String, world: String): Double {
        return getBalance(playerName)
    }

    override fun getBalance(player: OfflinePlayer, world: String): Double {
        return getBalance(player)
    }

    override fun has(playerName: String, amount: Double): Boolean {
        return has(Bukkit.getOfflinePlayer(playerName), amount)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return player.getBalance(currency) >= amount
    }

    override fun has(playerName: String, worldName: String, amount: Double): Boolean {
        return has(playerName, amount)
    }

    override fun has(player: OfflinePlayer, worldName: String, amount: Double): Boolean {
        return has(player, amount)
    }

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        if (player == null) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player can not be null.")
        }

        if (amount < 0) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Can't withdraw below 0.")
        }

        if (player.getBalance(currency) - amount < 0) {
            return EconomyResponse(
                0.0,
                0.0,
                EconomyResponse.ResponseType.FAILURE,
                "Can't withdraw this much!"
            )
        }

        player.adjustBalance(currency, -amount)

        return EconomyResponse(
            amount,
            player.getBalance(currency),
            EconomyResponse.ResponseType.SUCCESS,
            null
        )
    }

    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(player, amount)
    }

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount)
    }

    override fun depositPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        if (player == null) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player can not be null.")
        }

        if (amount < 0) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Can't deposit below 0.")
        }

        if (player.getBalance(currency) + amount > currency.max) {
            return EconomyResponse(
                0.0,
                0.0,
                EconomyResponse.ResponseType.FAILURE,
                "Can't deposit this much (over max)"
            )
        }

        player.adjustBalance(currency, amount)
        return EconomyResponse(
            amount,
            player.getBalance(currency),
            EconomyResponse.ResponseType.SUCCESS,
            null
        )
    }

    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    override fun depositPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(player, amount)
    }

    override fun createPlayerAccount(playerName: String): Boolean {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName))
    }

    override fun createPlayerAccount(player: OfflinePlayer): Boolean {
        return false
    }

    override fun createPlayerAccount(playerName: String, worldName: String): Boolean {
        return false
    }

    override fun createPlayerAccount(player: OfflinePlayer, worldName: String): Boolean {
        return false
    }

    override fun createBank(name: String, player: String): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun createBank(name: String, player: OfflinePlayer): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun deleteBank(name: String): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun bankBalance(name: String): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun bankHas(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun bankDeposit(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun isBankOwner(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun isBankOwner(name: String, player: OfflinePlayer): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun isBankMember(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun isBankMember(name: String, player: OfflinePlayer): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "EcoBits doesn't support bank accounts!"
        )
    }

    override fun getBanks(): MutableList<String> {
        return mutableListOf()
    }
}
