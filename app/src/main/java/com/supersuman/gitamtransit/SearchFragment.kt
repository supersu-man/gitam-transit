package com.supersuman.gitamtransit

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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class SearchFragment : Fragment() {

    private lateinit var textInputLayout: TextInputLayout
    private lateinit var textInputEditText: TextInputEditText
    private var data = mutableListOf<RoutesData>()
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
        getData()
        initListeners()
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

    private fun getData() {
        val gson = Gson()
        val json = mPrefs.getString("RoutesDataList", "")
        val type: Type = object : TypeToken<MutableList<RoutesData?>?>() {}.type
        data =  gson.fromJson(json, type)
    }

    private fun initListeners() {
        textInputEditText.addTextChangedListener{
            val newData = search(it.toString())
            recyclerView.adapter = SearchAdapter(newData, requireActivity())
        }
    }

    private fun search(keyword : String): MutableList<NewData> {
        val newData = mutableListOf<NewData>()
        for (i in data){
            val temp = mutableListOf<String>()
            for (j in i.coordinatesDetailsList){
                val address = j.lowercase()
                if (keyword in address)
                    temp.add(j)
            }
            if (temp.isNotEmpty())
                newData.add(NewData(i.busName, i.route, temp))
        }
        return newData
    }

    private fun showKeyboard() {
        val teleprinter = Teleprinter(requireActivity() as AppCompatActivity, true)
        teleprinter.showKeyboard(textInputEditText)
    }

}