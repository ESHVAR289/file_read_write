package com.einfoplanet.fileio

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File

class FileUploadWorker2(context: Context, params: WorkerParameters) :
    ListenableWorker(context, params) {
    companion object {
        // The name of the image manipulation work
        const val FILE_UPLOAD_WORK_NAME = "file_upload_work"
        const val KEY_FILE_URI = "key_file_uri"
        const val KEY_USER_ID = "key_user_uri"
        const val TAG_OUTPUT = "OUTPUT"
        const val CHILD_STR = "child"
        const val DOWNLOAD_URL = "download_url"
    }

    private val storage = Firebase.storage

    private fun uploadFileOnFireStorage(child: String, fileToUpload: File) {

    }

    override fun startWork(): ListenableFuture<Result> {
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

        return CallbackToFutureAdapter.getFuture { completer ->
            {
                uploadTask?.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            completer.setException(it)
                            throw it
                        }
                        completer.set(Result.failure())
                    }
                    spaceRef.downloadUrl
                }?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        Log.e("Storage", "task.isSuccessful : $downloadUri")
                        val outputData = Data.Builder()
                        outputData.putString(DOWNLOAD_URL, downloadUri.toString())
                        outputData.putString(KEY_FILE_URI, resourceUri)
                        outputData.putString(KEY_USER_ID, userId)
                        outputData.putString(CHILD_STR, child)
                        completer.set(Result.success(outputData.build()))
                    } else {
                        completer.set(Result.failure())
                    }
                }
            }
        }
    }
}