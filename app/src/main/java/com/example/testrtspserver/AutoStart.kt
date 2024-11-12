package com.example.testrtspserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.apply
import kotlin.jvm.java

class AutoStart : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                context?.apply {
                    val it = Intent(this, MainActivity::class.java)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    this.startActivity(it)
                }
            }
        }
    }
}