package com.mycelium.wallet.activity.receive

import android.app.Application
import android.app.NotificationManager
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.mrd.bitlib.model.Address
import com.mrd.bitlib.util.CoinUtil
import com.mycelium.wallet.MbwManager
import com.mycelium.wallet.R
import com.mycelium.wallet.activity.util.AccountDisplayType
import com.mycelium.wallet.event.SyncFailed
import com.mycelium.wallet.event.SyncStopped
import com.mycelium.wapi.model.TransactionSummary
import com.mycelium.wapi.wallet.AddressUtils
import com.mycelium.wapi.wallet.WalletAccount
import com.mycelium.wapi.wallet.btc.AbstractBtcAccount
import com.mycelium.wapi.wallet.btc.BtcLegacyAddress
import com.mycelium.wapi.wallet.btc.WalletBtcAccount
import com.mycelium.wapi.wallet.coins.BitcoinMain
import com.mycelium.wapi.wallet.coins.BitcoinTest
import com.mycelium.wapi.wallet.currency.CurrencyValue
import com.mycelium.wapi.wallet.currency.ExchangeBasedCurrencyValue
import com.mycelium.wapi.wallet.segwit.SegwitAddress
import com.squareup.otto.Subscribe

class ReceiveCoinsModel(
        val context: Application,
        val account: WalletAccount<*, *>,
        private val accountLabel: String,
        val havePrivateKey: Boolean,
        showIncomingUtxo: Boolean = false
) {
    val amountData: MutableLiveData<CurrencyValue?> = MutableLiveData()
    val alternativeAmountData: MutableLiveData<CurrencyValue?> = MutableLiveData()
    val nfc: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    val receivingAmount: MutableLiveData<CurrencyValue?> = MutableLiveData()
    val receivingAmountWrong: MutableLiveData<Boolean> = MutableLiveData()
    val receivingAddress: MutableLiveData<Address> = MutableLiveData()

    private var syncErrors = 0
    private val mbwManager = MbwManager.getInstance(context)
    private var receivingSince = System.currentTimeMillis()
    private var lastAddressBalance: CurrencyValue? = null
    private var accountDisplayType: AccountDisplayType? = null

    init {
        mbwManager.eventBus.register(this)
        receivingAmountWrong.value = false
        receivingAddress.value = (account as AbstractBtcAccount).receivingAddress.get()

        if (showIncomingUtxo) {
            updateObservingAddress()
        }
    }

    fun updateObservingAddress() {
        val address = receivingAddress.value
        mbwManager.watchAddress(AddressUtils.fromAddress(address))
    }

    fun onCleared() {
        mbwManager.stopWatchingAddress()
        mbwManager.eventBus.unregister(this)
    }

    fun setAmount(newAmount: CurrencyValue) {
        if (!CurrencyValue.isNullOrZero(newAmount)) {
            if (newAmount.currency == account.accountDefaultCurrency && newAmount.currency != mbwManager.fiatCurrency) {
                alternativeAmountData.value = CurrencyValue.fromValue(newAmount, mbwManager.fiatCurrency, mbwManager
                        .exchangeRateManager)
                amountData.value = newAmount
            } else {
                amountData.value = if (account.accountDefaultCurrency != newAmount.currency) {
                    // use the accounts default currency as alternative
                    CurrencyValue.fromValue(newAmount, account.accountDefaultCurrency,
                            mbwManager.exchangeRateManager)
                } else {
                    // special case for Coinapult
                    CurrencyValue.fromValue(newAmount, CurrencyValue.BTC,
                            mbwManager.exchangeRateManager)
                }
                alternativeAmountData.value = newAmount
            }
        } else {
            amountData.value = null
            alternativeAmountData.value = null
        }
    }

    fun getPaymentUri(): String {
        val prefix = accountLabel

        val uri = StringBuilder(prefix).append(':')
        uri.append(receivingAddress.value)
        if (!CurrencyValue.isNullOrZero(amountData.value)) {
            if (accountDisplayType == AccountDisplayType.COLU_ACCOUNT) {
                uri.append("?amountData=").append(amountData.value!!.value.toPlainString())
            } else {
                val value = ExchangeBasedCurrencyValue.fromValue(amountData.value,
                        account.accountDefaultCurrency, mbwManager.exchangeRateManager).value
                if (value != null) {
                    uri.append("?amountData=").append(CoinUtil.valueString(value,
                            CoinUtil.Denomination.BTC, false))
                } else {
                    Toast.makeText(context, R.string.value_conversion_error, Toast.LENGTH_LONG).show()
                }
            }
        }
        return uri.toString()
    }

    fun loadInstance(savedInstanceState: Bundle) {
        amountData.value = savedInstanceState.getSerializable(AMOUNT) as CurrencyValue?
        receivingSince = savedInstanceState.getLong(RECEIVING_SINCE)
        syncErrors = savedInstanceState.getInt(SYNC_ERRORS)
        lastAddressBalance = savedInstanceState.getSerializable(LAST_ADDRESS_BALANCE) as CurrencyValue?
    }

    fun saveInstance(outState: Bundle) {
        if (!CurrencyValue.isNullOrZero(amountData.value)) {
            outState.putSerializable(AMOUNT, amountData.value)
        }
        outState.putLong(RECEIVING_SINCE, receivingSince)
        outState.putInt(SYNC_ERRORS, syncErrors)
        outState.putSerializable(LAST_ADDRESS_BALANCE, lastAddressBalance)
    }

    @Subscribe
    fun syncError(event: SyncFailed) {
        // stop syncing after a certain amountData of errors (no network available)
        if (++syncErrors > MAX_SYNC_ERRORS) {
            mbwManager.stopWatchingAddress()
        }
    }

    @Subscribe
    fun syncStopped(event: SyncStopped) {
        val transactionsSince = (account as WalletBtcAccount).getTransactionsSince(receivingSince)
        val interesting = getTransactionsToCurrentAddress(transactionsSince)
        var sum = if (interesting.isEmpty()) {
            null
        } else {
            interesting.first().value
        }
        interesting.drop(1).forEach { sum = sum!!.add(it.value, mbwManager.exchangeRateManager) }
        receivingAmount.value = sum

        if (!CurrencyValue.isNullOrZero(amountData.value) && sum != null) {
            // if the user specified an amountData, check it if it matches up...
            receivingAmountWrong.value = sum!! != amountData.value
            if (sum != lastAddressBalance) {
                makeNotification(sum)
            }
        }
    }

    private fun makeNotification(sum: CurrencyValue?) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val mBuilder = NotificationCompat.Builder(context) //TODO api 28 change, broken.
                .setSmallIcon(R.drawable.ic_launcher)
                .setSound(soundUri, AudioManager.STREAM_NOTIFICATION) //This sets the sound to play
        notificationManager!!.notify(0, mBuilder.build())
        lastAddressBalance = sum
    }

    private fun getTransactionsToCurrentAddress(transactionsSince: MutableList<TransactionSummary>) =
            transactionsSince.filter { it.toAddresses.contains(receivingAddress.value) }

    companion object {
        private const val MAX_SYNC_ERRORS = 8
        private const val LAST_ADDRESS_BALANCE = "lastAddressBalance"
        private const val RECEIVING_SINCE = "receivingSince"
        private const val SYNC_ERRORS = "syncErrors"
        private const val AMOUNT = "amount"
    }
}