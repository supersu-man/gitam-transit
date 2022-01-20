package com.supersuman.gitamtransit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.util.*
import com.karumi.dexter.PermissionToken

import com.karumi.dexter.listener.PermissionDeniedResponse

import com.karumi.dexter.listener.PermissionGrantedResponse

import com.karumi.dexter.listener.single.PermissionListener

import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionRequest
import android.content.pm.PackageManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class ContributionActivity : AppCompatActivity() {

    private lateinit var chooseFileButton : MaterialButton
    private lateinit var openFolderButton: MaterialButton
    private lateinit var progressText : TextView
    private lateinit var textView: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private val resultLauncher = registerForResult()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contribution)

        initViews()
        modifyViews()
        requestMyPermissions()
        initListeners()

    }

    private fun initViews(){
        chooseFileButton = findViewById(R.id.contributionActivityFileButton)
        openFolderButton = findViewById(R.id.contributionActivityFolderButton)
        textView = findViewById(R.id.contributionActivityTextView)
        progressBar = findViewById(R.id.contributionActivityProgressBar)
        progressText = findViewById(R.id.contributionActivityProgressText)
    }

    @SuppressLint("SetTextI18n")
    private fun modifyViews(){
        textView.text = "Check out readme.md file on this github project to know more about contribution.\nClick here to open readme.md."
    }

    private fun requestMyPermissions(){
        Dexter.withContext(this)
            .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {}
                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(this@ContributionActivity, "xiseyorftnwejowermpnluteprtmv", Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {}
            }).check()
    }

    private fun initListeners(){
        textView.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/supersu-man/GitamTransit#contributing"))
            startActivity(browserIntent)
        }
        chooseFileButton.setOnClickListener {
            if (permissionsPresent()){
                var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                chooseFile.type = "*/*"
                chooseFile = Intent.createChooser(chooseFile, "Choose a file")
                resultLauncher.launch(chooseFile)
            }else{
                Toast.makeText(this, "Please give permission", Toast.LENGTH_SHORT).show()
            }
        }
        openFolderButton.setOnClickListener {
            val selectedUri = Uri.parse(getExternalFilesDir(null).toString())
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(selectedUri, "resource/folder")
            startActivity(intent)
        }
    }

    private fun permissionsPresent(): Boolean {
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        val res = this.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    private fun registerForResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                when {
                    result.data?.data.toString().endsWith("gpx") -> {
                        coroutineScope.launch {
                            val data = readFile(result.data?.data)
                            val latLonList = getLatLngGpx(data)
                            exportFiles(latLonList)
                        }
                    }
                    result.data?.data.toString().endsWith("kml") -> {
                        coroutineScope.launch {
                            val data = readFile(result.data?.data)
                            val latLonList = getLatLngKml(data)
                            exportFiles(latLonList)
                        }
                    }
                    else -> {
                        Toast.makeText(this, "Only KML/GPX files are supported", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun readFile(data: Uri?): String {
        val inputStream = contentResolver.openInputStream(data!!)
        val r = BufferedReader(InputStreamReader(inputStream))
        var allLines = ""
        while (true) {
            val line: String = r.readLine() ?: break
            allLines += line
        }
        return allLines
    }

    private fun getLatLngGpx(data: String): MutableList<LatLng> {
        val latLonList = mutableListOf<LatLng>()
        val soup = Jsoup.parse(data)
        val trackPoints = soup.getElementsByTag("trkpt")
        for (i in 0 until trackPoints.size){
            val lat = trackPoints[i].attr("lat").toDouble()
            val lon = trackPoints[i].attr("lon").toDouble()
            latLonList.add(LatLng(lat, lon))
        }
        return latLonList
    }

    private fun getLatLngKml(data: String): MutableList<LatLng> {
        val latLonList = mutableListOf<LatLng>()
        val soup = Jsoup.parse(data)
        val coordinates = soup.getElementsByTag("coordinates").first()?.text().toString()
        val coordinatesList = coordinates.split(",0")
        for (i in coordinatesList) {
            if ("," !in i) continue
            val lat = i.split(",")[1].toDouble()
            val lon = i.split(",")[0].toDouble()
            latLonList.add(LatLng(lat,lon))
        }
        return latLonList
    }

    private fun exportFiles(latLonList: MutableList<LatLng>) {
        val keywordsList = reverseGeoCode(latLonList)

        val keyWordString = keywordsListToString(keywordsList)
        writeFile(keyWordString, "keywords.txt")

        val busRouteTxtData = latLonListToString(latLonList)
        writeFile(busRouteTxtData, "busroute.txt")
    }

    private fun latLonListToString(latLonList: MutableList<LatLng>): String{
        var latlonString =""
        for (i in 0 until latLonList.size){
            val lat = latLonList[i].latitude.toString()
            val lon = latLonList[i].longitude.toString()
            latlonString += "[$lat,$lon]"
            if (i+1<latLonList.size){
                latlonString += ",\n"
            }
        }
        return latlonString
    }

    private fun writeFile(data: String, filename: String) {
        val file = File(getExternalFilesDir(null), filename)
        file.createNewFile()
        val writer = FileWriter(file)
        writer.write(data)
        writer.flush()
        writer.close()
        runOnUiThread {
            Toast.makeText(this, "$filename saved", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun reverseGeoCode(latLonList: MutableList<LatLng>): MutableList<String> {
        val mutableList = mutableListOf<String>()
        val geocoder = Geocoder (this, Locale.getDefault())
        var previousLocality = ""
        var previousThoroughfare = ""
        for (i in 0 until latLonList.size){
            if (i%2 != 0) continue
            val latitude = latLonList[i].latitude
            val longitude = latLonList[i].longitude
            println("$latitude $longitude")
            val addresses : List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
            val locality = addresses[0].locality
            val thoroughfare = addresses[0].thoroughfare
            if (locality != null && locality != previousLocality && locality !in mutableList){
                previousLocality = locality
                mutableList.add(locality)
            }
            if (thoroughfare != null && thoroughfare != previousThoroughfare && thoroughfare !in mutableList){
                previousThoroughfare = thoroughfare
                mutableList.add(thoroughfare)
            }
            runOnUiThread {
                val progress = (((i+1).toFloat()/latLonList.size) * 100 ).toInt()
                progressText.text = "$progress%"
                progressBar.progress = progress
            }
        }

        runOnUiThread {
            progressText.text = "100%"
            progressBar.progress = 100
        }
        return mutableList
    }

    private fun keywordsListToString(keywordsList: MutableList<String>): String {
        var latlonString =""
        for (i in 0 until keywordsList.size){
            val keyword = keywordsList[i]
            latlonString += "\"$keyword\""
            if (i+1<keywordsList.size){
                latlonString += ",\n"
            }
        }
        return latlonString
    }


}