package com.einfoplanet.fileio

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File

class FileUploadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        // The name of the image manipulation work
        const val FILE_UPLOAD_WORK_NAME = "file_upload_work"
        const val KEY_FILE_URI = "key_file_uri"
        const val KEY_USER_ID = "key_user_uri"
        const val TAG_OUTPUT = "OUTPUT"
        const val CHILD_STR = "child"
    }

    private val storage = Firebase.storage

    override fun doWork(): ListenableWorker.Result {
        val storageRef = storage.reference
        val imagesRef: StorageReference? = storageRef.child("player_issues")

        val resourceUri: String? = inputData.getString(KEY_FILE_URI)
        val userId: String? = inputData.getString(KEY_USER_ID)
        val child: String? = inputData.getString(CHILD_STR)

        // Points to "images/space.jpg"
        // Note that you can use variables to create child values
        val spaceRef = imagesRef?.child(child!!)

        val metadata: StorageMetadata = storageMetadata {
            contentType = "file/txt"
            setCustomMetadata("user_id", userId)
        }

        val uploadTask = spaceRef?.putFile(Uri.parse(resourceUri), metadata)
        try {
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

            return Result.success()
        } catch (throwable: Throwable) {
            return Result.failure()
        }
    }

    private fun uploadFileOnFireStorage(child: String, fileToUpload: File) {

    }
}