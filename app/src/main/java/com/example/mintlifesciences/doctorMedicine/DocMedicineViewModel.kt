package com.example.mintlifesciences.doctorMedicine

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mintlifesciences.Model.Medicine
import kotlinx.coroutines.launch

class DocMedicineViewModel(application: Application) : AndroidViewModel(application) {

    // Use LiveData to observe changes to the selected medicines
    private val _selectedMedicines = MutableLiveData<MutableList<Medicine>>()
    val selectedMedicines: LiveData<MutableList<Medicine>> get() = _selectedMedicines

    init {
        _selectedMedicines.value = mutableListOf()
    }

    // Function to add a medicine to the list
    fun addMedicine(medicine: Medicine) {
        val updatedList = _selectedMedicines.value ?: mutableListOf()
        updatedList.add(medicine)
        _selectedMedicines.value = updatedList

        // Log the addition of the medicine
        Log.d("DocMedicineViewModel", "Medicine Added: ${medicine.name}")
    }

    // Function to remove a medicine from the list
    fun removeMedicine(medicine: Medicine) {
        val updatedList = _selectedMedicines.value ?: mutableListOf()
        updatedList.remove(medicine)
        _selectedMedicines.value = updatedList

        // Log the removal of the medicine
        Log.d("DocMedicineViewModel", "Medicine Removed: ${medicine.name}")
    }

    // Function to clear the list
    fun clearMedicines() {
        _selectedMedicines.value?.clear()
        _selectedMedicines.value = _selectedMedicines.value
    }

    // Use viewModelScope for coroutine operations
    fun performSomeOperation() {
        viewModelScope.launch {
            // Perform asynchronous tasks here
        }
    }
}
