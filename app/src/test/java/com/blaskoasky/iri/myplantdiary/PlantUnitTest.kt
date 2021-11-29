package com.blaskoasky.iri.myplantdiary

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.blaskoasky.iri.myplantdiary.dto.Plant
import com.blaskoasky.iri.myplantdiary.service.PlantService
import com.blaskoasky.iri.myplantdiary.ui.main.MainViewModel
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class PlantUnitTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    lateinit var mvm: MainViewModel

    var plantService = mockk<PlantService>()


    @Test
    fun searchRedbud_returnsRedbud() {
        givenAFeedofPlantMockedDataAreAvailable()
        whenSearchForRedbud()
        thenResultContainsEasternRedbud()
        thenVerifyFunctionsInvoked()
    }

    private fun thenVerifyFunctionsInvoked() {
        verify { plantService.fetchPlants("Redbud") }
        verify(exactly = 0) { plantService.fetchPlants("Kambing") }
        confirmVerified(plantService)
    }

    private fun givenAFeedofPlantMockedDataAreAvailable() {
        mvm = MainViewModel()
        createMockData()
    }

    //Buat Dummy Data
    private fun createMockData() {
        val allPlantsLiveData = MutableLiveData<ArrayList<Plant>>()
        val allPlant = ArrayList<Plant>()

        val redbud = Plant("Cercis", "canadensis", "Eastern Redbud")
        allPlant.add(redbud)
        allPlantsLiveData.postValue(allPlant)

        every { plantService.fetchPlants(or("Redbud", "kambing")) } returns allPlantsLiveData
        every {
            plantService.fetchPlants(
                not(
                    or(
                        "Redbud",
                        "kambing"
                    )
                )
            )
        } returns MutableLiveData<ArrayList<Plant>>()

        mvm.plantService = plantService
    }

    private fun whenSearchForRedbud() {
        mvm.fetchPlants("Redbud")
    }

    private fun thenResultContainsEasternRedbud() {
        var redbudFound = false

        mvm.plants.observeForever {
            assertNotNull(it)
            assertTrue(it.size > 0)
            it.forEach {
                if (it.genus == "Cercis" && it.species == "canadensis" && it.common.contains("Eastern Redbud"))
                    redbudFound = true
            }
        }
        assertTrue(redbudFound)
    }

    @Test
    fun confirmEasternRedbud_outputsEasternRedbud() {
        val plant = Plant("Cercis", "canadesis", "Eastern Redbud")
        assertEquals("Eastern Redbud", plant.toString())
    }


    //Test search asal
    @Test
    fun searchForNothing_returnsNothing() {
        givenAFeedofPlantMockedDataAreAvailable()
        whenSearchforNothing()
        zeroResults()
    }

    private fun whenSearchforNothing() {
        mvm.fetchPlants("kajshfkajhsf")
    }

    private fun zeroResults() {
        mvm.plants.observeForever {
            assertEquals(0, it.size)
        }

    }


}
