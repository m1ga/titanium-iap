/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2018 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
@file:Suppress("unused", "SpellCheckingInspection")

package ti.iap

import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.android.billingclient.api.BillingClient.FeatureType.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.android.billingclient.api.BillingClient.SkuType.SUBS
import com.android.billingclient.api.Purchase.PurchaseState.*
import com.android.billingclient.api.QueryProductDetailsParams.Product
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollFunction
import org.appcelerator.kroll.KrollModule
import org.appcelerator.kroll.KrollProxy
import org.appcelerator.kroll.annotations.Kroll
import org.appcelerator.titanium.TiApplication
import ti.iap.handlers.BillingConnectionHandler
import ti.iap.handlers.ProductsHandler
import ti.iap.handlers.PurchaseHandler
import ti.iap.helper.QueryHandler
import ti.iap.models.ProductModel
import ti.iap.models.PurchaseModel


@Kroll.module(name = "TitaniumInAppPurchase", id = "ti.iap")
class TitaniumInAppPurchaseModule : KrollModule() {
    private var purchaseHandler: PurchaseHandler? = null
    private var billingConnectionState: BillingConnectionHandler? = null
    private var billingClient: BillingClient? = null

    companion object {
        const val LCAT = "TitaniumIAP"

        @Kroll.constant const val FEATURE_TYPE_IN_APP_ITEMS_ON_VR = -1                                  // Purchase/query for in-app items on VR (removed in v5)
        @Kroll.constant const val FEATURE_TYPE_PRICE_CHANGE_CONFIRMATION = PRICE_CHANGE_CONFIRMATION    // Launch a price change confirmation flow
        @Kroll.constant const val FEATURE_TYPE_SUBSCRIPTIONS = SUBSCRIPTIONS                            // Purchase/query for subscriptions
        @Kroll.constant const val FEATURE_TYPE_SUBSCRIPTIONS_ON_VR = -2                                 // Purchase/query for subscriptions on VR (removed in v5)
        @Kroll.constant const val FEATURE_TYPE_SUBSCRIPTIONS_UPDATE = SUBSCRIPTIONS_UPDATE              // Subscriptions update/replace

        @Kroll.constant const val SKU_TYPE_INAPP = INAPP
        @Kroll.constant const val SKU_TYPE_SUBS = SUBS

        @Kroll.constant const val PURCHASE_STATE_UNSPECIFIED_STATE = UNSPECIFIED_STATE
        @Kroll.constant const val PURCHASE_STATE_PURCHASED = PURCHASED
        @Kroll.constant const val PURCHASE_STATE_PENDING = PENDING

        @Kroll.constant const val CODE_BILLING_UNAVAILABLE = BILLING_UNAVAILABLE        // "Billing API version is not supported for the type requested."
        @Kroll.constant const val CODE_DEVELOPER_ERROR = DEVELOPER_ERROR                // "Invalid arguments provided to the API."
        @Kroll.constant const val CODE_FEATURE_NOT_SUPPORTED = FEATURE_NOT_SUPPORTED    // "Requested feature is not supported by Play Store on the current device."
        @Kroll.constant const val CODE_ITEM_ALREADY_OWNED = ITEM_ALREADY_OWNED          // "Failure to purchase since item is already owned."
        @Kroll.constant const val CODE_ITEM_NOT_OWNED = ITEM_NOT_OWNED                  // "Failure to consume since item is not owned."
        @Kroll.constant const val CODE_ITEM_UNAVAILABLE = ITEM_UNAVAILABLE              // "Requested product is not available for purchase."
        @Kroll.constant const val CODE_SERVICE_DISCONNECTED = SERVICE_DISCONNECTED      // "Play Store service is not connected now - potentially transient state."
        @Kroll.constant const val CODE_SERVICE_TIMEOUT = SERVICE_TIMEOUT                // "The request has reached the maximum timeout before Google Play responds."
        @Kroll.constant const val CODE_SERVICE_UNAVAILABLE = SERVICE_UNAVAILABLE        // "Network connection is down."
        @Kroll.constant const val CODE_USER_CANCELED = USER_CANCELED                    // "User pressed back or canceled dialog."
        @Kroll.constant const val CODE_ERROR = ERROR                                    // "Fatal error during the API action."
        @Kroll.constant const val CODE_OK = OK
        @Kroll.constant const val CODE_BILLING_NOT_READY = 100                          // "Billing library not ready"
        @Kroll.constant const val CODE_SKU_NOT_AVAILABLE = 101                          // "SKU details not available for making purchase"
    }

