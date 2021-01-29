package com.example.livetrackingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.livetrackingapp.databinding.ActivityMainBinding
import com.example.livetrackingapp.utils.UserSharedPreference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.btnLogin.setOnClickListener {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                viewBinding.email.text.toString(),
                viewBinding.password.text.toString()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    getUserData()
                    Toast.makeText(this@MainActivity, "Login Success", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this@MainActivity, "" + task.exception, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        viewBinding.btnRegister.setOnClickListener {
            val userModel = UserModel(
                viewBinding.name.text.toString(),
                viewBinding.email.text.toString(),
                viewBinding.password.text.toString()
            )
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                viewBinding.email.text.toString(),
                viewBinding.password.text.toString()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storeUser(userModel)
                    //Toast.makeText(MainActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(this@MainActivity, "" + task.exception, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getUserData() {
        FirebaseFirestore.getInstance().collection("Users")
            .document(viewBinding.email.text.toString())
            .get().addOnSuccessListener { documentSnapshot ->
                val model: UserModel? = documentSnapshot.toObject(UserModel::class.java)
                Toast.makeText(
                    this@MainActivity,
                    "Logged in as " + model?.userName,
                    Toast.LENGTH_SHORT
                ).show()
                if (model != null) {
                    UserSharedPreference.instance?.userLogin(model, this)
                    startActivity(Intent(this, MapActivity::class.java))
                    finish()
                }
            }
    }

    private fun storeUser(userModel: UserModel) {
        FirebaseFirestore.getInstance().collection("Users")
            .document(viewBinding.email.text.toString()).set(userModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "User Stored Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else Toast.makeText(this@MainActivity, "" + task.exception, Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
