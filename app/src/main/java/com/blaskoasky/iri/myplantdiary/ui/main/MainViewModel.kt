package com.blaskoasky.iri.myplantdiary.ui.main

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blaskoasky.iri.myplantdiary.dto.Photo
import com.blaskoasky.iri.myplantdiary.dto.Plant
import com.blaskoasky.iri.myplantdiary.dto.Specimen
import com.blaskoasky.iri.myplantdiary.service.PlantService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

class MainViewModel : ViewModel() {

    private var _plants: MutableLiveData<ArrayList<Plant>> = MutableLiveData<ArrayList<Plant>>()
    private var _specimens: MutableLiveData<ArrayList<Specimen>> =
        MutableLiveData<ArrayList<Specimen>>()

    private var plantService: PlantService = PlantService()
    private var firestore: FirebaseFirestore

    private var storageReference = FirebaseStorage.getInstance().reference


    init {
        fetchPlants("e")
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenToSpecimens()
    }

    //  >>>>> JANGAN SENTUH FUNCTION INI <<<<<
    private fun listenToSpecimens() {
        firestore.collection("specimens").addSnapshotListener { snapshot, e ->
            // if there is an exception we want to skip.
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            // if we are here, we did not encounter an exception
            if (snapshot != null) {
                // now, we have a populated snapshot
                val allSpecimens = ArrayList<Specimen>()
                val documents = snapshot.documents
                documents.forEach {
                    val specimen = it.toObject(Specimen::class.java)
                    if (specimen != null) {
                        specimen.specimenId = it.id
                        allSpecimens.add(specimen)
                    }
                }
                _specimens.value = allSpecimens
            }
        }
    }

    fun fetchPlants(plantName: String) {
        plants = plantService.fetchPlants(plantName)
    }

    fun save(
        specimen: Specimen,
        photos: java.util.ArrayList<Photo>,
        user: FirebaseUser
    ) {

        val document = firestore.collection("specimens").document()
        specimen.specimenId = document.id

        val set = document.set(specimen)
        set.addOnSuccessListener {
            Log.d("Firebase Save", "document Saved")

            if (photos != null && photos.size > 0) {
                savePhotos(specimen, photos, user)
            }
        }
        set.addOnFailureListener {
            Log.d("Firebase Save", "Save Failed")
        }
    }

    private fun savePhotos(
        specimen: Specimen,
        photos: ArrayList<Photo>,
        user: FirebaseUser
    ) {
        val collection = firestore.collection("specimens")
            .document(specimen.specimenId)
            .collection("photos")

        photos.forEach { photo ->
            val task = collection.add(photo)
            task.addOnSuccessListener {
                photo.id = it.id
                uploadPhotos(specimen, photos, user)
            }
        }

    }

    private fun uploadPhotos(
        specimen: Specimen,
        photos: ArrayList<Photo>,
        user: FirebaseUser
    ) {

        photos.forEach { photo ->
            var uri = Uri.parse(photo.localUri)
            val imageRef = storageReference.child("images/" + user.uid + "/" + uri.lastPathSegment)
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                val downloadUrl = imageRef.downloadUrl
                downloadUrl.addOnSuccessListener {
                    photo.remoteUri = it.toString()

                    //update firestore with public image uri
                    updatePhotoDatabase(specimen, photo)
                }
            }

            uploadTask.addOnFailureListener {
                Log.e(TAG, it.message.toString())
            }

        }

    }

    private fun updatePhotoDatabase(specimen: Specimen, photo: Photo) {
        firestore.collection("specimens")
            .document(specimen.specimenId)
            .collection("photos")
            .document(photo.id)
            .set(photo)

    }

    // this for giving and receiving data to public
    internal var plants: MutableLiveData<ArrayList<Plant>>
        get() {
            return _plants
        }
        set(value) {
            _plants = value
        }

    internal var specimen: MutableLiveData<ArrayList<Specimen>>
        get() {
            return _specimens
        }
        set(value) {
            _specimens = value
        }

}