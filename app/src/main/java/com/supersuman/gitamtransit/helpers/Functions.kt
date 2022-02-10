package com.supersuman.gitamtransit.helpers

import android.content.SharedPreferences
import org.json.JSONArray

fun updateStarsList(starsList : MutableList<String>, sharedPreferences: SharedPreferences){
    val prefsEditor = sharedPreferences.edit()
    val jsonArray = JSONArray()
    starsList.forEach {
        jsonArray.put(it)
    }
    val string = jsonArray.toString()
    prefsEditor?.putString("Stars", string)
    prefsEditor?.apply()
}

fun getStarsList(sharedPreferences: SharedPreferences): MutableList<String> {
    val string = sharedPreferences.getString("Stars", "")
    if (string == "") return mutableListOf()
    val jsonArray = JSONArray(string)
    val mutableList : MutableList<String> = mutableListOf()
    for (i in 0 until jsonArray.length()){
        mutableList.add(jsonArray.getString(i))
    }
    return mutableList
}