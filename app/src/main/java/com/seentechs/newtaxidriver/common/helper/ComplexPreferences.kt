package com.seentechs.newtaxidriver.common.helper

import android.content.Context
import android.content.SharedPreferences

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class ComplexPreferences private constructor(private val context: Context, namePreferences: String?, mode: Int) {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor
    internal var typeOfObject = object : TypeToken<Any>() {

    }.type

    init {
        var namePreference = namePreferences
        if (namePreference == null || namePreference == "") {
            namePreference = "complex_preferences"
        }
        preferences = context.getSharedPreferences(namePreference, mode)
        editor = preferences.edit()
    }


    fun clearSharedPreferences() {
        editor.clear()
        editor.commit()
        editor.apply()
    }

    fun putObject(key: String?, `object`: Any?) {
        if (`object` == null) {
            throw IllegalArgumentException("object is null")
        }

        if (key == "" || key == null) {
            throw IllegalArgumentException("key is empty or null")
        }

        editor.putString(key, GSON.toJson(`object`))
    }

    fun commit() {
        editor.commit()
    }

    fun <T> getObject(key: String, a: Class<T>): T? {

        val gson = preferences.getString(key, null)
        return if (gson == null) {
            null
        } else {
            try {
                GSON.fromJson(gson, a)
            } catch (e: Exception) {
                throw IllegalArgumentException("Object storaged with key $key is instanceof other class")
            }

        }
    }

    companion object {

        private var complexPreferences: ComplexPreferences ? = null
        private val GSON = Gson()

        fun getComplexPreferences(context: Context,
                                  namePreferences: String, mode: Int): ComplexPreferences {

            if (complexPreferences == null) {
                complexPreferences = ComplexPreferences(context,
                        namePreferences, mode)
            }

            return complexPreferences!!
        }
    }


}
