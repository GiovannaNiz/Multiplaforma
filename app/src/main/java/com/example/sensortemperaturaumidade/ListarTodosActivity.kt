package com.example.sensortemperaturaumidade

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ListarTodosActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var voltarButton: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_todos)

        recyclerView = findViewById(R.id.recyclerView)
        voltarButton = findViewById(R.id.voltarButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        voltarButton.setOnClickListener {
            finish()
        }

        buscarTodos()
    }

    private fun buscarTodos() {
        db.collection("SensorData")
            .orderBy("date", Query.Direction.DESCENDING) // Certifique-se que "date" e "time" são os nomes corretos no Firestore
            .get()
            .addOnSuccessListener { querySnapshot ->
                val sensorDataList = mutableListOf<SensorData>()

                for (document in querySnapshot.documents) {
                    val date = document.getString("date") ?: "Data indisponível"
                    val time = document.getString("time") ?: "Hora indisponível"
                    val temp = document.getString("temp") ?: "N/A"
                    val hmd = document.getString("hmd") ?: "N/A"

                    sensorDataList.add(SensorData("$date $time", temp, hmd))
                }

                if (sensorDataList.isEmpty()) {
                    recyclerView.adapter = SensorDataAdapter(emptyList()) // Caso não haja dados
                } else {
                    recyclerView.adapter = SensorDataAdapter(sensorDataList)
                }
            }
            .addOnFailureListener { exception ->
                recyclerView.adapter = SensorDataAdapter(emptyList())
            }
    }
}

data class SensorData(val dataHora: String, val temperatura: String, val umidade: String)
