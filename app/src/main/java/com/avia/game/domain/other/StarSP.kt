package com.avia.game.domain.other

import android.content.Context

class StarSP(context: Context) {
    private val sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE)

    fun getBalance(): Int = sp.getInt("BALANCE", 0)

    fun setBalance(value: Int) = sp.edit().putInt("BALANCE", getBalance() + value).apply()
}