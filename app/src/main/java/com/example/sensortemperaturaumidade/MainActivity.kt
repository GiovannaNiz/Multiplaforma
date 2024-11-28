package com.example.sensortemperaturaumidade

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var dataInput: EditText
    private lateinit var horaInput: EditText
    private lateinit var verificarButton: Button
    private lateinit var verTodosButton: Button
    private lateinit var resultTextView: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataInput = findViewById(R.id.dataInput)
        horaInput = findViewById(R.id.horaInput)
        verificarButton = findViewById(R.id.verificarButton)
        verTodosButton = findViewById(R.id.verTodosButton)
        resultTextView = findViewById(R.id.resultTextView)

        // Calendário para data
        dataInput.setOnClickListener {
            showDatePicker()
        }

        verificarButton.setOnClickListener {
            val data = dataInput.text.toString().trim()
            val hora = horaInput.text.toString().trim()

            if (data.isNotEmpty() && hora.isNotEmpty()) {
                verificarDataHora(data, hora)
            } else {
                resultTextView.text = "Por favor, insira uma data e uma hora."
            }
        }

        verTodosButton.setOnClickListener {
            val intent = Intent(this, ListarTodosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun verificarDataHora(data: String, hora: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
        timeFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")

        try {
            val date = dateFormat.parse(data)
            val time = timeFormat.parse(hora)

            if (date != null && time != null) {
                db.collection("SensorData")
                    .whereEqualTo("date", data)
                    .whereEqualTo("time", hora)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val result = StringBuilder()

                            for (document in querySnapshot.documents) {
                                val temp = document.getString("temp")
                                val hmd = document.getString("hmd")
                                val date = document.getString("date")
                                val time = document.getString("time")

                                result.append(
                                    "Data: $date $time\n" +
                                            "Temperatura: ${temp ?: "N/A"}°C\n" +
                                            "Umidade: ${hmd ?: "N/A"}%\n\n"
                                )
                            }
                            resultTextView.text = result.toString()
                        } else {
                            resultTextView.text = "Nenhum documento encontrado para a data e hora fornecida."
                        }
                    }
                    .addOnFailureListener { e ->
                        resultTextView.text = "Erro ao consultar os dados: ${e.message}"
                    }
            }
        } catch (e: Exception) {
            resultTextView.text = "Formato de data ou hora inválido. Use o formato: YYYY-MM-DD para data e HH:MM:SS para hora."
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val monthFormatted = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
                val dayFormatted = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                dataInput.setText("$year-$monthFormatted-$dayFormatted")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}