package com.supersuman.gitamtransit.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.supersuman.gitamtransit.R
import com.supersuman.gitamtransit.helpers.RoutesData
import com.supersuman.gitamtransit.adapters.SearchAdapter
import com.supersuman.gitamtransit.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray


class SearchFragment : Fragment() {

    private lateinit var textInputLayout: TextInputLayout
    private lateinit var textInputEditText: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var mPrefs : SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        modifyViews()
        animateViews()
        coroutineScope.launch {
            val data = getLatestData()
            requireActivity().runOnUiThread {
                initListeners(data)
            }
        }
    }

    private fun initViews() {
        textInputLayout = requireActivity().findViewById(R.id.textInputLayout)
        recyclerView = requireActivity().findViewById(R.id.recyclerView2)
        textInputEditText = requireActivity().findViewById(R.id.textInputEditText)
        mPrefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
    }

    private fun modifyViews() {
        val linearManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearManager
        recyclerView.adapter = SearchAdapter(mutableListOf(), requireActivity())
        textInputEditText.requestFocus()
        showKeyboard()
    }

    private fun animateViews() {
        textInputLayout.translationY = 50F
        textInputLayout.alpha = 0.2F
        textInputLayout.animate().translationY(0F).alpha(1F).setDuration(600).start()
    }

    private fun getLatestData(): MutableList<RoutesData> {
        val jsonString =
            khttp.get("https://raw.githubusercontent.com/supersu-man/GitamTransit/main/assets/busRoutes.json").text
        val routes = JSONArray(jsonString)
        val data = mutableListOf<RoutesData>()
        for (i in 0 until routes.length()) {
            val it = routes.getJSONObject(i)
            val busName = it.get("busName").toString()
            val startPoint = it.get("startPoint").toString()
            val keywords = JSONArray(it.get("keywords").toString())
            val keywordArr = mutableListOf<String>()
            for (j in 0 until keywords.length()){
                keywordArr.add(keywords.getString(j))
            }
            val busInfo = RoutesData(busName, startPoint, it.get("route").toString(), keywordArr)
            data.add(busInfo)
        }
        return data
    }

    private fun initListeners(data: MutableList<RoutesData>) {
        var newData = search(textInputEditText.text.toString(), data)
        recyclerView.adapter = SearchAdapter(newData, requireActivity())
        textInputEditText.addTextChangedListener{
            newData = search(it.toString(), data)
            recyclerView.adapter = SearchAdapter(newData, requireActivity())
        }
    }

    private fun search(keyword: String, data: MutableList<RoutesData>): MutableList<RoutesData> {
        val newData = mutableListOf<RoutesData>()
        for (i in data){
            val temp = mutableListOf<String>()
            for (j in i.keywords){
                val address = j.lowercase()
                if (keyword in address)
                    temp.add(j)
            }
            if (temp.isNotEmpty())
                newData.add(RoutesData(i.busName,"", i.route, temp))
        }
        return newData
    }

    private fun showKeyboard() {
        val teleprinter = Teleprinter(requireActivity() as AppCompatActivity, true)
        teleprinter.showKeyboard(textInputEditText)
    }

}