package com.example.eduscore

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHistorial)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.navigationIcon?.setTint(android.graphics.Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        val lista = findViewById<ListView>(R.id.listHistorial)
        val tvVacio = findViewById<TextView>(R.id.tvVacio)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users").document(uid).collection("historial")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    tvVacio.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val items = resultado.documents.map { doc ->
                    val fecha = doc.getTimestamp("fecha")?.toDate()?.let { formato.format(it) } ?: ""
                    val promedio = doc.getDouble("promedio") ?: 0.0
                    val notas = doc.getString("notas") ?: ""
                    "$fecha\nNotas: $notas\nPromedio: %.2f".format(promedio)
                }
                lista.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
            }
            .addOnFailureListener {
                tvVacio.text = "No se pudo cargar el historial"
                tvVacio.visibility = View.VISIBLE
            }
    }
}