package com.example.mintlifesciences.login

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.example.mintlifesciences.R
import com.example.mintlifesciences.Utility

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var activity: LoginActivity

    fun init(activity: LoginActivity){
        this.activity=activity
        activity.binding.btn.background=Utility.createGeadientDrawable(25f,ContextCompat.getColor(activity, R.color.purple_500,),ContextCompat.getColor(activity, R.color.purple_500))

    }

}