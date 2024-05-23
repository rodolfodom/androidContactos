package fes.aragon.proyectofinal

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso

class ContactAdapter(private val contacts: MutableList<Contact>, private val activity: ContactsActivity) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private val userId = Firebase.auth.currentUser?.uid
    private val db = Firebase.firestore

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.avatar)
        val name: TextView = itemView.findViewById(R.id.name)
        val lastName: TextView = itemView.findViewById(R.id.lastName)
        val phone: TextView = itemView.findViewById(R.id.phone)
        val email: TextView = itemView.findViewById(R.id.email)
        val editBtn: Button = itemView.findViewById(R.id.editButton)
        val deleteBtn: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_card, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        // Aquí puedes cargar la imagen en avatar usando una biblioteca como Glide o Picasso
        Picasso.get().load(contact.getAvatar()).into(holder.avatar)
        holder.name.text = contact.getName()
        holder.lastName.text = contact.getLastName()
        holder.phone.text = contact.getPhone()
        holder.email.text = contact.getEmail()
        holder.deleteBtn.setOnClickListener {
            // Aquí puedes abrir la actividad de edición de contactos y pasar el ID del contacto
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminar contacto")
                .setMessage("¿Estás seguro de eliminar a ${contact.getName()} de tus contactos?")
                .setPositiveButton("Estoy seguro") { dialog, _ ->
                    // Aquí puedes eliminar el contacto de la base de datos
                    val contactRef = db.collection("appContactos").document(userId.toString()).collection("contactos").document(contact.getId())
                    contactRef.delete()
                        .addOnSuccessListener {
                            // Aquí puedes mostrar un mensaje de éxito
                            activity.getContacts()
                            Toast.makeText(holder.itemView.context, "Contacto eliminado", Toast.LENGTH_SHORT).show()
                            //dialog.dismiss()
                        }
                        .addOnFailureListener {
                            // Aquí puedes mostrar un mensaje de error
                            Toast.makeText(holder.itemView.context, "Error al eliminar contacto", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        holder.editBtn.setOnClickListener {
            val intent = Intent(holder.itemView.context, CreateContactActivity::class.java)
            intent.putExtra("contactId", contact.getId())
            intent.putExtra("contactName", contact.getName())
            intent.putExtra("contactLastName", contact.getLastName())
            intent.putExtra("contactPhone", contact.getPhone())
            intent.putExtra("contactEmail", contact.getEmail())
            intent.putExtra("contactAvatar", contact.getAvatar())

            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount() = contacts.size
}