package com.mintlifescience.app.homescreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.mintlifescience.app.R
import com.mintlifescience.app.helperUtils.FirebaseConstants
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.model.BrandItem

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userId: String? get() = PrefsManager.userId(getApplication())

    private val _items = MutableLiveData<List<BrandItem>>()
    val items: LiveData<List<BrandItem>> get() = _items

    init {
        _items.value = listOf(
            BrandItem("Mint Life Sciences Pvt Ltd", R.drawable.mint_life_sciences),
            BrandItem("USP Life Sciences", R.drawable.usp_life_sciences),
            BrandItem("USP Medicraft", R.drawable.usp_medicraft),
            BrandItem("Critical Care", R.drawable.critical_care),
            BrandItem("Gyno Care", R.drawable.gynocare),
            BrandItem("Bv-Clean", R.drawable.bv_clean)
        )
    }

    fun updateDoctorPresentationStatus(doctorName: String, isPresentation: Boolean) {
        val id = userId ?: run {
            Log.e("HomeViewModel", "User ID is null, cannot update presentation status.")
            return
        }
        FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)
            .child(id).child(FirebaseConstants.CLIENT)
            .child(FirebaseConstants.DOCTORS).child(doctorName)
            .child(FirebaseConstants.HAVE_PRESENTATION)
            .setValue(isPresentation)
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Failed to update presentation status for $doctorName", e)
            }
    }
}
