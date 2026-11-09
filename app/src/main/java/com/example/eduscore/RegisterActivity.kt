package com.example.eduscore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Si ya hay sesión iniciada, saltamos directo a la calculadora
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_register)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etClave = findViewById<EditText>(R.id.etClave)

        findViewById<Button>(R.id.btnRegistrar).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val clave = etClave.text.toString()

            if (nombre.isEmpty() || correo.isEmpty() || clave.length < 6) {
                Toast.makeText(
                    this,
                    "Completa todo (la contraseña debe tener mínimo 6 caracteres)",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(correo, clave)
                .addOnSuccessListener { result ->
                    // Guardamos el nombre en el perfil del usuario
                    val perfil = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    result.user?.updateProfile(perfil)?.addOnCompleteListener {
                        auth.signOut() // el usuario inicia sesión en la pantalla 2
                        Toast.makeText(this, "Cuenta creada. Inicia sesión", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        findViewById<Button>(R.id.btnIrLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}