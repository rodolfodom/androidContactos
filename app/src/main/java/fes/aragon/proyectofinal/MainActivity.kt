package fes.aragon.proyectofinal
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import fes.aragon.proyectofinal.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding // data binding
    private val provider = OAuthProvider.newBuilder("github.com")




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnGithub.setOnClickListener {
            signInWithProvider(provider)
        }

        binding.btnGoogle.setOnClickListener {
            googleSignIn()
        }
    }

    fun signInWithProvider(provider: OAuthProvider.Builder) {
        // [START auth_oidc_provider_signin]
        firebaseAuth
            .startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener {
                // User is signed in.
                // IdP data available in
                // authResult.getAdditionalUserInfo().getProfile().
                // The OAuth access token can also be retrieved:
                // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                // The OAuth secret can be retrieved by calling:
                // ((OAuthCredential)authResult.getCredential()).getSecret().
                Toast.makeText(this, "Usuario autenticado con Github", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ContactsActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                // Handle failure.
                Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()
            }
        // [END auth_oidc_provider_signin]
    }

    private fun googleSignIn() {
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, googleConf)
        startActivityForResult(googleClient.signInIntent, 100)
        googleClient.signOut()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(this, "Error al autenticar con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Usuario autenticado con Google", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ContactsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Autenticación con Firebase fallida.", Toast.LENGTH_SHORT).show()
                }
            }
    }

}