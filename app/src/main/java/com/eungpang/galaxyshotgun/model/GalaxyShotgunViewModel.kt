package com.eungpang.galaxyshotgun.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.samsung.android.sdk.iap.lib.helper.HelperDefine
import com.samsung.android.sdk.iap.lib.helper.IapHelper
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener
import com.samsung.android.sdk.iap.lib.listener.OnPaymentListener
import com.samsung.android.sdk.iap.lib.vo.ConsumeVo
import com.samsung.android.sdk.iap.lib.vo.ErrorVo
import com.samsung.android.sdk.iap.lib.vo.PurchaseVo
import java.util.*

class GalaxyShotgunViewModel(
    application: Application
) : AndroidViewModel(application), OnPaymentListener, OnConsumePurchasedItemsListener {
    private val _bullets = MutableLiveData<Int>()
    private val _capacities = MutableLiveData<Int>()
    private val _isPremiums = MutableLiveData<Boolean>() // property

    val bullets : LiveData<Int> = _bullets
    val capacities : LiveData<Int> = _capacities
    val isPremiums : LiveData<Boolean> = _isPremiums

    private val iapHelper: IapHelper
    private val context: Context = application.applicationContext
    private val sharedPreferences: SharedPreferences

    private val SHARED_PREFERENCE_NAME = "GalaxyShotgunPreference"
    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        // sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        sharedPreferences.apply {
            val bullet = getInt(PREFERENCE_KEY_BULLET, DEFAULT_BULLET)
            val capacity = getInt(PREFERENCE_KEY_CAPACITY, DEFAULT_CAPACITY)
            val isPremium = getBoolean(PREFERENCE_KEY_INFINITE, false)

            _bullets.value = bullet
            _capacities.value = capacity
            _isPremiums.value = isPremium
        }

        iapHelper = IapHelper.getInstance(context)
        iapHelper.setOperationMode(HelperDefine.OperationMode.OPERATION_MODE_TEST)
    }

    fun bullet() : Int = _bullets.value ?: 0
    fun capacity() : Int = _capacities.value ?: 0
    fun isPremium() : Boolean = _isPremiums.value ?: false

    fun onFire() {
        _bullets.value = _bullets.value!! - 1
    }

    fun buyBullets() {
        if (_bullets.value!! + 10 > _capacities.value!!) {
            Toast.makeText(context, "Capacity is insufficient. Please buy the capacity first.", Toast.LENGTH_SHORT).show()
            return
        }

        val passThroughParam = UUID.randomUUID().toString()
        sharedPreferences.edit().apply {
            putString(PREFERENCE_KEY_PASS_THROUGH_PARAM, passThroughParam)
            apply()
        }

        iapHelper.startPayment(ITEM_ID_BULLET, passThroughParam, true, this)
    }

    fun buyCapacity() {
        val passThroughParam = UUID.randomUUID().toString()

        sharedPreferences.edit().apply {
            putString(PREFERENCE_KEY_PASS_THROUGH_PARAM, passThroughParam)
            apply()
        }

        iapHelper.startPayment(ITEM_ID_CAPACITY, passThroughParam, true, this)
    }

    fun buyInfinite() {
        val passThroughParam = UUID.randomUUID().toString()

        sharedPreferences.edit().apply {
            putString(PREFERENCE_KEY_PASS_THROUGH_PARAM, passThroughParam)
            apply()
        }

        iapHelper.startPayment(ITEM_ID_INFINITE, passThroughParam, true, this)
    }

    override fun onPayment(errorVo: ErrorVo?, purchaseVo: PurchaseVo?) {
        if (errorVo != null && errorVo.errorCode != IapHelper.IAP_ERROR_NONE) {
            Toast.makeText(context, "Purchase Failed", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "errorVo: ${errorVo.dump()}")
            return
        }

        if (purchaseVo == null) {
            Toast.makeText(context, "PurchaseVo is null.", Toast.LENGTH_SHORT).show()
            return
        }

        val cachedPassThroughParam = sharedPreferences.getString(PREFERENCE_KEY_PASS_THROUGH_PARAM, "")
        if (purchaseVo.passThroughParam != cachedPassThroughParam) {
            Toast.makeText(context, "PassThroughParam is different. maybe It is an abnormal purchase.", Toast.LENGTH_SHORT).show()
            return
        }

        val itemId = purchaseVo.itemId
        when (itemId) {
            ITEM_ID_BULLET -> {
                _bullets.value = _bullets.value!! + 10
            }

            ITEM_ID_CAPACITY -> {
                _capacities.value = _capacities.value!! + 10
            }

            ITEM_ID_INFINITE -> {
                _isPremiums.value = true
            }

            else -> {
                // nop
            }
        }

        if (purchaseVo.isConsumable) {
            iapHelper.consumePurchasedItems(itemId, this)
        }
    }

    fun onStop() {
        sharedPreferences.edit().apply {
            putInt(PREFERENCE_KEY_BULLET, bullet())
            putInt(PREFERENCE_KEY_CAPACITY, capacity())
            putBoolean(PREFERENCE_KEY_INFINITE, isPremium())
            apply()
        }
    }

    override fun onConsumePurchasedItems(_errorVO: ErrorVo?, _consumeList: ArrayList<ConsumeVo>?) {
        // consume finished
    }


    companion object {
        const val TAG = "GalaxyShotgunViewModel"
        const val DEFAULT_BULLET = 5
        const val DEFAULT_CAPACITY = 5

        private const val ITEM_ID_BULLET = "ITEM_BULLET"
        private const val ITEM_ID_CAPACITY = "ITEM_CAPACITY"
        private const val ITEM_ID_INFINITE = "ITEM_INFINITE"

        private const val PREFERENCE_KEY_BULLET = "PREFERENCE_KEY_BULLET"
        private const val PREFERENCE_KEY_CAPACITY = "PREFERENCE_KEY_CAPACITY"
        private const val PREFERENCE_KEY_INFINITE = "PREFERENCE_KEY_INFINITE"

        private const val PREFERENCE_KEY_PASS_THROUGH_PARAM = "PREFERENCE_KEY_PASS_THROUGH_PARAM"
    }
}
