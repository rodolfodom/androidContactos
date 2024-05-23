package fes.aragon.proyectofinal

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import fes.aragon.proyectofinal.databinding.ActivityCreateContactBinding

class CreateContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateContactBinding
    private val SELECT_IMAGE_CODE = 1000
    private val storage = Firebase.storage
    private var storageRef = storage.reference
    private val avatarsRef: StorageReference? = storageRef.child("avatars")
    private var avatarUri: Uri = Uri.EMPTY
    private val userId = Firebase.auth.currentUser?.uid
    private val db = Firebase.firestore
    private lateinit var contactId:String
    private var contactImg:String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactId = intent.getStringExtra("contactId").toString()

        if (contactId != "null") {
            val contactName = intent.getStringExtra("contactName")
            val contactLastName = intent.getStringExtra("contactLastName")
            val contactPhone = intent.getStringExtra("contactPhone")
            val contactEmail = intent.getStringExtra("contactEmail")
            val contactAvatar = intent.getStringExtra("contactAvatar")
            contactImg = contactAvatar.toString()
            binding.nameInput.setText(contactName)
            binding.lastNameInput.setText(contactLastName)
            binding.phoneInput.setText(contactPhone)
            binding.emailInput.setText(contactEmail)
            Picasso.get().load(contactAvatar).into(binding.avatar)
            binding.ContactsTitle.text = "Editar contacto"
        }

        binding.avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_IMAGE_CODE)
        }

        binding.createContactButton.setOnClickListener {
            if (contactId == "null") {
                saveUser()
            } else {
                updateUser()
            }
        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE_CODE && resultCode == RESULT_OK) {
            val selectedImage = data?.data
            binding.avatar.setImageURI(selectedImage)
            avatarUri = selectedImage!!
        }
    }

    fun updateUser() {
        if (!isOnline(this)) {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }
        val name = binding.nameInput.text.toString()
        val lastName = binding.lastNameInput.text.toString()
        val phone = binding.phoneInput.text.toString()
        val email = binding.emailInput.text.toString()
        val user = hashMapOf(
            "name" to name,
            "lastName" to lastName,
            "phone" to phone,
            "email" to email,
            "avatar" to contactImg
        )

        if (name.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if(avatarUri != Uri.EMPTY){
            val ref = avatarsRef?.child("$email-${System.currentTimeMillis()}-${avatarUri.lastPathSegment}")
            val uploadTask = ref?.putFile(avatarUri)
            uploadTask?.addOnFailureListener{
                Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }?.addOnSuccessListener{taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener{uri ->
                    Toast.makeText(this, "Imagen subida", Toast.LENGTH_SHORT).show()
                    user["avatar"] = uri.toString()
                    val contactRef = db.collection("appContactos").document(userId.toString()).collection("contactos").document(contactId)
                    contactRef.set(user).addOnSuccessListener {
                        Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener{
                        Toast.makeText(this, "Error al actualizar contacto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else{
            val contactRef = db.collection("appContactos").document(userId.toString()).collection("contactos").document(contactId)
            contactRef.set(user).addOnSuccessListener {
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener{
                Toast.makeText(this, "Error al actualizar contacto", Toast.LENGTH_SHORT).show()
            }
        }

    }



    private fun saveUser() {
        if (!isOnline(this)) {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }
        val name = binding.nameInput.text.toString()
        val lastName = binding.lastNameInput.text.toString()
        val phone = binding.phoneInput.text.toString()
        val email = binding.emailInput.text.toString()
        val ref = avatarsRef?.child("$email-${System.currentTimeMillis()}-${avatarUri.lastPathSegment}")
        val uploadTask = ref?.putFile(avatarUri)


        if(avatarUri == Uri.EMPTY){
            Toast.makeText(this, "Selecciona una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Guardado...", Toast.LENGTH_SHORT).show()
        uploadTask?.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
        }?.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                val user = hashMapOf(
                    "name" to name,
                    "lastName" to lastName,
                    "phone" to phone,
                    "email" to email,
                    "avatar" to uri.toString()
                )

                val userCollectionRef = db.collection("appContactos").document(userId.toString()).collection("contactos")
                userCollectionRef.add(user)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
                        Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                        Toast.makeText(this, "Error al guardar contacto", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}