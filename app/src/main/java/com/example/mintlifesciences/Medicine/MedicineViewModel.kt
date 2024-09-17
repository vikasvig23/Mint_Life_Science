package com.example.mintlifesciences.Medicine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mintlifesciences.model.Medicine
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MedicineViewModel : ViewModel() {

    private val _medicineList = MutableLiveData<List<Medicine>>()
    val medicineList: LiveData<List<Medicine>> get() = _medicineList

    private var lastKey: String? = null
    private var isLoading = false // To avoid multiple fetch calls
    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Admin")

    // Backup list to store the full medicine list
    private val _fullMedicineList: MutableLiveData<List<Medicine>> = MutableLiveData(emptyList())

    fun fetchMedicines(brandName: String, isLoadMore: Boolean = false) {
        // Avoid fetching more data if we're already loading
        if (isLoading) return
        isLoading = true

        val query = if (lastKey == null) {
            database.child(brandName).limitToFirst(20) // Fetch the first page of data
        } else {
            database.child(brandName).orderByKey().startAfter(lastKey).limitToFirst(20) // Fetch the next page
        }

        query.get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<Medicine>()
            for (medicineSnapshot in snapshot.children) {
                val medicine = medicineSnapshot.getValue(Medicine::class.java)
                if (medicine != null) {
                    list.add(medicine)
                }
            }

            if (snapshot.childrenCount > 0) {
                lastKey = snapshot.children.lastOrNull()?.key // Update the lastKey for pagination
            }

            // Update both _medicineList and _fullMedicineList, appending if it's a "load more" action
            if (isLoadMore) {
                val currentList = _medicineList.value?.toMutableList() ?: mutableListOf()
                currentList.addAll(list)
                _medicineList.value = currentList
                _fullMedicineList.value = currentList
            } else {
                _medicineList.value = list
                _fullMedicineList.value = list
            }

            isLoading = false
        }.addOnFailureListener {
            it.printStackTrace()
            isLoading = false
        }
    }

    fun filterMedicines(query: String) {
        val trimmedQuery = query.trim()
        val filteredList = if (trimmedQuery.isEmpty()) {
            _fullMedicineList.value ?: emptyList()
        } else {
            _fullMedicineList.value?.filter {
                it.name?.contains(trimmedQuery, ignoreCase = true) ?: false
            } ?: emptyList()
        }
        _medicineList.value = filteredList
    }

    fun resetPagination() {
        lastKey = null
    }
}
