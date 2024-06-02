package com.slf.module.presentation.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.slf.module.presentation.ViewModel.UserViewModel
import com.slf.module.presentation.databinding.FragmentEditProfileBinding
import com.slf.module.data.model.User
import com.slf.module.data.model.UserRepository
import com.slf.module.utils.Constant
import com.slf.module.utils.SharedHelper
import com.google.android.material.snackbar.Snackbar
import com.slf.module.presentation.R
import com.slf.module.presentation.worker.BlurWorker
import com.slf.module.utils.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditProfileFragment : Fragment() {
    private var bind: FragmentEditProfileBinding? = null
    private val binding get() = bind!!
    private lateinit var userRepository: UserRepository
    private lateinit var shared: SharedHelper
    private lateinit var userViewModel: UserViewModel
    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val SELECT_PICTURE = 2
        private const val CAMERA_REQUEST_CODE = 100
        private const val REQUEST_CODE_PERMISSION = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentEditProfileBinding.inflate(inflater, container, false)
        userRepository = UserRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shared = SharedHelper(requireContext())
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        binding.apply {
            getDataProfile()

            btnEditSave.setOnClickListener {
                saveDataProfile()
            }

            bornedit.setOnClickListener { showDatePickerDialog() }

            photoedit.setOnClickListener {
                selectImage()
            }
        }
    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Photo!")

        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                    } else {
                        dispatchTakePictureIntent()
                    }
                }
                options[item] == "Choose from Gallery" -> {
                    selectImageFromGallery()
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, SELECT_PICTURE)
    }
    private fun startBlurWork(imageUri: Uri) {
        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(workDataOf(BlurWorker.KEY_IMAGE_URI to imageUri.toString()))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(requireContext()).enqueue(blurRequest)

        // Observe work status
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(blurRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    val outputUriString = workInfo.outputData.getString(BlurWorker.KEY_BLURRED_IMAGE_URI)
                    if (!outputUriString.isNullOrEmpty()) {
                        val outputUri = Uri.parse(outputUriString)
                        binding.photoedit.setImageURI(outputUri)
                        selectedImage = outputUri
                    } else {
                        Toast.makeText(requireContext(), "Failed to blur image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(requireContext(), "Permission to use camera was denied", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    selectedBitmap = imageBitmap
                    binding.photoedit.setImageBitmap(selectedBitmap)
                    selectedImage = saveImageToInternalStorage(imageBitmap)
                    // Start Blur Work
                    selectedImage?.let { startBlurWork(it) }
                }
                SELECT_PICTURE -> {
                    selectedImage = data?.data
                    context?.let {
                        try {
                            selectedImage?.let { uri ->
                                val internalUri = copyImageToInternalStorage(uri)
                                if (internalUri != null) {
                                    selectedImage = internalUri
                                    selectedBitmap = if (Build.VERSION.SDK_INT >= 28) {
                                        val source = ImageDecoder.createSource(it.contentResolver, internalUri)
                                        ImageDecoder.decodeBitmap(source)
                                    } else {
                                        MediaStore.Images.Media.getBitmap(it.contentResolver, internalUri)
                                    }
                                    binding.photoedit.setImageBitmap(selectedBitmap)

                                    // Start Blur Work
                                    startBlurWork(internalUri)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream: InputStream? = context?.contentResolver?.openInputStream(uri)
            val file = File(context?.filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            outputStream.close()
            inputStream?.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri? {
        return try {
            val file = File(context?.filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun getDataProfile() {
        val username = shared.getString(Constant.USERNAME)
        getUser(username)
        binding.apply {
            userViewModel.user.observe(viewLifecycleOwner) { user ->
                Nameedit.setText(user.name)
                Emailedit.setText(user.email)
                bornedit.setText(user.born)
                addressedit.setText(user.address)

                // Debug log to check the URI
                Log.d("EditProfileFragment", "Profile photo URI: ${user.profilePhoto}")

                // Set the user's profile photo to the ImageButton
                if (user.profilePhoto != null) {
                    val imageUri = Uri.parse(user.profilePhoto)
                    try {
                        context?.contentResolver?.openInputStream(imageUri)?.close()
                        photoedit.setImageURI(imageUri)
                    } catch (e: Exception) {
                        Log.e("EditProfileFragment", "Error accessing URI: $imageUri", e)
                        photoedit.setImageResource(R.drawable.ic_photowhite) // Default drawable
                    }
                } else {
                    photoedit.setImageResource(R.drawable.ic_photowhite) // Default drawable
                }
            }
        }
    }

    private fun getUser(username: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val data = userRepository.getUser()
            runBlocking(Dispatchers.Main) {
                data?.let {
                    userViewModel.dataUser(it)
                }
            }
        }
    }

    fun showDatePickerDialog() {
        val newFragment = DatePickerFragment()
        newFragment.show(childFragmentManager, "datePicker")
    }

    private fun saveDataProfile() {
        binding.apply {
            when {
                Nameedit.text.isNullOrEmpty() -> Nameedit.error = "Fill the name"
                Emailedit.text.isNullOrEmpty() -> Emailedit.error = "Fill the email"
                !Validation.isValidEmail(Emailedit.text.toString()) -> Emailedit.error = "Enter a valid email"
                bornedit.text.isNullOrEmpty() -> bornedit.error = "Fill the date of birth"
                addressedit.text.isNullOrEmpty() -> addressedit.error = "Fill the address"
                else -> {
                    val user = User(
                        id = 0,
                        name = Nameedit.text.toString(),
                        email = Emailedit.text.toString(),
                        born = bornedit.text.toString(),
                        address = addressedit.text.toString(),
                        username = shared.getString(Constant.USERNAME).toString(),
                        password = shared.getString(Constant.PASSWORD).toString(),
                        profilePhoto = selectedImage.toString()
                    )
                    lifecycleScope.launch(Dispatchers.IO) {
                        userRepository.saveUser(user)
                        runBlocking(Dispatchers.Main) {
                            Snackbar.make(binding.root, "Profile saved successfully", Snackbar.LENGTH_SHORT).setTextColor(Color.WHITE).setBackgroundTint(Color.BLUE).show()
                        }
                    }
                }
            }
        }
    }
}
