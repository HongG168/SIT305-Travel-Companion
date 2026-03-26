package com.example.mytravelcompanion

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Unit lists per category
    private val categoryUnits = mapOf(
        "Currency"    to listOf("USD", "AUD", "EUR", "JPY", "GBP"),
        "Fuel"        to listOf("MPG", "km/L", "Gallon (US)", "Liter", "Nautical Mile", "Kilometer"),
        "Temperature" to listOf("Celsius", "Fahrenheit", "Kelvin")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerFrom     = findViewById<Spinner>(R.id.spinnerFrom)
        val spinnerTo       = findViewById<Spinner>(R.id.spinnerTo)
        val editValue       = findViewById<EditText>(R.id.editValue)
        val btnConvert      = findViewById<Button>(R.id.btnConvert)
        val txtResult       = findViewById<TextView>(R.id.txtResult)

        // Set up category spinner
        val categories = categoryUnits.keys.toList()
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        // When category changes, update From/To spinners
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val units = categoryUnits[categories[pos]] ?: return
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, units)
                spinnerFrom.adapter = adapter
                spinnerTo.adapter   = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, units)
                spinnerTo.setSelection(1) // default to second unit
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Convert button click
        btnConvert.setOnClickListener {
            val input = editValue.text.toString()
            if (input.isEmpty()) {
                txtResult.text = "⚠️ Please enter a value"
                return@setOnClickListener
            }
            val value = input.toDoubleOrNull()
            if (value == null) {
                txtResult.text = "⚠️ Invalid number"
                return@setOnClickListener
            }

            val category = spinnerCategory.selectedItem.toString()
            val from     = spinnerFrom.selectedItem.toString()
            val to       = spinnerTo.selectedItem.toString()
            val result   = convert(category, from, to, value)

            txtResult.text = "$value $from = $result $to"
        }
    }

    // ── Main conversion router ──────────────────────────────
    private fun convert(category: String, from: String, to: String, value: Double): String {
        if (from == to) return String.format("%.4f", value)
        return when (category) {
            "Currency"    -> convertCurrency(from, to, value)
            "Fuel"        -> convertFuel(from, to, value)
            "Temperature" -> convertTemperature(from, to, value)
            else          -> "N/A"
        }
    }

    // ── Currency (via USD as base) ──────────────────────────
    private fun convertCurrency(from: String, to: String, value: Double): String {
        val toUSD = mapOf("USD" to 1.0, "AUD" to 1/1.55, "EUR" to 1/0.92, "JPY" to 1/148.50, "GBP" to 1/0.78)
        val fromUSD = mapOf("USD" to 1.0, "AUD" to 1.55, "EUR" to 0.92, "JPY" to 148.50, "GBP" to 0.78)
        val inUSD = value * (toUSD[from] ?: return "N/A")
        val result = inUSD * (fromUSD[to] ?: return "N/A")
        return String.format("%.4f", result)
    }

    // ── Fuel & Distance ─────────────────────────────────────
    private fun convertFuel(from: String, to: String, value: Double): String {
        val result = when ("$from->$to") {
            "MPG->km/L"              -> value * 0.425
            "km/L->MPG"              -> value / 0.425
            "Gallon (US)->Liter"     -> value * 3.785
            "Liter->Gallon (US)"     -> value / 3.785
            "Nautical Mile->Kilometer" -> value * 1.852
            "Kilometer->Nautical Mile" -> value / 1.852
            else -> return "⚠️ Conversion not supported"
        }
        return String.format("%.4f", result)
    }

    // ── Temperature ─────────────────────────────────────────
    private fun convertTemperature(from: String, to: String, value: Double): String {
        // Step 1: convert to Celsius
        val celsius = when (from) {
            "Celsius"    -> value
            "Fahrenheit" -> (value - 32) / 1.8
            "Kelvin"     -> value - 273.15
            else -> return "N/A"
        }
        // Step 2: convert Celsius to target
        val result = when (to) {
            "Celsius"    -> celsius
            "Fahrenheit" -> (celsius * 1.8) + 32
            "Kelvin"     -> celsius + 273.15
            else -> return "N/A"
        }
        return String.format("%.4f", result)
    }
}
