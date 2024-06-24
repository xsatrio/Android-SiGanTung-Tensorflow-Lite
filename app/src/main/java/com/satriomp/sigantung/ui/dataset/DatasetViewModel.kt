package com.satriomp.sigantung.ui.dataset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.BufferedReader

class DatasetViewModel : ViewModel() {

    private val _tableData = MutableLiveData<List<List<String>>>()
    val tableData: LiveData<List<List<String>>> get() = _tableData

    var isDataLoaded: Boolean = false
        private set

    fun loadCSVData(inputStream: BufferedReader) {
        val tableDataList = mutableListOf<List<String>>()

        var line: String? = inputStream.readLine()
        while (line != null) {
            val rowData = line.split(",") // Split CSV row into columns by comma
            tableDataList.add(rowData)
            line = inputStream.readLine()
        }

        _tableData.value = tableDataList
        isDataLoaded = true

        inputStream.close()
    }

    fun clearData() {
        _tableData.value = emptyList()
        isDataLoaded = false
    }
}