    @Kroll.method
    fun initialize() {
        if (purchaseHandler == null) {
            purchaseHandler = PurchaseHandler(this as KrollProxy)
        }

        if (billingConnectionState == null) {
            billingConnectionState = BillingConnectionHandler(this as KrollProxy)
        }

        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(TiApplication.getInstance())
                    .setListener(purchaseHandler as PurchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build()
        }

        billingClient?.startConnection(billingConnectionState as BillingClientStateListener)
    }

    @Kroll.method
    fun getConnectionState(): Int? {
        billingClient?.let {
            return it.connectionState
        } ?: return null
    }

    @Kroll.method
    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
    }

    @Kroll.method
    fun isReady(): Boolean {
        return billingConnectionState?.isConnected == true && billingClient?.isReady == true
    }

    @Kroll.method
    fun isFeatureSupported(feature: String): Boolean {
        return isReady() && billingClient!!.isFeatureSupported(feature).responseCode == OK
    }

    @Kroll.method
    fun launchPriceChangeConfirmationFlow(args: KrollDict) {
        val callback = args[IAPConstants.Properties.CALLBACK] as KrollFunction?
        val productId = args[IAPConstants.PurchaseModelKeys.PRODUCT_ID] as String
        val skuDetails = ProductsHandler.getSkuDetails(productId) ?: return

        org.appcelerator.kroll.common.Log.w("Ti.IAP", "The \"launchPriceChangeConfirmationFlow\" method has been deprecated by Google and may be removed in the future.")

        val params = PriceChangeFlowParams.newBuilder().setSkuDetails(skuDetails).build()
        billingClient?.launchPriceChangeConfirmationFlow(TiApplication.getInstance().rootOrCurrentActivity, params) { billingResult ->
            val event = KrollDict()
            event[IAPConstants.Properties.SUCCESS] = billingResult.responseCode == OK
            event[IAPConstants.Properties.CODE] = billingResult.responseCode

            callback?.callAsync(getKrollObject(), event)
        }
    }

    private fun isBillingLibraryReady(args: KrollDict? = null): Boolean {
        if (!isReady()) {
            if (args?.containsKeyAndNotNull(IAPConstants.Properties.CALLBACK) == true) {
                val event = KrollDict()
                event[IAPConstants.Properties.SUCCESS] = false
                event[IAPConstants.Properties.CODE] = CODE_BILLING_NOT_READY

                val callback = args[IAPConstants.Properties.CALLBACK] as KrollFunction?
                callback?.callAsync(getKrollObject(), event)
            }

            return false
        }

        return true
    }

    @Kroll.method
    fun fetchLocalProductList(): Array<Any> {
        val productList = ArrayList<KrollDict>()

        for (skuModel in ProductsHandler.skuList) {
            productList.add(skuModel.modelData)
        }

        return productList.toTypedArray()
    }

    @Kroll.method
    fun fetchLocalPurchaseList(): Array<Any> {
        val purchaseList = ArrayList<KrollDict>()

        for (purchaseModel in PurchaseHandler.purchaseCatalog) {
            purchaseList.add(purchaseModel.modelData)
        }

        return purchaseList.toTypedArray()
    }

    @Kroll.method
    fun retrieveProductsInfo(args: KrollDict) {
        if (isBillingLibraryReady(args)) {
            QueryHandler.fetchProductsInfo(billingClient!!, args, getKrollObject())
        }
    }

    @Kroll.method
    fun purchase(productId: String): Int {
        if (!isBillingLibraryReady()) {
            return CODE_BILLING_NOT_READY
        }

        val skuDetails = ProductsHandler.getSkuDetails(productId) ?: return CODE_SKU_NOT_AVAILABLE

        val flowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
        val launchBillingResult = billingClient!!.launchBillingFlow(TiApplication.getAppCurrentActivity(), flowParams)

        return launchBillingResult.responseCode
    }

    @Kroll.method
    fun acknowledgeConsumableProduct(args: KrollDict) {
        if (isBillingLibraryReady(args)) {
            QueryHandler.acknowledgeConsumableProduct(billingClient!!, args, getKrollObject())
        }
    }

    @Kroll.method
    fun acknowledgeNonConsumableProduct(args: KrollDict) {
        if (isBillingLibraryReady(args)) {
            QueryHandler.acknowledgeNonConsumableProduct(billingClient!!, args, getKrollObject())
        }
    }

    @Kroll.method
    fun queryPurchases(args: KrollDict) {
        val callback = args["callback"] as KrollFunction
        val purchaseList = ArrayList<KrollDict>()
        val resultDict = KrollDict()
        resultDict[IAPConstants.Properties.SUCCESS] = false
        resultDict[IAPConstants.Properties.CODE] = CODE_BILLING_NOT_READY

        if (isReady()) {
            resultDict[IAPConstants.Properties.SUCCESS] = true
            resultDict[IAPConstants.Properties.CODE] = OK

            val productType = args.optString(IAPConstants.Properties.PRODUCT_TYPE, SKU_TYPE_INAPP)
            val params = QueryPurchasesParams.newBuilder().setProductType(productType).build()

            billingClient?.queryPurchasesAsync(params) { billingResult, purchasesList ->
                val event = KrollDict()
                if (billingResult.responseCode == OK) {
                    if (purchasesList.isNotEmpty()) {
                        for (purchase in purchasesList) {
                            purchaseList.add(PurchaseModel(purchase).modelData)
                        }
                    }
                    event["purchaseList"] = purchaseList.toTypedArray()
                    event["code"] = billingResult.responseCode
                    event["success"] = billingResult.responseCode == OK
                }
                callback.callAsync(getKrollObject(), event)
            }
        }
    }

    @Kroll.method
    fun queryProductDetails(args: KrollDict) {
        if (isBillingLibraryReady(args)) {
            val callback = args["callback"] as KrollFunction

            val productIdList = args.getStringArray(IAPConstants.Properties.PRODUCT_ID_LIST)
            val productType = args.optString(IAPConstants.Properties.PRODUCT_TYPE, SKU_TYPE_INAPP)

            val productList: ArrayList<Product> = ArrayList()
            for (productID in productIdList) {
                productList.add(
                    Product.newBuilder()
                        .setProductId(args.getString(productID))
                        .setProductType(productType)
                        .build()
                )
            }

            val productListKroll = ArrayList<KrollDict>()
            val queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

            billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                                 productDetailsList ->
                val event = KrollDict()
                if (billingResult.responseCode == OK) {
                    if (productDetailsList.isNotEmpty()) {
                        for (product in productDetailsList) {
                            productListKroll.add(ProductModel(product).modelData)
                        }
                    }
                    event["productList"] = productListKroll.toTypedArray()
                    event["code"] = billingResult.responseCode
                    event["success"] = billingResult.responseCode == OK
                }
                callback.callAsync(getKrollObject(), event)
            }
        }
    }
    @Kroll.method
    fun queryPurchasesAsync(args: KrollDict) {
        org.appcelerator.kroll.common.Log.e("Ti.IAP", "The \"queryPurchasesAsync\" API has been removed, use \"queryPurchases\" instead!")
    }

    @Kroll.method
    fun showInAppMessages(callback: KrollFunction) {
        val params = InAppMessageParams.newBuilder().build()
        billingClient?.showInAppMessages(TiApplication.getAppCurrentActivity(), params) { inAppMessageResult ->
            val event = KrollDict()
            event["code"] = inAppMessageResult.responseCode
            if (inAppMessageResult.responseCode == InAppMessageResult.InAppMessageResponseCode.NO_ACTION_NEEDED) {
                // The flow has finished and there is no action needed from developers.
            } else if (inAppMessageResult.responseCode == InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED) {
                event["purchaseToken"] = inAppMessageResult.purchaseToken
            }
            callback.callAsync(getKrollObject(), event)
        }
    }

    @Kroll.method
    fun queryPurchaseHistoryAsync(args: KrollDict) {
        if (isBillingLibraryReady(args)) {
            QueryHandler.queryPurchaseHistoryAsync(billingClient!!, args, getKrollObject())
        }
    }
}