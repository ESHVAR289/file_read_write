package com.einfoplanet.fileio

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.einfoplanet.fileiolib.FileIOUtil
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        private const val DIRECTORY_NAME = "player_issue"
        private const val FILE_NAME = "eshvar1.txt"
    }

    private val storage = Firebase.storage
    private val database = Firebase.database
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Get the ViewModel
        viewModel = ViewModelProviders.of(this).get(
            MainActivityViewModel::class.java
        )

        observeData()

        findViewById<Button>(R.id.btn_upload_file).setOnClickListener {
            viewModel.isIdPresentInFirebaseRealTimeDb("112233")
        }
//        uploadFileOnAWS(fileToUpload = getFileToUpload(), child = FILE_NAME)

    }

    private fun getFileToUpload(): File {
        val fileIOUtil = FileIOUtil(this)
//        val internalFilesDirectory =
//                fileIOUtil.internalFilesDirectory + File.separator + DIRECTORY_NAME
//        fileIOUtil.createDirectory(internalFilesDirectory)
//        Log.e(
//                "Storage",
//                "getInternalFilesDirectory() : $internalFilesDirectory"
//        )
//
//        val isFileExists = fileIOUtil.isFileExist(internalFilesDirectory)
//        Log.e("Storage", "isFileExists() : $isFileExists")
//
//        val fileToWriteData =
//            internalFilesDirectory + File.separator + FILE_NAME
//
//        val createFile =
//                fileIOUtil.createFile(internalFilesDirectory, FILE_NAME)

        val internalStorageDirectory =
            fileIOUtil.internalStorageDirectory + File.separator + DIRECTORY_NAME
        fileIOUtil.createDirectory(internalStorageDirectory)
        Log.e(
            "Storage",
            "getInternalFilesDirectory() : $internalStorageDirectory"
        )

        val isFileExists = fileIOUtil.isFileExist(internalStorageDirectory)
        Log.e("Storage", "isFileExists() : $isFileExists")

        val fileToWriteData = internalStorageDirectory + File.separator + FILE_NAME

        val createFile = fileIOUtil.createFile(internalStorageDirectory, FILE_NAME)

        Log.e("Storage", "isFileCreated() : $createFile")

//        storageUtil.writeToFile(fileToWriteData,"START")

        Log.e(
            "Storage",
            "isFileExists() : fileToWriteData : ${fileIOUtil.isFileExist(fileToWriteData)}"
        )
        fileIOUtil.appendFile(fileToWriteData, "ESHVAR MALI")
        fileIOUtil.appendFile(fileToWriteData, "Android application developer")
        fileIOUtil.appendFile(fileToWriteData, "Company : ALTBalaji")

        val fileToUpload = fileIOUtil.getFile(fileToWriteData)
        return fileToUpload
    }

    private fun observeData() {

        viewModel._userExistsLiveData.observe(this, androidx.lifecycle.Observer {
            if (it) {
                viewModel.uploadFile(FILE_NAME, getFileToUpload())
            }
        })
    }

    private fun uploadFileOnAWS(child: String, fileToUpload: File) {
//        try {
//            val exampleFile = File(applicationContext.filesDir, "ExampleKey")
//
//            exampleFile.writeText("Example file contents")
//
//
//            Amplify.Auth.fetchAuthSession(
//                { result -> Log.i("AmplifyQuickstart", result.toString()) },
//                { error -> Log.e("AmplifyQuickstart", error.toString()) }
//            )
//
////            Amplify.Storage.uploadFile(
////                "ExampleKey",
////                exampleFile,
////                { result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()) },
////                { error -> Log.e("MyAmplifyApp", "Upload failed", error) }
////            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    private fun uploadFileOnFireStorage(child: String, fileToUpload: File) {
        val storageRef = storage.reference
        val imagesRef: StorageReference? = storageRef.child("player_issues")

        // Points to "images/space.jpg"
        // Note that you can use variables to create child values
        val spaceRef = imagesRef?.child(child)

        val metadata: StorageMetadata = storageMetadata {
            contentType = "file/txt"
            setCustomMetadata("user_id", "289")
            setCustomMetadata("user_name", "eshvar")
        }

        val uploadTask = spaceRef?.putFile(Uri.fromFile(fileToUpload), metadata)

        val urlTask =
            uploadTask?.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                spaceRef.downloadUrl
            }?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Log.e("Storage", "task.isSuccessful : $downloadUri")
                } else {
                    // Handle failures
                    // ...
                }
            }
    }

    fun getToken() {

    }
}