package com.example.livetrackingapp.utils

import android.content.Context
import com.example.livetrackingapp.model.UserModel

class UserSharedPreference private constructor() {
    fun userLogin(user: UserModel, context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(UserModel.UserEnum.USER.name, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString(UserModel.UserEnum.email.name, user.email)
        editor.putString(UserModel.UserEnum.password.name, user.password)
        editor.putString(UserModel.UserEnum.userName.name, user.userName)
        editor.apply()
    }

    fun getUser(context: Context): UserModel {
        val sharedPreferences =
            context.getSharedPreferences(UserModel.UserEnum.USER.name, Context.MODE_PRIVATE)
        return UserModel(
            sharedPreferences.getString(UserModel.UserEnum.userName.name, ""),
            sharedPreferences.getString(UserModel.UserEnum.email.name, ""),
            sharedPreferences.getString(UserModel.UserEnum.password.name, "")
        )
    }

    fun isLoggedIn(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(UserModel.UserEnum.USER.name, Context.MODE_PRIVATE)
        return sharedPreferences.getString(UserModel.UserEnum.userName.name, null) != null
    }

    fun logout(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(UserModel.UserEnum.USER.name, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun userUpdate(user: UserModel, context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(UserModel.UserEnum.USER.name, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(UserModel.UserEnum.userName.name, user.userName)
        editor.apply()
    }

    companion object {
        private var mInstance: UserSharedPreference? = null

        @get:Synchronized
        val instance: UserSharedPreference?
            get() {
                if (mInstance == null) mInstance = UserSharedPreference()
                return mInstance
            }
    }
}