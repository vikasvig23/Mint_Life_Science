package com.example.mintlifesciences.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class NetworkChangeReceiver(private val onNetworkAvailable: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val isConnected = network != null && connectivityManager.getNetworkCapabilities(network) != null

        if (isConnected) {
            onNetworkAvailable() // Trigger the callback when the network is available
        }
    }
}
