package com.satriomp.sigantung.ui.simulasi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SimulasiViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Prediksi : "
    }
    val text: LiveData<String> = _text

    fun setResultText(resultText: String) {
        _text.value = resultText
    }
}