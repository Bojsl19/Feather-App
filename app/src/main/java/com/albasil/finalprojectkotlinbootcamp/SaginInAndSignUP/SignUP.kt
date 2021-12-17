package com.albasil.finalprojectkotlinbootcamp.SaginInAndSignUP

import android.os.Bundle
import android.service.autofill.UserData
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.albasil.finalprojectkotlinbootcamp.R
import com.albasil.finalprojectkotlinbootcamp.data.Users
import com.albasil.finalprojectkotlinbootcamp.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUP : Fragment() {

    lateinit var binding: FragmentSignUpBinding


   // val userId = FirebaseAuth.getInstance().currentUser?.uid


    /* var etEmail = binding.etEmailXml.text.toString()
     var etPassword = binding.etPasswordXml.text.toString()
     var etUserName = binding.etUserNameXml.text.toString()
     var etPhone = binding.etPhoneXml.toString()*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment


        binding = FragmentSignUpBinding.inflate(inflater,container,false)


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


      //  val fireBaseAuth =FirebaseAuthentication()

        //fireBaseAuth.registerUser("basil@hotmail.com","123456")


        binding.btnSignUpXml.setOnClickListener {

            SignUpFirebaseAuth()
        }

    }

    private fun SignUpFirebaseAuth() {
        when {
            TextUtils.isEmpty(binding.etEmailXml.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(context, "Please Enter Email", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etPasswordXml.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(context, "Please Enter Full Name", Toast.LENGTH_LONG).show()

            }
            TextUtils.isEmpty(binding.etPhoneXml.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(context, "Please Enter Phone Number", Toast.LENGTH_LONG).show()

            }
            TextUtils.isEmpty(binding.etUserNameXml.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(context, "Please Enter Password", Toast.LENGTH_LONG).show()

            }else -> {

                registerUser("${binding.etEmailXml.text.toString()}","${binding.etPasswordXml.text.toString()}")

            }
        }

        }


    //class Firebase
    fun registerUser(email:String , password:String) {

        val email: String = email.toString().trim { it <= ' ' }
        val password: String = password.toString().trim { it <= ' ' }

        //Phone number must be 10

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    //firebase register user


                    Log.e("OK","registration is sucessfully done")


                    insertUser("${email.toString()}",
                        "${binding.etUserNameXml.text.toString()}",
                        "${binding.etPhoneXml.text.toString()}")

                    findNavController().navigate(R.id.action_signUP_to_setting)




                } else {

                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()


                }
            }.addOnCompleteListener {


            }



    }



    //class
     fun insertUser(email: String, userName: String, userPhone: String) {
         val userId = FirebaseAuth.getInstance().currentUser?.uid


         val user =Users()
         user.userId = userId.toString()
         user.userEmail = email.toString()
         user.userNamae = userName.toString()
         user.userPhone = userPhone.toString()


        createUserFirestore(user)
    }



    //firebase class
     fun createUserFirestore(user: Users) = CoroutineScope(Dispatchers.IO).launch {

        try {
            val userRef = Firebase.firestore.collection("Users")
            //-----------UID------------------------
            val userId = FirebaseAuth.getInstance().currentUser?.uid



            userRef.document("$userId").set(user).addOnCompleteListener { it
                when {
                    it.isSuccessful -> {


                        Toast.makeText(context, "is Successful fire store", Toast.LENGTH_LONG).show()



                    }
                    else -> {

                        Toast.makeText(context, "is not Successful fire store ", Toast.LENGTH_LONG).show()


                    }


                }
            }

            withContext(Dispatchers.Main) {
                //Toast.makeText(coroutineContext.javaClass, "Welcome ${user.fullName.toString()}", Toast.LENGTH_LONG).show()

            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // Toast.makeText(coroutineContext,0,0, e.message, Toast.LENGTH_LONG).show()
                Log.e("FUNCTION createUserFirestore", "${e.message}")
            }
        }
    }





}
