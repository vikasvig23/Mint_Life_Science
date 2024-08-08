package com.example.mintlifesciences.homescreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class HomeViewModel (application: Application) : AndroidViewModel(application) {
    lateinit var activity: HomeActivity
    private val _items = MutableLiveData<List<String>>()
    val items: LiveData<List<String>> get() = _items

    fun init(activity:HomeActivity) {
        this.activity=activity

        _items.value = listOf("Mini Life Sciences Pvt Ltd", "USP Life Sciences", "USP Medicraft", "Critical Care", "Gyno Care", "Bv-Clean")
    }
}