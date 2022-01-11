package com.supersuman.gitamtransit

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.*
import java.util.*


class ContributionActivity : AppCompatActivity() {

    private lateinit var openFileButton : MaterialButton
    private lateinit var openFolderButton: MaterialButton
    private val resultLauncher = registerForResult()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contribution)

        initViews()
        initListeners()

    }

    private fun initViews(){
        openFileButton = findViewById(R.id.contributionActivityFileButton)
        openFolderButton = findViewById(R.id.contributionActivityFolderButton)
    }

    private fun initListeners(){
        openFileButton.setOnClickListener {

            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "*/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a file")
            resultLauncher.launch(chooseFile)
        }
        openFolderButton.setOnClickListener {
            val selectedUri = Uri.parse(getExternalFilesDir(null).toString())
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(selectedUri, "resource/folder")
            startActivity(intent)
        }
    }

    private fun registerForResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = readGPXFile(result.data)
                    val latLonList = getLatLonList(data)
                    val latLonString = getLatLonString(latLonList)
                    writeFile(latLonString, "busroute.txt")
                    val keywordsList = getKeywordsList(latLonList)
                    val keyWordString = getKeywordsString(keywordsList)
                    writeFile(keyWordString, "keywords.txt")
                }
            }
        }
    }

    private fun readGPXFile(data: Intent?): String {
        val uri = data?.data
        val inputStream = contentResolver.openInputStream(uri!!)
        val r = BufferedReader(InputStreamReader(inputStream))
        var allLines = ""
        while (true) {
            val line: String = r.readLine() ?: break
            allLines += line
        }
        return allLines
    }

    private fun getLatLonList(data: String): MutableList<LatLng> {
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

    private fun getLatLonString(latLonList: MutableList<LatLng>): String{
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
    
    private fun getKeywordsList(latLonList: MutableList<LatLng>): MutableList<String> {
        val mutableList = mutableListOf<String>()
        val geocoder = Geocoder (this, Locale.getDefault())
        var previousLocality = ""
        var previousThoroughfare = ""
        for (i in 0 until latLonList.size){
            if (i%2 != 0) continue
            val latitude = latLonList[i].latitude
            val longitude = latLonList[i].longitude
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
        }
        return mutableList
    }

    private fun getKeywordsString(keywordsList: MutableList<String>): String {
        var latlonString =""
        for (i in 0 until keywordsList.size){
            val keyword = keywordsList[i]
            latlonString += keyword
            if (i+1<keywordsList.size){
                latlonString += ",\n"
            }
        }
        return latlonString
    }


}