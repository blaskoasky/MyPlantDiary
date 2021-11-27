package com.blaskoasky.iri.myplantdiary.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blaskoasky.iri.myplantdiary.dto.Plant
import com.blaskoasky.iri.myplantdiary.service.PlantService

class MainViewModel : ViewModel() {

    var plants: MutableLiveData<ArrayList<Plant>> = MutableLiveData<ArrayList<Plant>>()
    var plantService: PlantService = PlantService()

    fun fetchPlants(plantName: String) {
        plants = plantService.fetchPlants(plantName)

    }
}