package com.example.mintlifesciences.addDoctor

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mintlifesciences.R
import com.example.mintlifesciences.Utility

class AddDoctorViewModel(application: Application):AndroidViewModel(application) {
    lateinit var activity: AddDoctorActivity

    private var _docDate= MutableLiveData<List<DoctorData>>()
    val docData:LiveData<List<DoctorData>> get()=_docDate


    fun init(activity: AddDoctorActivity){
        this.activity=activity
        activity.binding.btn.background= Utility.createGeadientDrawable(25f,
            ContextCompat.getColor(activity, R.color.purple_500,),
            ContextCompat.getColor(activity, R.color.purple_500))

        _docDate.value= mutableListOf()
    }

    fun addDoctor(doctor:DoctorData){
        val updatedList= _docDate.value?.toMutableList()
        updatedList?.add(doctor)
        _docDate.value=updatedList

    }
}