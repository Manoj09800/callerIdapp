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
[11-08-2026 01:29 PM] Kumar: package com.example.calleridapp

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    fun lookupNumber(number: String, callback: (name: String?, isSpam: Boolean) -> Unit) {
        db.collection("numbers").document(number)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val names = doc.get("names") as? List<*>
                    val mostCommonName = names?.firstOrNull()?.toString()
                    val reportCount = (doc.getLong("reportCount") ?: 0L).toInt()
                    val isSpam = reportCount >= 5
                    callback(mostCommonName, isSpam)
                } else {
                    callback(null, false)
                }
            }
            .addOnFailureListener {
                callback(null, false)
            }
    }

    fun uploadContact(number: String, name: String) {
        val docRef = db.collection("numbers").document(number)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val names = (snapshot.get("names") as? MutableList<String>) ?: mutableListOf()
            if (!names.contains(name)) names.add(0, name)
            transaction.set(docRef, mapOf("names" to names), com.google.firebase.firestore.SetOptions.merge())
        }
    }

    fun reportSpam(number: String) {
        val docRef = db.collection("numbers").document(number)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val current = (snapshot.getLong("reportCount") ?: 0L)
            transaction.set(docRef, mapOf("reportCount" to current + 1), com.google.firebase.firestore.SetOptions.merge())
        }
    }
}
