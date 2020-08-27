package com.asinosoft.cdm.dialer

import java.util.*

class Stopwatch {

    private var startTime: Long = 0
    private var stopTime: Long = 0
    private var running = false

    /**
     * Запуск таймера
     */
    fun start() {
        startTime = System.currentTimeMillis()
        running = true
    }

    /**
     * Остановка таймера
     */
    fun stop() {
        stopTime = System.currentTimeMillis()
        running = false
    }


    fun getElapsedTime(): Long {
        return if (running) {
            System.currentTimeMillis() - startTime
        } else stopTime - startTime
    }


    fun getElapsedTimeSecs(): Long {
        return if (running) {
            (System.currentTimeMillis() - startTime) / 1000
        } else (stopTime - startTime) / 1000
    }


    fun getStringTime(): String? {
        val currentTime = getElapsedTime()
        var seconds = currentTime.toInt() / 1000
        var minutes = seconds / 60
        val hours = minutes / 60
        seconds -= minutes * 60
        minutes -= hours * 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

}