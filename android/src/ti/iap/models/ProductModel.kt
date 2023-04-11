package ti.iap.models

import com.android.billingclient.api.ProductDetails
import org.appcelerator.kroll.KrollDict

class ProductModel(val product: ProductDetails) {
    companion object {
        @JvmStatic fun createProductData(product: ProductDetails): KrollDict {
            val modelDict = KrollDict()

            modelDict["productId"] = product.productId
            modelDict["productType"] = product.productType
            modelDict["name"] = product.name
            modelDict["description"] = product.description
            modelDict["title"] = product.title
            modelDict["formattedPrice"] = product.oneTimePurchaseOfferDetails?.formattedPrice
            modelDict["priceCurrencyCode"] = product.oneTimePurchaseOfferDetails?.priceCurrencyCode
            modelDict["priceAmountMicros"] = product.oneTimePurchaseOfferDetails?.priceAmountMicros
            if (product.subscriptionOfferDetails?.size!! > 0) {
                modelDict["offerId"] = product.subscriptionOfferDetails?.get(0)?.offerId
                modelDict["offerTags"] = product.subscriptionOfferDetails?.get(0)?.offerTags
                modelDict["offerToken"] = product.subscriptionOfferDetails?.get(0)?.offerToken
                modelDict["basePlanId"] = product.subscriptionOfferDetails?.get(0)?.basePlanId
            }
            return modelDict
        }
    }

    val modelData: KrollDict get() {
        return createProductData(this.product)
    }
}