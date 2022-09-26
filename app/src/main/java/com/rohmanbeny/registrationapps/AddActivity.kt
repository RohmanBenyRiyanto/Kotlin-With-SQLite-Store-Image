package com.rohmanbeny.registrationapps

import android.Manifest
import android.R.attr.password
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.rohmanbeny.registrationapps.databinding.ActivityMainBinding
import com.rohmanbeny.registrationapps.model.RegisterModel
import com.rohmanbeny.registrationapps.services.helper.ALERT_DIALOG_CLOSE
import com.rohmanbeny.registrationapps.services.helper.ALERT_DIALOG_DELETE
import com.rohmanbeny.registrationapps.services.helper.EXTRA_POSITION
import com.rohmanbeny.registrationapps.services.helper.EXTRA_REGISTRATION
import com.rohmanbeny.registrationapps.services.helper.RESULT_ADD
import com.rohmanbeny.registrationapps.services.helper.RESULT_DELETE
import com.rohmanbeny.registrationapps.services.helper.RESULT_UPDATE
import com.rohmanbeny.registrationapps.utils.DatabaseRegsiter
import com.rohmanbeny.registrationapps.utils.RegisterHelper
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*


class AddActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var registerHelper: RegisterHelper
    private var imageView: ImageView? = null

    private val permissionId = 55
    private val RESULT_LOAD_IMAGE = 123
    private val REQUEST_CODE_GALLERY = 999
    private var jenisKelamin: String = ""
    private var lokasi: String = ""

    private var isEdit = false
    private var register: RegisterModel? = null
    private var position: Int = 0

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        registerHelper = RegisterHelper.getInstance(applicationContext)
        registerHelper.open()

        register = intent.getParcelableExtra(EXTRA_REGISTRATION)
        if (register != null){
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            register = RegisterModel()
        }

        val actionBarTitle: String
        val btnTitle: String

        if (isEdit){
            actionBarTitle = "Ubah"
            btnTitle = "Update"
            register?.let { it ->
                binding.nameEditText.setText(it.name)
                binding.alamatEditText.setText(it.alamat)
                binding.phoneEditText.setText(it.phone)

                //Handle Jenis Kelamin

                if (it.jk.equals("Laki-Laki")){
                    binding.radioLaki.isChecked = true
                    jenisKelamin = "Laki Laki"
                } else {
                    binding.radioPerempuan.isChecked = true
                    jenisKelamin = "Perempuan"
                }

                //handle location

                binding.tvCurrentLocation.text = it.location

                val registerImage: ByteArray? = it.image
                val bitmap =
                    registerImage?.let { it1 -> BitmapFactory.decodeByteArray(registerImage, 0, it1.size) }
                binding.ivFoto.setImageBitmap(bitmap)
            }!!
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
        }

        actionBar?.title = actionBarTitle
        binding.btnSubmit.text = btnTitle
        binding.btnSubmit.setOnClickListener(this)

        imageView = binding.ivFoto

        binding.btnCekLokasi.setOnClickListener {
            getLocation()
        }

        binding.ivFoto.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
        }

        imageView!!.setOnClickListener {

            ActivityCompat.requestPermissions(
                this@AddActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE

                ),
                REQUEST_CODE_GALLERY
            )
        }

        binding.nameEditText.setOnEditorActionListener { v, actionId, event ->
            register?.let { it ->
                it.name = v.text.toString()
            }
            false
        }

        setOnCheckedChangeListener()

        binding.phoneEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                //not focused
                binding.phoneEditText.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.phoneEditText.windowToken, 0)

                true
            } else {
                false
            }
        }
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String
        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?"
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Register"
        }
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result = registerHelper.deleteById(register?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddActivity,
                            "Gagal menghapus data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE_GALLERY)
            } else {
                Toast.makeText(
                    applicationContext,
                    "You don't have permission to access file location!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        binding.apply {
                            tvCurrentLocation.text = list[0].getAddressLine(0)
                            lokasi = list[0].getAddressLine(0)
                            latitude = list[0].latitude
                            longitude = list[0].longitude
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            val uri: Uri? = data.data
            try {
                val inputStream = contentResolver.openInputStream(uri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val resized = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
                imageView!!.setImageBitmap(resized)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun setOnCheckedChangeListener() {
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = findViewById(checkedId)
            jenisKelamin = radio.text.toString()
            Log.i(TAG, "onCheckedChangeListener: $jenisKelamin")
            Toast.makeText(this, radio.text, Toast.LENGTH_SHORT).show()
        }
    }


    private fun imageViewToByte(image: ImageView): ByteArray? {
        val bitmap = (image.getDrawable() as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onClick(view: View?) {
        if(view?.id == R.id.btn_submit) {

            val name = binding.nameEditText.text.toString().trim()
            val alamat = binding.alamatEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()
            if(name.isEmpty()) {
                binding.nameEditText.error = "Nama tidak boleh kosong"
                binding.nameEditText.requestFocus()
                return
            }

            register?.name = name
            register?.alamat = alamat
            register?.phone = phone
            register?.jk = jenisKelamin
            register?.location = lokasi
            register?.latitude = latitude
            register?.longitude = longitude
            register?.image = imageViewToByte(imageView!!)

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(EXTRA_REGISTRATION, register)
            intent.putExtra(EXTRA_POSITION, position)
            val values = ContentValues()
            values.put(DatabaseRegsiter.RegisterColumns.NAME, register?.name)
            values.put(DatabaseRegsiter.RegisterColumns.ALAMAT, register?.alamat)
            values.put(DatabaseRegsiter.RegisterColumns.PHONE, register?.phone)
            values.put(DatabaseRegsiter.RegisterColumns.JK, register?.jk)
            values.put(DatabaseRegsiter.RegisterColumns.LOCATION, register?.location)
            values.put(DatabaseRegsiter.RegisterColumns.LATITUDE, register?.latitude)
            values.put(DatabaseRegsiter.RegisterColumns.LONGITUDE, register?.longitude)
            values.put(DatabaseRegsiter.RegisterColumns.IMAGE, register?.image)

            if(isEdit) {
                val result = registerHelper.update(
                    register?.id.toString(),
                    values
                ).toLong()
                if (result > 0) {
                    setResult(RESULT_UPDATE, intent)
                    finish()
                } else {
                    Toast.makeText(this, "Gagal mengupdate data", Toast.LENGTH_SHORT).show()
                }
            } else {
                val result = registerHelper.insert(values)
                if (result > 0) {
                    register?.id = result.toInt()
                    setResult(RESULT_ADD, intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@AddActivity,
                        "Gagal menambah data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}


