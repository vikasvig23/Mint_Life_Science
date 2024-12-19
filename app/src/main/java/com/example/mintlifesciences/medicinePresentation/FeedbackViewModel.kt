package com.example.mintlifesciences.medicinePresentation

import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class FeedbackViewModel(application: Application) : AndroidViewModel(application) {

    val selectedDate: MutableLiveData<String> = MutableLiveData()
}