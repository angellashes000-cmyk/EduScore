package com.example.eduscore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val auth = FirebaseAuth.getInstance()
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etClave = findViewById<EditText>(R.id.etClave)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val clave = etClave.text.toString()

            if (correo.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(correo, clave)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity() // cierra Registro y Login para que "atrás" no regrese
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show()
                }
        }

        findViewById<Button>(R.id.btnIrRegistro).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}