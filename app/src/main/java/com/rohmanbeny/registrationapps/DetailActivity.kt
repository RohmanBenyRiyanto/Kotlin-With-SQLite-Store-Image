package com.rohmanbeny.registrationapps

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rohmanbeny.registrationapps.adapter.RegisterAdapter
import com.rohmanbeny.registrationapps.databinding.ActivityDetailBinding
import com.rohmanbeny.registrationapps.model.RegisterModel
import com.rohmanbeny.registrationapps.services.helper.EXTRA_POSITION
import com.rohmanbeny.registrationapps.services.helper.EXTRA_REGISTRATION
import com.rohmanbeny.registrationapps.services.helper.REQUEST_ADD
import com.rohmanbeny.registrationapps.services.helper.REQUEST_UPDATE
import com.rohmanbeny.registrationapps.services.helper.RESULT_ADD
import com.rohmanbeny.registrationapps.services.helper.RESULT_DELETE
import com.rohmanbeny.registrationapps.services.helper.RESULT_UPDATE
import com.rohmanbeny.registrationapps.services.helper.mapCursorToArrayList
import com.rohmanbeny.registrationapps.utils.RegisterHelper
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executor


class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var registerHelper: RegisterHelper
    private lateinit var adapter: RegisterAdapter
    private val EXTRA_STATE = "EXTRA_SATE"

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var coordinatorLayout: CoordinatorLayout? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.daftar_mahasiswa)
        binding.rvRegister.layoutManager = LinearLayoutManager(this)
        binding.rvRegister.setHasFixedSize(true)
        adapter = RegisterAdapter(this)
        binding.rvRegister.adapter = adapter

        coordinatorLayout = binding.clDetail

        handleViewOnEmpty()

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showSnackbarMessage(getString(R.string.error_no_hardware))
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showSnackbarMessage(getString(R.string.error_hw_unavailable))
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showSnackbarMessage(getString(R.string.error_none_enrolled))
            }
        }

        var executor: Executor = ContextCompat.getMainExecutor(this)

        biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showSnackbarMessage(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showSnackbarMessage(getString(R.string.success))
                    coordinatorLayout!!.visibility = View.VISIBLE
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.title_biometric_prompt))
            .setDescription(getString(R.string.description_biometric_prompt))
            .setDeviceCredentialAllowed(true).build()

        biometricPrompt.authenticate(promptInfo)


        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD)
        }

        registerHelper = RegisterHelper.getInstance(applicationContext)
        registerHelper.open()
        if (savedInstanceState == null) {
            loadRegister()
        } else {
            val list = savedInstanceState.getParcelableArrayList<RegisterModel>(EXTRA_STATE)
            if (list != null) {
                adapter.listRegister = list
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listRegister)
    }

    private fun loadRegister() {
        GlobalScope.launch(Dispatchers.Main) {
            binding.progressbar.visibility = View.VISIBLE
            val cursor = registerHelper.queryAll()
            val register = mapCursorToArrayList(cursor)
            binding.progressbar.visibility = View.INVISIBLE

            if (register.size > 0) {
                adapter.listRegister = register
                handleViewOnEmpty()
            } else {
                adapter.listRegister = ArrayList()
                handleViewOnEmpty()
//                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    private fun handleViewOnEmpty() {
        if (adapter.listRegister.size > 0) {
            binding.tvEmpty.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(binding.rvRegister, message, Snackbar.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            when (requestCode) {
                REQUEST_ADD -> if (resultCode == RESULT_ADD) {
                    val regist =
                        data.getParcelableExtra<RegisterModel>(EXTRA_REGISTRATION) as RegisterModel
                    adapter.addItem(regist)
                    binding.rvRegister.smoothScrollToPosition(adapter.itemCount - 1)
                    adapter.notifyDataSetChanged()
                    showSnackbarMessage("Satu item berhasil ditambahkan")
                    handleViewOnEmpty()

                }
                REQUEST_UPDATE -> when (resultCode) {
                    RESULT_UPDATE -> {
                        val regist =
                            data.getParcelableExtra<RegisterModel>(EXTRA_REGISTRATION) as RegisterModel
                        val position = data.getIntExtra(EXTRA_POSITION, 0)
                        adapter.updateItem(position, regist)
                        binding.rvRegister.smoothScrollToPosition(position)
                        adapter.notifyDataSetChanged()
                        showSnackbarMessage("Satu item berhasil diubah")
                        handleViewOnEmpty()
                    }
                    RESULT_DELETE -> {
                        val position = data.getIntExtra(EXTRA_POSITION, 0)
                        adapter.removeItem(position)
                        adapter.notifyDataSetChanged()
                        showSnackbarMessage("Satu item berhasil dihapus")
                        handleViewOnEmpty()
                    }

                }
            }
        }
    }
}