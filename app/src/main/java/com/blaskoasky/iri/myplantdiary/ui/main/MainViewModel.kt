package com.blaskoasky.iri.myplantdiary.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blaskoasky.iri.myplantdiary.dto.Plant
import com.blaskoasky.iri.myplantdiary.dto.Specimen
import com.blaskoasky.iri.myplantdiary.service.PlantService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainViewModel : ViewModel() {

    private var _plants: MutableLiveData<ArrayList<Plant>> = MutableLiveData<ArrayList<Plant>>()
    private var plantService: PlantService = PlantService()
    private lateinit var firestore: FirebaseFirestore


    init {
        fetchPlants("e")
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun fetchPlants(plantName: String) {
        plants = plantService.fetchPlants(plantName)
    }

    fun save(specimen: Specimen) {

        val document = firestore.collection("specimens").document()

        val set = document.set(specimen)
        set.addOnSuccessListener {
            specimen.specimenId = document.id
            Log.d("Firebase Save", "document Saved")
        }
        set.addOnFailureListener {
            Log.d("Firebase Save", "Save Failed")
        }
    }

    // this for giving and receiving data to public
    internal var plants: MutableLiveData<ArrayList<Plant>>
        get() {
            return _plants
        }
        set(value) {
            _plants = value
        }

}