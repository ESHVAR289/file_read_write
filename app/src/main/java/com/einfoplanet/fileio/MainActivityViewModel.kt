package com.einfoplanet.fileio

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.einfoplanet.fileio.FileUploadWorker.Companion.CHILD_STR
import com.einfoplanet.fileio.FileUploadWorker.Companion.FILE_UPLOAD_WORK_NAME
import com.einfoplanet.fileio.FileUploadWorker.Companion.KEY_FILE_URI
import com.einfoplanet.fileio.FileUploadWorker.Companion.KEY_USER_ID
import com.einfoplanet.fileio.FileUploadWorker.Companion.TAG_OUTPUT
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import java.util.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val workManager: WorkManager = WorkManager.getInstance(application)
    internal var fileUri: Uri? = null
    private val database = Firebase.database
    private val userExistsLiveData = MutableLiveData<Boolean>()
    internal val _userExistsLiveData: LiveData<Boolean>
        get() = userExistsLiveData

    internal fun cancelWork() {
        workManager.cancelUniqueWork(FILE_UPLOAD_WORK_NAME)
    }

    fun isIdPresentInFirebaseRealTimeDb(userId: String): Boolean {
        val dbRef = database.getReference("user_ids")
        // Read from the database
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val listOfAllowedUsers: ArrayList<String> = dataSnapshot.value as ArrayList<String>
                val isUserPresent = listOfAllowedUsers.contains(userId)
                Log.e("Storage", "User is present? => $isUserPresent")
                userExistsLiveData.value = isUserPresent
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e("Storage", "Failed to read value.", error.toException())
                Log.e("Storage", "User is present? => false")
                userExistsLiveData.value = false
            }
        })
        return false
    }

    /**
     * create work request to upload file
     */
    internal fun uploadFile(child: String, fileToUpload: File) {

//        setFileToUpload(Uri.fromFile(fileToUpload))

        //create internet constraint
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val fileUploadWorkBuilder = OneTimeWorkRequestBuilder<FileUploadWorker>()
            .setConstraints(constraint)
            .addTag(TAG_OUTPUT)
            .setInputData(createInputDataForUri(Uri.fromFile(fileToUpload), child))

        val fileUploadWorker2 = OneTimeWorkRequestBuilder<FileUploadWorker2>()
            .setConstraints(constraint)
            .addTag(TAG_OUTPUT)
            .setInputData(createInputDataForUri(Uri.fromFile(fileToUpload), child))

        // Add File clean up request
        val deleteUploadedFile = OneTimeWorkRequestBuilder<CleanupWorker>()
            .addTag(TAG_OUTPUT)
            .build()

        //add work request
        val continuation = workManager
            .beginWith(fileUploadWorker2.build())
            .then(deleteUploadedFile)

        continuation.enqueue()
    }


//    private fun setFileToUpload(uri: Uri?) {
//        fileUri = uri
//    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * Creates the input data bundle which includes the Uri to operate on
     * @return Data which contains the Image Uri as a String
     */
    private fun createInputDataForUri(uriOfFileToUpload: Uri, child: String): Data {
        val builder = Data.Builder()
        uriOfFileToUpload.let {
            builder.putString(KEY_FILE_URI, it.toString())
            builder.putString(KEY_USER_ID, "112233")
            builder.putString(CHILD_STR, child)
        }
        return builder.build()
    }
}