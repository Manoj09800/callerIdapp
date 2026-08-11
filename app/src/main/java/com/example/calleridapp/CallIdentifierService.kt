[11-08-2026 01:23 PM] Kumar: <?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="CallerID App"
        android:theme="@style/Theme.CallerIDApp">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".CallIdentifierService"
            android:permission="android.permission.BIND_SCREENING_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.telecom.CallScreeningService" />
            </intent-filter>
        </service>

        <service
            android:name=".OverlayService"
            android:enabled="true"
            android:exported="false" />

    </application>
</manifest>
[11-08-2026 01:35 PM] Kumar: package com.example.calleridapp

import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService

class CallIdentifierService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: return

        FirestoreHelper.lookupNumber(phoneNumber) { name, isSpam ->
            val displayName = name ?: "Unknown"

            val overlayIntent = Intent(this, OverlayService::class.java).apply {
                putExtra("number", phoneNumber)
                putExtra("name", displayName)
                putExtra("isSpam", isSpam)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startService(overlayIntent)
        }

        val response = CallResponse.Builder().build()
        respondToCall(callDetails, response)
    }
}
