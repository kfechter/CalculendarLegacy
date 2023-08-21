package com.kennethfechter.datepicker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kennethfechter.datepicker.businesslogic.Utilities
import com.kennethfechter.datepicker.databinding.ActivityCalculendarMainBinding
import kotlinx.coroutines.*
import java.util.*

class CalculendarMain : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var binding: ActivityCalculendarMainBinding


    private var selectedDates: MutableList<Date> = mutableListOf()
    private var excludedDates: MutableList<Date> = mutableListOf()
    private var exclusionOption: String = ""

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityCalculendarMainBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setSupportActionBar(binding.toolbar)

            binding.btnPickRange.setOnClickListener {
                showRangeDialog()
            }

            binding.btnPickCustom.setOnClickListener {
                showCustomDialog()
            }

            binding.btnPerformCalculation.setOnClickListener {
                val result = Utilities.calculateDays(this, selectedDates, excludedDates, exclusionOption)

                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Calculation Result")
                dialogBuilder.setMessage(result)

                dialogBuilder.setNeutralButton("OK") {dialog, _ ->
                    dialog.dismiss()
                }

                dialogBuilder.create().show()
            }

            val deprecationDialogBuilder = AlertDialog.Builder(this)
            deprecationDialogBuilder.setTitle("Deprecation Alert")
            deprecationDialogBuilder.setMessage(R.string.deprecation_dialog_text)
            deprecationDialogBuilder.setNeutralButton("OK") {dialog, _ ->
                dialog.dismiss()
            }
            deprecationDialogBuilder.create().show()


            binding.exclusionOptions.onItemSelectedListener = this
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_calculendar_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.about_application -> navigateToNewApp()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        viewModelJob.cancel()
    }

    override fun onStop() {
        super.onStop()
        viewModelJob.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
    }

    override fun onRestart() {
        super.onRestart()
        viewModelJob = Job()
    }

    override fun onResume() {
        super.onResume()
        viewModelJob = Job()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
        val exclusionOptions = resources.getStringArray(R.array.exclusion_options)
        exclusionOption = exclusionOptions[position]
        when(exclusionOption) {
            "Exclude Custom" -> {
                binding.btnPickCustom.visibility = View.VISIBLE
                binding.btnPickCustom.text = Utilities.getCustomDatesFormatterString(this, excludedDates.size)
            }
            else -> binding.btnPickCustom.visibility = View.INVISIBLE
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {

    }

    private fun navigateToNewApp() {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.kennethfechter.calculendar")))
    }

    fun showRangeDialog() = uiScope.launch {
        val localSelectedDates = Utilities.displayDatePickerDialog(this@CalculendarMain, resources.getString(R.string.date_picker_dialog_title),true)

        if(localSelectedDates.size > 1) {
            selectedDates = localSelectedDates
            binding.exclusionOptionsContainer.visibility = View.VISIBLE
            binding.btnPerformCalculation.isEnabled = true
            binding.btnPickRange.text = Utilities.getSelectedRangeString(selectedDates)
        } else {
            Toast.makeText(this@CalculendarMain, "A valid date range was not selected", Toast.LENGTH_LONG).show()
        }
    }

    fun showCustomDialog() = uiScope.launch {
        excludedDates =  Utilities.displayDatePickerDialog(this@CalculendarMain, resources.getString(R.string.custom_date_dialog_title),false, selectedDates, excludedDates)
        binding.btnPickCustom.text = Utilities.getCustomDatesFormatterString(this@CalculendarMain, excludedDates.size)
    }
}