package com.app.realmtodoapp.ui.view.base
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.app.realmtodoapp.R
import com.app.realmtodoapp.utils.NetworkChangeCallback
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), NetworkChangeCallback.NetworkChangeListener {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkChangeCallback
     var binding: VB? = null
    private lateinit var progressDialog: Dialog

    abstract fun getViewBinding(): VB
    abstract fun doWorkHere()
    abstract fun onInternetAvailable()
    abstract fun onInternetUnavailable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding!!.root)

        // Initialize ConnectivityManager and NetworkCallback
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = NetworkChangeCallback(this)


        // Initialize progress dialog
        progressDialog = Dialog(this)
        val progressDialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, binding!!.root as ViewGroup, false)
        progressDialog.setContentView(progressDialogView)
        progressDialog.setCancelable(false)

        // Initial internet connectivity check
        if (isInternetAvailable()) {
            onInternetAvailable()
        } else {
            onInternetUnavailable()
        }

        // Call the abstract method to perform subclass-specific tasks
        doWorkHere()
    }

    override fun onStart() {
        super.onStart()
        // Register network callback
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onStop() {
        super.onStop()
        // Unregister network callback
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onNetworkChanged(isConnected: Boolean) {
        if (isConnected) {
            onInternetAvailable()
        } else {
            onInternetUnavailable()
           // redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        //startNewActivity<LoginActivity>()
        finishAffinity()
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            println("${it.key} = ${it.value}")
        }
    }

    fun requestPermissions(vararg permissions: String) {
        requestPermissionLauncher.launch(permissions.toList().toTypedArray())
    }

    fun showSnackBar(message: String) {
        Snackbar.make(binding!!.root, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showProgressDialog() {
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
    }

    fun hideProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    // Check current internet connectivity
    private fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
