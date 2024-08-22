package com.example.mintlifesciences.doctorMedicine

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class DocMedicineViewModel(application: Application):AndroidViewModel(application){

    lateinit var activity: DoctorMedicineActivity

    fun init(activity: DoctorMedicineActivity){


    }

}