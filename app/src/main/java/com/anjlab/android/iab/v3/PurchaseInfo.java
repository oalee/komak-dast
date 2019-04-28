/**
 * Copyright 2014 AnjLab and Unic8
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anjlab.android.iab.v3;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * With this PurchaseInfo a developer is able verify
 * a purchase from the google play store on his own
 * server. An example implementation of how to verify
 * a purchase you can find <a href="https://github.com/mgoldsborough/google-play-in-app-billing-verification/blob/master/library/GooglePlay/InAppBilling/GooglePlayResponseValidator.php#L64">here</a>
 */
public class PurchaseInfo implements Parcelable {

    private static final String LOG_TAG = "iabv3.purchaseInfo";

    public enum PurchaseState {
        PurchasedSuccessfully, Canceled, Refunded, SubscriptionExpired;
    }

    public final String responseData;
    public final String signature;

    public PurchaseInfo(String responseData, String signature) {
        this.responseData = responseData;
        this.signature = signature;
    }

    public static class ResponseData implements Parcelable {

        public String orderId;
        public String packageName;
        public String productId;
        public Date purchaseTime;
        public PurchaseState purchaseState;
        public String developerPayload;
        public String purchaseToken;
        public boolean autoRenewing;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.orderId);
            dest.writeString(this.packageName);
            dest.writeString(this.productId);
            dest.writeLong(purchaseTime != null ? purchaseTime.getTime() : -1);
            dest.writeInt(this.purchaseState == null ? -1 : this.purchaseState.ordinal());
            dest.writeString(this.developerPayload);
            dest.writeString(this.purchaseToken);
            dest.writeByte(autoRenewing ? (byte) 1 : (byte) 0);
        }

        public ResponseData() {
        }

        protected ResponseData(Parcel in) {
            this.orderId = in.readString();
            this.packageName = in.readString();
            this.productId = in.readString();
            long tmpPurchaseTime = in.readLong();
            this.purchaseTime = tmpPurchaseTime == -1 ? null : new Date(tmpPurchaseTime);
            int tmpPurchaseState = in.readInt();
            this.purchaseState = tmpPurchaseState == -1 ? null : PurchaseState.values()[tmpPurchaseState];
            this.developerPayload = in.readString();
            this.purchaseToken = in.readString();
            this.autoRenewing = in.readByte() != 0;
        }

        public static final Creator<ResponseData> CREATOR = new Creator<ResponseData>() {
            public ResponseData createFromParcel(Parcel source) {
                return new ResponseData(source);
            }

            public ResponseData[] newArray(int size) {
                return new ResponseData[size];
            }
        };
    }

    public static PurchaseState getPurchaseState(int state) {
        switch (state) {
            case 0:
                return PurchaseState.PurchasedSuccessfully;
            case 1:
                return PurchaseState.Canceled;
            case 2:
                return PurchaseState.Refunded;
            case 3:
                return PurchaseState.SubscriptionExpired;
            default:
                return PurchaseState.Canceled;
        }
    }

    public ResponseData parseResponseData() {
        try {
            JSONObject json = new JSONObject(responseData);
            ResponseData data = new ResponseData();
            data.orderId = json.optString("orderId");
            data.packageName = json.optString("packageName");
            data.productId = json.optString("productId");
            long purchaseTimeMillis = json.optLong("purchaseTime", 0);
            data.purchaseTime = purchaseTimeMillis != 0 ? new Date(purchaseTimeMillis) : null;
            data.purchaseState = getPurchaseState(json.optInt("purchaseState", 1));
            data.developerPayload = json.optString("developerPayload");
            data.purchaseToken = json.getString("purchaseToken");
            data.autoRenewing = json.optBoolean("autoRenewing");
            return data;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to parse response data", e);
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.responseData);
        dest.writeString(this.signature);
    }

    protected PurchaseInfo(Parcel in) {
        this.responseData = in.readString();
        this.signature = in.readString();
    }

    public static final Creator<PurchaseInfo> CREATOR = new Creator<PurchaseInfo>() {
        public PurchaseInfo createFromParcel(Parcel source) {
            return new PurchaseInfo(source);
        }

        public PurchaseInfo[] newArray(int size) {
            return new PurchaseInfo[size];
        }
    };
}
