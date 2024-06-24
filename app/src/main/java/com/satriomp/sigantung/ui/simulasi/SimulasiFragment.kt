package com.satriomp.sigantung.ui.simulasi

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.satriomp.sigantung.R
import com.satriomp.sigantung.databinding.FragmentSimulasiBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@Suppress("SameParameterValue", "ReplaceGetOrSet")
class SimulasiFragment : Fragment() {

    private var _binding: FragmentSimulasiBinding? = null
    private val binding get() = _binding!!

    private lateinit var genderValues: Map<String, Int>
    private lateinit var chestPainTypeValues: Map<String, Int>
    private lateinit var restingECGValues: Map<String, Int>
    private lateinit var exerciseAnginaValues: Map<String, Int>
    private lateinit var fastingBPValues: Map<String, Int>
    private lateinit var stSlopeValues: Map<String, Int>

    private lateinit var interpreter: Interpreter
    private val mModelPath = "heart.tflite"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val simulasiViewModel = ViewModelProvider(this).get(SimulasiViewModel::class.java)

        _binding = FragmentSimulasiBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initInterpreter(requireContext().assets)

        val textView: TextView = binding.txtResult
        simulasiViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        genderValues = mapOf("Laki-Laki" to 1, "Perempuan" to 0)
        chestPainTypeValues = mapOf("Typical Angina" to 1, "Non-Anginal Pain" to 2, "Asymptomatic" to 0, "Typical Angina" to 3)
        restingECGValues = mapOf("Normal" to 1, "Kelainan ST-T" to 2, "Kriteria LVH Estes" to 0)
        exerciseAnginaValues = mapOf("Tidak ada" to 0, "Iya" to 1)
        fastingBPValues = mapOf("Tidak" to 0, "Iya" to 1)
        stSlopeValues = mapOf("Condong keatas" to 2, "Datar" to 1, "Sedikit Landai" to 0)

        setUpDropdown(
            binding.edtGender.editText as AutoCompleteTextView,
            R.array.gender_array
        )
        setUpDropdown(
            binding.edtCPType.editText as AutoCompleteTextView,
            R.array.chest_pain_type_array
        )
        setUpDropdown(
            binding.edtRestingECG.editText as AutoCompleteTextView,
            R.array.resting_ecg_array
        )
        setUpDropdown(
            binding.edtExAngina.editText as AutoCompleteTextView,
            R.array.exercise_angina_array
        )
        setUpDropdown(
            binding.edtFasting.editText as AutoCompleteTextView,
            R.array.fasting_bs
        )
        setUpDropdown(
            binding.edtSTSlope.editText as AutoCompleteTextView,
            R.array.st_slope_array
        )

        binding.btnPredict.setOnClickListener {
            val genderValue = genderValues[binding.complete2.text.toString()]
            val chestPainTypeValue = chestPainTypeValues[binding.complete3.text.toString()]
            val restingECGValue = restingECGValues[binding.complete7.text.toString()]
            val exerciseAnginaValue = exerciseAnginaValues[binding.complete9.text.toString()]
            val fastingBPValue = fastingBPValues[binding.complete6.text.toString()]
            val stSlopeValue = stSlopeValues[binding.complete11.text.toString()]
            val ageValue = binding.complete1.text.toString()
            val restingBPValue = binding.complete4.text.toString()
            val cholesterolValue = binding.complete5.text.toString()
            val maxHRValue = binding.complete8.text.toString()
            val oldPeakValue = binding.complete10.text.toString()

            val fields = arrayOf(
                ageValue, genderValue, chestPainTypeValue, restingBPValue,
                cholesterolValue, fastingBPValue, restingECGValue, maxHRValue,
                exerciseAnginaValue, oldPeakValue, stSlopeValue
            )

            val missingFields = mutableListOf<String>()
            for (i in fields.indices) {
                if (fields[i] == null || (fields[i] is String && (fields[i] as String).isEmpty())) {
                    missingFields.add((i + 1).toString())
                }
            }

            if (missingFields.isEmpty()) {
                val inputArray = floatArrayOf(
                    ageValue.toFloat(),
                    genderValue!!.toFloat(),
                    chestPainTypeValue!!.toFloat(),
                    restingBPValue.toFloat(),
                    cholesterolValue.toFloat(),
                    fastingBPValue!!.toFloat(),
                    restingECGValue!!.toFloat(),
                    maxHRValue.toFloat(),
                    exerciseAnginaValue!!.toFloat(),
                    oldPeakValue.toFloat(),
                    stSlopeValue!!.toFloat()
                )

                val result = doInference(inputArray)
                displayResult(result)
            } else {
                val message = "Isi tabel Nomor :\n${missingFields.joinToString(", ")}"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnReset.setOnClickListener {
            binding.complete1.setText("")
            binding.complete2.setText("")
            binding.complete3.setText("")
            binding.complete4.setText("")
            binding.complete5.setText("")
            binding.complete6.setText("")
            binding.complete7.setText("")
            binding.complete8.setText("")
            binding.complete9.setText("")
            binding.complete10.setText("")
            binding.complete11.setText("")

            val viewModel = ViewModelProvider(this).get(SimulasiViewModel::class.java)
            viewModel.setResultText("Prediksi :")

            Toast.makeText(context, "Form telah direset!", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    private fun displayResult(result: Float) {
        val resultText = when {
            result > 0.5 -> "Prediksi : BERPOTENSI mengalami Gagal Jantung. \n\nTetaplah mendengarkan tubuh Anda dan jangan ragu untuk berkonsultasi dengan dokter secara rutin."
            else -> "Prediksi : TIDAK BERPOTENSI mengalami Gagal Jantung. \n\nIngatlah untuk menjaga pola hidup sehat dan tetap aktif secara fisik."
        }
        binding.txtResult.text = resultText

        val viewModel = ViewModelProvider(this).get(SimulasiViewModel::class.java)
        viewModel.setResultText(resultText)
    }

    private fun setUpDropdown(autoCompleteTextView: AutoCompleteTextView, arrayResId: Int) {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            arrayResId,
            android.R.layout.simple_dropdown_item_1line
        )
        autoCompleteTextView.setAdapter(adapter)
    }

    private fun initInterpreter(assetManager: AssetManager) {
        val options = Interpreter.Options()
        options.setNumThreads(5)
        options.setUseNNAPI(true)
        interpreter = Interpreter(loadModelFile(assetManager, mModelPath), options)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun doInference(inputData: FloatArray): Float {
        val output = Array(1) { FloatArray(1) }
        interpreter.run(arrayOf(inputData), output)

        Log.e("result", (output[0].toList() + " ").toString())
        return output[0][0]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
