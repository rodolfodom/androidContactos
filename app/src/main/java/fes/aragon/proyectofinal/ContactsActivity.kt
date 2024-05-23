package fes.aragon.proyectofinal

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import fes.aragon.proyectofinal.databinding.ActivityContactsBinding

class ContactsActivity : AppCompatActivity() {
    private val  db = Firebase.firestore
    private lateinit var contactsRef: CollectionReference
    private lateinit var binding: ActivityContactsBinding
    private val userId = Firebase.auth.currentUser?.uid
    private val contacts = mutableListOf<Contact>()
    private lateinit var adapter: ContactAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        adapter = ContactAdapter(contacts, this)
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.contactsRecyclerView.adapter = adapter
        setContentView(binding.root)
        binding.createContactButton.setOnClickListener {
            val intent = Intent(this, CreateContactActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        getContacts()
    }

    fun getContacts() {
        if (!isOnline(this)) {
            binding.noContacts.visibility = View.VISIBLE
            binding.noContacts.text = "No hay conexión a internet"
            binding.contactsRecyclerView.visibility = View.GONE
            return
        }
        contacts.clear()
        contactsRef = db.collection("appContactos").document(userId.toString()).collection("contactos")
        contactsRef.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                Log.d("ContactsActivity", "No contacts")
                binding.noContacts.visibility = View.VISIBLE
                binding.contactsRecyclerView.visibility = View.GONE
            }else{
                binding.noContacts.visibility = View.GONE
                binding.contactsRecyclerView.visibility = View.VISIBLE
            }
            for (document in documents) {
                val id = document.id
                val name = document.data["name"].toString()
                val lastName = document.data["lastName"].toString()
                val phone = document.data["phone"].toString()
                val email = document.data["email"].toString()
                val avatar = document.data["avatar"].toString()
                val contact = Contact(id, name, lastName, phone, email, avatar)
                contacts.add(contact)
                Log.d("document", "Contact: $document")
            }
            Log.d("ContactsActivity", "Contacts: $contacts")
            adapter.notifyDataSetChanged()
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