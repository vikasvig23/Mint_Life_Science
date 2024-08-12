package com.example.mintlifesciences.addDoctor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AddDoctorViewModel(application: Application):AndroidViewModel(application) {
    lateinit var activity: AddDoctorActivity

    private var _docDate= MutableLiveData<List<DoctorData>>()
    val docData:LiveData<List<DoctorData>> get()=_docDate


    fun init(activity: AddDoctorActivity){
        _docDate.value= mutableListOf()
    }

    fun addDoctor(doctor:DoctorData){

    }
}