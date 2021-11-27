package com.blaskoasky.iri.myplantdiary.dto

data class Plant(var genus: String, var species : String, var common :String, var plantId:Int = 0) {
    override fun toString(): String {
        return common
    }
}