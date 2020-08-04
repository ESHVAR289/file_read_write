package com.einfoplanet.fileio

import android.app.Application

class FileApplication : Application() {
    override fun onCreate() {
        super.onCreate()

//        try {
//            val configuration:AmplifyConfiguration = AmplifyConfiguration.fromConfigFile(applicationContext,R.raw.amplifyconfiguration)
//            Amplify.configure(configuration,applicationContext)
//            Amplify.addPlugin(AWSCognitoAuthPlugin())
//            Amplify.addPlugin(AWSS3StoragePlugin())
//            Log.e("MyAmplifyApp", "Initialized Amplify")
//        } catch (error: AmplifyException) {
//            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
//        }
    }
}