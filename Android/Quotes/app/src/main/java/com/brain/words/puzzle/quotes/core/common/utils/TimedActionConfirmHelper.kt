package com.brain.words.puzzle.quotes.core.common.utils

import android.os.Handler
import android.os.Message

class TimedActionConfirmHelper(private val timeOut: Long) : Handler() {

    private var timerRunning: Boolean = false

    private var listener: (() -> Unit)? = null

    override fun handleMessage(msg: Message) {
        if (msg.what == MSG_TIMED_OUT) {
            timerRunning = false
        }
        super.handleMessage(msg)
    }

    fun setListener(listener: (() -> Unit)) {
        this.listener = listener
    }

    fun onAction(): Boolean {
        removeMessages(MSG_TIMED_OUT)
        if (timerRunning) {
            listener?.invoke()
            timerRunning = false
        } else {
            sendEmptyMessageDelayed(MSG_TIMED_OUT, timeOut)
            timerRunning = true
        }
        return timerRunning
    }

    companion object {
        const val TIMEOUT_DEFAULT: Long = 2000
        private const val MSG_TIMED_OUT = 1
    }
}