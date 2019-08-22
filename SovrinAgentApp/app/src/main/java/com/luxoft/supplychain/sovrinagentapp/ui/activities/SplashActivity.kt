package com.luxoft.supplychain.sovrinagentapp.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.GENESIS_CONTENT
import com.luxoft.supplychain.sovrinagentapp.application.GENESIS_PATH
import com.luxoft.supplychain.sovrinagentapp.di.indyInitialize
import java.io.File

class SplashActivity : AppCompatActivity() {

    private val permissionRequestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE),
            permissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            permissionRequestCode -> {
                if (grantResults.any { it != PermissionChecker.PERMISSION_GRANTED })
                    throw RuntimeException("You should grant permissions if you want to use vcx")
                else {
                    initGenesis()
                    indyInitialize
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun initGenesis() {
        val genesis = File(GENESIS_PATH)
        if (genesis.exists()) genesis.delete()
        genesis.createNewFile()
        genesis.writeText(GENESIS_CONTENT)
    }
}
