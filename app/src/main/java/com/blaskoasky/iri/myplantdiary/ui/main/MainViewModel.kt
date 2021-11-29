package com.blaskoasky.iri.myplantdiary.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blaskoasky.iri.myplantdiary.dto.Plant
import com.blaskoasky.iri.myplantdiary.service.PlantService

class MainViewModel : ViewModel() {

    private var _plants: MutableLiveData<ArrayList<Plant>> = MutableLiveData<ArrayList<Plant>>()
    var plantService: PlantService = PlantService()

    init {
        fetchPlants("e")
    }

    fun fetchPlants(plantName: String) {
        plants = plantService.fetchPlants(plantName)
    }

    var plants : MutableLiveData<ArrayList<Plant>>
        get() { return _plants }
        set(value) { _plants = value }

}