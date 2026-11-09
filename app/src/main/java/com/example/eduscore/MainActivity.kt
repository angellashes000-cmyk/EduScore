package com.example.eduscore

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var containerNotas: LinearLayout
    private lateinit var tvResultado: TextView
    private lateinit var drawerLayout: DrawerLayout

    private val notaMaxima = 5.0 // cambia a 10.0 o 100.0 según tu escala

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuario = auth.currentUser
        if (usuario == null) { // seguridad: sin sesión, volvemos al registro
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        val nombre = usuario.displayName ?: "Usuario"

        // --- Saludo con el nombre ---
        findViewById<TextView>(R.id.tvBienvenida).text = "¡Bienvenido a EduScore, $nombre!"

        // --- Barra superior + menú desplegable ---
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close
        )
        toggle.drawerArrowDrawable.color = Color.WHITE
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.navView)
        val header = navView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvNavNombre).text = nombre
        header.findViewById<TextView>(R.id.tvNavCorreo).text = usuario.email

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_historial ->
                    startActivity(Intent(this, HistoryActivity::class.java))
                R.id.nav_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // --- Calculadora ---
        containerNotas = findViewById(R.id.containerNotas)
        tvResultado = findViewById(R.id.tvResultado)

        agregarCampoNota() // empezamos con 2 campos (el mínimo)
        agregarCampoNota()

        findViewById<Button>(R.id.btnAgregar).setOnClickListener { agregarCampoNota() }

        findViewById<Button>(R.id.btnQuitar).setOnClickListener {
            if (containerNotas.childCount > 2) {
                containerNotas.removeViewAt(containerNotas.childCount - 1)
            } else {
                Toast.makeText(this, "Debes tener mínimo 2 notas", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnCalcular).setOnClickListener { calcularPromedio() }
    }

    private fun agregarCampoNota() {
        val campo = EditText(this).apply {
            hint = "Nota ${containerNotas.childCount + 1}"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        containerNotas.addView(campo)
    }

    private fun calcularPromedio() {
        val notas = mutableListOf<Double>()

        for (i in 0 until containerNotas.childCount) {
            val campo = containerNotas.getChildAt(i) as EditText
            val valor = campo.text.toString().replace(',', '.').toDoubleOrNull()

            if (valor == null || valor < 0 || valor > notaMaxima) {
                campo.error = "Ingresa una nota válida (0 a $notaMaxima)"
                tvResultado.text = "Error: revisa las notas ingresadas"
                return
            }
            notas.add(valor)
        }

        if (notas.size < 2) {
            tvResultado.text = "Error: debes ingresar mínimo 2 notas"
            return
        }

        val promedio = notas.average()
        tvResultado.text = "Tu promedio es: %.2f".format(promedio)
        guardarEnHistorial(notas, promedio)
    }

    private fun guardarEnHistorial(notas: List<Double>, promedio: Double) {
        val uid = auth.currentUser?.uid ?: return
        val registro = hashMapOf(
            "notas" to notas.joinToString(", "),
            "cantidad" to notas.size,
            "promedio" to promedio,
            "fecha" to Timestamp.now()
        )
        db.collection("users").document(uid).collection("historial").add(registro)
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo guardar el historial", Toast.LENGTH_SHORT).show()
            }
    }
}