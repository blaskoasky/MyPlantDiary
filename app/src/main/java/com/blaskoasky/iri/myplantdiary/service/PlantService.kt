package com.blaskoasky.iri.myplantdiary.service

import androidx.lifecycle.MutableLiveData
import com.blaskoasky.iri.myplantdiary.RetrofitClientInstance
import com.blaskoasky.iri.myplantdiary.dao.IPlantDAO
import com.blaskoasky.iri.myplantdiary.dto.Plant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlantService {

    fun fetchPlants(plantName: String): MutableLiveData<ArrayList<Plant>> {
        val _plants = MutableLiveData<ArrayList<Plant>>()
        val service = RetrofitClientInstance.retrofitInstance?.create(IPlantDAO::class.java)

        val call = service?.getAllPlants()
        call?.enqueue(object : Callback<ArrayList<Plant>> {
            override fun onResponse(
                call: Call<ArrayList<Plant>>,
                response: Response<ArrayList<Plant>>
            ) {
                _plants.value = response.body()
            }

            override fun onFailure(call: Call<ArrayList<Plant>>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

        return _plants
    }
}