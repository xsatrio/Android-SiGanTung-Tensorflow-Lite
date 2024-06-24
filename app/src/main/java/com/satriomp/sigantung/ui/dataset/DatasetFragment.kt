package com.satriomp.sigantung.ui.dataset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.satriomp.sigantung.R
import java.io.BufferedReader
import java.io.InputStreamReader

@Suppress("ReplaceGetOrSet")
class DatasetFragment : Fragment() {

    private lateinit var viewModel: DatasetViewModel
    private lateinit var tableLayout: TableLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dataset, container, false)

        tableLayout = rootView.findViewById(R.id.tableLayout)
        progressBar = rootView.findViewById(R.id.progressBar)

        viewModel = ViewModelProvider(this).get(DatasetViewModel::class.java)

        viewModel.tableData.observe(viewLifecycleOwner) { tableData ->
            displayTableData(tableData)
        }

        return rootView
    }

    private fun displayTableData(tableData: List<List<String>>) {
        tableLayout.removeAllViews()

        val maxRows = 20
        val rowsToShow = tableData.take(maxRows)

        for (rowData in rowsToShow) {
            val tableRow = TableRow(requireContext())
            val layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            tableRow.layoutParams = layoutParams
            tableRow.setPadding(0, 0, 0, 0)

            for (column in rowData) {
                val textView = TextView(requireContext())
                textView.text = column
                textView.setPadding(8, 8, 8, 8)
                textView.setBackgroundResource(R.drawable.cell_border)
                tableRow.addView(textView)
            }

            tableLayout.addView(tableRow)
            progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.isDataLoaded) {
            // Show progress bar and load CSV data if not already loaded
            progressBar.visibility = View.VISIBLE
            val inputStream = resources.openRawResource(R.raw.data_csv) // Assume data_csv.csv file is in res/raw/
            val reader = BufferedReader(InputStreamReader(inputStream))
            viewModel.loadCSVData(reader)
        }
    }

    override fun onPause() {
        super.onPause()
        tableLayout.removeAllViews()
        progressBar.visibility = View.GONE
        viewModel.clearData()
    }
}
