package com.example.mintlifesciences.signUp

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.example.mintlifesciences.R
import com.example.mintlifesciences.Utility

class SignUpViewModel (application: Application) : AndroidViewModel(application){

    lateinit var activity:SignUpActivity

    fun init(activity: SignUpActivity){
        this.activity=activity
        activity.binding.btn.background=Utility.createGeadientDrawable(25f,ContextCompat.getColor(activity,R.color.purple_500),ContextCompat.getColor(activity,R.color.purple_500))
    }

}