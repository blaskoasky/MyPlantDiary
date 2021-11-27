package com.blaskoasky.iri.myplantdiary.service

import androidx.lifecycle.MutableLiveData
import com.blaskoasky.iri.myplantdiary.dto.Plant

class PlantService {

    fun fetchPlants(plantName: String): MutableLiveData<ArrayList<Plant>> {
        return MutableLiveData<ArrayList<Plant>>()
    }
}