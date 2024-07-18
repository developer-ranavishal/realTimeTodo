package com.app.realmtodoapp.utils
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

class NetworkChangeCallback(private val listener: NetworkChangeListener) : ConnectivityManager.NetworkCallback() {

    interface NetworkChangeListener {
        fun onNetworkChanged(isConnected: Boolean)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        listener.onNetworkChanged(true)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        listener.onNetworkChanged(false)
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        listener.onNetworkChanged(isConnected)
    }
}
