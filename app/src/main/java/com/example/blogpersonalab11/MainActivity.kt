package com.example.blogpersonalab11

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogpersonalab11.ui.theme.BlogPersonalLab11Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var userPreferences: UserPreferences
    private val storage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private var imageUri by mutableStateOf<Uri?>(null)

    // Instancia de FirebaseAuth
    private lateinit var auth: FirebaseAuth

    // Lanzador de actividad para seleccionar una imagen
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this) // Inicializar UserPreferences
        auth = FirebaseAuth.getInstance() // Inicializar FirebaseAuth
        signInAnonymously() // Iniciar autenticación anónima

        setContent {
            BlogPersonalLab11Theme {
                val coroutineScope = rememberCoroutineScope()

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserProfileForm(onSave = { userProfile ->
                            coroutineScope.launch {
                                userPreferences.saveUserPreferences(
                                    name = userProfile.name,
                                    surname = userProfile.surname,
                                    email = userProfile.email,
                                    birthDate = userProfile.birthDate
                                )
                                Log.d("UserProfile", "Datos de usuario guardados en DataStore")
                            }
                        })

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de texto para la publicación
                        var postText by remember { mutableStateOf(TextFieldValue("")) }
                        OutlinedTextField(
                            value = postText,
                            onValueChange = { postText = it },
                            label = { Text("Escribe tu publicación") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Botón para seleccionar una imagen
                        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Text("Seleccionar Imagen")
                        }

                        // Botón para guardar la publicación
                        Button(
                            onClick = {
                                imageUri?.let { uri ->
                                    coroutineScope.launch {
                                        savePostToFirebase(postText.text, uri)
                                    }
                                }
                            },
                            enabled = imageUri != null
                        ) {
                            Text("Guardar Publicación")
                        }
                    }
                }
            }
        }

        // Prueba de conexión con Firebase Realtime Database
        database.child("test").setValue("Conexión exitosa")
            .addOnSuccessListener {
                Log.d("Firebase", "¡Conexión exitosa con Firebase Realtime Database!")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al conectar con Firebase: ${e.message}")
            }
    }

    // Función para autenticación anónima
    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Autenticación anónima exitosa")
                } else {
                    Log.e("FirebaseAuth", "Error en la autenticación anónima", task.exception)
                }
            }
    }

    // Función para guardar la publicación en Firebase
    private fun savePostToFirebase(text: String, imageUri: Uri) {
        val postId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("posts/$postId.jpg")

        // Subir la imagen a Firebase Storage
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Obtener la URL de descarga de la imagen
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Guardar los datos de la publicación en Realtime Database
                    val postMap = mapOf(
                        "text" to text,
                        "imageUrl" to downloadUrl.toString(),
                        "timestamp" to System.currentTimeMillis()
                    )

                    database.child("posts").child(postId).setValue(postMap)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Publicación guardada exitosamente en Realtime Database.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error al guardar en Realtime Database: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al subir la imagen: ${e.message}")
            }
    }
}

// Modelo de datos para almacenar el perfil de usuario
data class UserProfile(
    val name: String,
    val surname: String,
    val email: String,
    val birthDate: String
)

@Composable
fun UserProfileForm(onSave: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var surname by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var birthDate by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        OutlinedTextField(value = surname, onValueChange = { surname = it }, label = { Text("Apellido") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") })
        OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Fecha de nacimiento (DD/MM/AAAA)") })
        Button(onClick = {
            val userProfile = UserProfile(name = name.text, surname = surname.text, email = email.text, birthDate = birthDate.text)
            onSave(userProfile)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar")
        }
    }
}



