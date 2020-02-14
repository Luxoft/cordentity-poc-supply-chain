package com.luxoft.supplychain.sovrinagentapp.views.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.GENESIS_PATH
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import rx.Completable
import rx.schedulers.Schedulers
import java.io.File

lateinit var splashScreen: SplashActivity

class SplashActivity : AppCompatActivity() {

    private val permissionRequestCode = 101

    private val indyUser: IndyUser by inject()
    private val appState: ApplicationState by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ActivityCompat.requestPermissions(
                this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE),
            permissionRequestCode
        )
        splashScreen = this
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            permissionRequestCode -> {
                if (grantResults.any { it != androidx.core.content.PermissionChecker.PERMISSION_GRANTED })
                    throw RuntimeException("You should grant permissions if you want to use vcx")
                else {
                    val state = getKoin().get<ApplicationState>()
                    state.indyState.openOrCreateWallet()
                    state.indyState.connectToPool()

                    state.walletCredentials.value?.forEach {
                        Log.d("User", "User $it")
                    }

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }
}
