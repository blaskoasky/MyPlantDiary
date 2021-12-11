package com.blaskoasky.iri.myplantdiary.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blaskoasky.iri.myplantdiary.R
import com.blaskoasky.iri.myplantdiary.databinding.MainFragmentBinding
import com.blaskoasky.iri.myplantdiary.dto.Photo
import com.blaskoasky.iri.myplantdiary.dto.Plant
import com.blaskoasky.iri.myplantdiary.dto.Specimen
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        const val PERMISSION_REQUEST_CAMERA = 3001
        const val CAMERA_REQUEST_CODE = 3002
        const val SAVE_PHOTO_IMAGE_REQUEST_CODE = 3003
        const val IMAGE_GALLERY_REQUEST = 3004
        const val PERMISSION_REQUEST_LOCATION = 3005
        const val AUTH_REQUEST_CODE = 3006
    }

    private lateinit var mainFragmentBinding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var locationViewModel: LocationViewModel

    private var _plantId = 0
    private var user: FirebaseUser? = null
    private var photos: ArrayList<Photo> = ArrayList<Photo>()
    private var photoURI: Uri? = null
    private lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainFragmentBinding = MainFragmentBinding.inflate(inflater, container, false)
        return mainFragmentBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // LiveData Observer autocomplete
        viewModel.plants.observe(viewLifecycleOwner, { plants ->
            // adding autocomplete
            mainFragmentBinding.actPlantName.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    plants
                )
            )
        })

        //Live data receiving data
        viewModel.specimen.observe(viewLifecycleOwner, { specimens ->
            mainFragmentBinding.spnSpeciments.adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                specimens
            )
        })

        mainFragmentBinding.actPlantName.setOnItemClickListener { parent, view, position, id ->
            val selectedPlant = parent.getItemAtPosition(position) as Plant
            _plantId = selectedPlant.plantId
        }


        mainFragmentBinding.btnTakePhoto.setOnClickListener {
            prepTakePhoto()
        }

        mainFragmentBinding.btnGallery.setOnClickListener {
            prepOpenGallery()
        }
        mainFragmentBinding.btnSave.setOnClickListener {
            savePlant()
        }

        mainFragmentBinding.imgView.setOnClickListener {
            prepOpenGallery()
        }

        mainFragmentBinding.btnLogon.setOnClickListener {
            logon()
        }

        prepRequestLocationUpdates()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    Toast.makeText(
                        context,
                        "Unable to take photo without permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            PERMISSION_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationUpdates()
                } else {
                    Toast.makeText(
                        context,
                        "Unable to find location without permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // BUKA CAMERA PERMISSION
    private fun prepTakePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePhoto()
        } else {
            val permissionRequest = arrayOf(android.Manifest.permission.CAMERA)
            requestPermissions(permissionRequest, PERMISSION_REQUEST_CAMERA)
        }
    }

    // AMBIL POTO
    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)
            if (takePictureIntent != null) {
                val photoFile: File = createImageFile()

                photoFile.also {
                    photoURI = FileProvider.getUriForFile(
                        requireActivity().applicationContext,
                        "com.blaskoasky.iri.myplantdiary",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, SAVE_PHOTO_IMAGE_REQUEST_CODE)
                }
            } else {
                Toast.makeText(context, "Unable to save photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // SAVE PHOTO
    private fun createImageFile(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("PlantDiary${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    // OPEN GALLERY
    private fun prepOpenGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, IMAGE_GALLERY_REQUEST)
        }
    }

    // PERMISSION GPS
    private fun prepRequestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationUpdates()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(permissionRequest, PERMISSION_REQUEST_LOCATION)
            }
        }
    }

    private fun locationUpdates() {
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        locationViewModel.getLocationLiveData().observe(viewLifecycleOwner, { location ->
            val altitude = location.latitude
            val longitude = location.longitude

            mainFragmentBinding.tvLatitude.text = altitude
            mainFragmentBinding.tvLongitude.text = longitude
        })
    }

    private fun savePlant() {
        var specimen = Specimen().apply {
            latitude = mainFragmentBinding.tvLatitude.text.toString()
            longitude = mainFragmentBinding.tvLongitude.text.toString()
            plantName = mainFragmentBinding.actPlantName.text.toString()
            description = mainFragmentBinding.txtDescription.text.toString()
            datePlanted = mainFragmentBinding.txtDatePlanted.text.toString()
            plantId = _plantId
        }
        viewModel.save(specimen, photos)
        Toast.makeText(requireContext(), "document saved", Toast.LENGTH_SHORT).show()


        // clear memory
        specimen = Specimen()
        photos = ArrayList<Photo>()
    }

    private fun logon() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            AUTH_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                val imageBitmap = data!!.extras!!.get("data") as Bitmap
                mainFragmentBinding.imgView.setImageBitmap(imageBitmap)
            }
            SAVE_PHOTO_IMAGE_REQUEST_CODE -> {
                /*val imageBitmap = data!!.extras!!.get("data") as Bitmap
                mainFragmentBinding.imgView.setImageBitmap(imageBitmap)*/
                Toast.makeText(requireContext(), "saved", Toast.LENGTH_SHORT).show()
                val photo = Photo(localUri = photoURI.toString())
                photos.add(photo)
            }
            IMAGE_GALLERY_REQUEST -> {
                if (data != null && data.data != null) {
                    val image = data.data
                    val source =
                        ImageDecoder.createSource(requireActivity().contentResolver, image!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    mainFragmentBinding.imgView.setImageBitmap(bitmap)
                }
            }
            AUTH_REQUEST_CODE -> {
                user = FirebaseAuth.getInstance().currentUser
            }
        }

    }
}