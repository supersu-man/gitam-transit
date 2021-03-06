package com.supersuman.gitamtransit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.util.*


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
                val uri = result.data?.data!!
                val fileName = getFileName(uri)
                when {
                    fileName.endsWith("gpx") -> {
                        coroutineScope.launch {
                            val data = readFile(uri)
                            val latLonList = getLatLngGpx(data)
                            exportFiles(latLonList, fileName)
                        }
                    }
                    fileName.endsWith("kml") -> {
                        coroutineScope.launch {
                            val data = readFile(uri)
                            val latLonList = getLatLngKml(data)
                            exportFiles(latLonList, fileName)
                        }
                    }
                    else -> {
                        Toast.makeText(this, "Only KML/GPX files are supported", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
            catch (e:Exception){}
            cursor?.close()
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun readFile(data: Uri): String {
        val inputStream = contentResolver.openInputStream(data)
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

    private fun exportFiles(latLonList: MutableList<LatLng>, fileName: String) {

        val jsonObject = JSONObject()
        jsonObject.put("busName", "")
        jsonObject.put("startPoint", "")

        val busRouteJsonArray = latLonListToJson(latLonList)
        jsonObject.put("route", busRouteJsonArray)

        val keywordsList = reverseGeoCode(latLonList)
        val keyWordJsonArray = keywordsListToJson(keywordsList)
        jsonObject.put("keywords", keyWordJsonArray)

        writeFile(jsonObject, "$fileName.txt")
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

    private fun latLonListToJson(latLonList: MutableList<LatLng>): JSONArray {
        val jsonArray = JSONArray()
        latLonList.forEach {
            val temp = JSONArray().apply {
                this.put(it.latitude)
                this.put(it.longitude)
            }
            jsonArray.put(temp)
        }
        return jsonArray
    }

    private fun keywordsListToJson(keywordsList: MutableList<String>): JSONArray {

        val jsonArray = JSONArray()
        keywordsList.forEach {
            jsonArray.put(it)
        }
        return jsonArray
    }

    private fun writeFile(jsonObject: JSONObject, filename: String) {
        val data = jsonObject.toString()
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
}