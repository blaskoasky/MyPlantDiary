package com.blaskoasky.iri.myplantdiary.dto

data class Specimen(
    var plantName: String = "",
    var latitude: String = "",
    var longitude: String = "",
    var description: String = "",
    var datePlanted: String = "",
    var specimenId: String = "",
    var plantId: Int = 0
) {
    override fun toString(): String {
        return "$plantName , $description , $latitude , $longitude"
    }
}
