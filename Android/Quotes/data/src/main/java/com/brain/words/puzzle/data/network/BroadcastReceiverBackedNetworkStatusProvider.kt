package com.brain.words.puzzle.data.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class BroadcastReceiverBackedNetworkStatusProvider(
    private val context: Context
) : NetworkStatusProvider {

    private val connectedFlowable = Flowable.create<Boolean>({ emitter ->
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val connectivityChangeBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                emitter.onNext(connectivityManager.isConnected())
            }
        }

        emitter.onNext(connectivityManager.isConnected())

        context.registerReceiver(connectivityChangeBroadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        emitter.setCancellable {
            context.unregisterReceiver(connectivityChangeBroadcastReceiver)
        }
    }, BackpressureStrategy.LATEST)

    override fun isConnected(): Flowable<Boolean> = connectedFlowable

    private fun ConnectivityManager.isConnected() = activeNetworkInfo?.isConnected == true
}