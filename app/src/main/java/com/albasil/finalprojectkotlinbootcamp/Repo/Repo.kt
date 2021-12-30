package com.albasil.finalprojectkotlinbootcamp.Repo

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.albasil.finalprojectkotlinbootcamp.data.Users
import com.albasil.finalprojectkotlinbootcamp.Firebase.FirebaseAuthentication
import com.albasil.finalprojectkotlinbootcamp.data.Article
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.change_password_bottom_sheet.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


var fireStore :FirebaseFirestore= FirebaseFirestore.getInstance()
class AppRepo(context: Context) {

    private var imageUrl: Uri? = null

    val firebase = FirebaseAuthentication()

    suspend fun insertUserToDB(users: Users) {


    }


    suspend fun addArticleToFirestore(article: Article) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val articleRef = Firebase.firestore.collection("Articles")
            articleRef.document(article.articleID).set(article).addOnCompleteListener {
                it
                when {
                    it.isSuccessful -> {
//                        upLoadImage("${article.articleImage.toString()}")
                        Log.e("Add Article", "Done ${article.title}")
                    }
                    else -> {
                        Log.e("Field Article", "Error ${article.title}")
                    }
                }
            }
//            withContext(Dispatchers.Main) { }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("FUNCTION createUserFirestore", "${e.message}")
            }
        }
    }


    fun upLoadImage(uIdCategory: String) {
        val storageReference =
            FirebaseStorage.getInstance().getReference("imagesArticle/${uIdCategory}")

        imageUrl?.let {
            storageReference.putFile(it)
                .addOnSuccessListener {

                    Log.e("addOnSuccessListener", "addOnSuccessListener")

                }.addOnFailureListener {
                    Log.e("Failed", "Failed")

                }
        }
    }


    fun getAllMyArticles(
        myID: String,
        articleList: MutableList<Article>
    ): LiveData<MutableList<Article>> {
        val article = MutableLiveData<MutableList<Article>>()

        fireStore = FirebaseFirestore.getInstance()
        fireStore.collection("Articles").whereEqualTo("userId", myID)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            articleList.add(dc.document.toObject(Article::class.java))
                        }
                    }
                    article.value = articleList
                }

            })
        return article
    }


    fun addUserInformation(myID: String, userInformation: String) =
        CoroutineScope(Dispatchers.IO).launch {

            val userInfo = hashMapOf("moreInfo" to "${userInformation}",)
            val userRef = Firebase.firestore.collection("Users")
            userRef.document("$myID").set(userInfo, SetOptions.merge()).addOnCompleteListener { it
                when {
                    it.isSuccessful -> {
                    }
                    else -> {
                    }
                }
            }
        }


    fun upDateUserInfo(upDateName: String, upDatePhoneNumber: String) {
        val uId = FirebaseAuth.getInstance().currentUser?.uid
        val upDateUserData = Firebase.firestore.collection("Users")
        upDateUserData.document(uId.toString()).update(
            "userName", "${upDateName.toString()}", "userPhone", "${upDatePhoneNumber.toString()}"
        )

    }


    fun getUserInfo(myID: String,userInfo :Users): LiveData<Users>{
        val user = MutableLiveData<Users>()
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document("$myID")
            .get().addOnCompleteListener { it

                if (it.result?.exists()!!) {
                    userInfo.userName = it.result!!.getString("userName").toString()
                    userInfo.userPhone = it.result!!.getString("userPhone").toString()
                    userInfo.userEmail = it.result!!.getString("userEmail").toString()
                    userInfo.moreInfo =it.result!!.getString("moreInfo").toString()
                } else {
                }
                user.value= userInfo
            }
        return user

    }

    fun deleteArticle(articleID:String){
        val deleteArticle = Firebase.firestore.collection("Articles")
            .document(articleID).delete()

        deleteArticle.addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    Log.d("Delete", "Delete Article")
                }
            }
        }
    }




    //-------------------------Settings----------------------------------
     fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String,view:View
    ) {

        if (oldPassword.isNotEmpty() &&
            newPassword.isNotEmpty() &&
            confirmNewPassword.toString().isNotEmpty()
        ) {
            Log.e("new password", "${newPassword.toString()}")
            Log.e("confirmNewPassword", "${confirmNewPassword.toString()}")

            if (newPassword.toString().equals(confirmNewPassword.toString())) {

                val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                val userEmail = FirebaseAuth.getInstance().currentUser!!.email
                if (user.toString() != null && userEmail.toString() != null) {

                    val credential: AuthCredential = EmailAuthProvider
                        .getCredential(
                            "${user?.email.toString()!!}",
                            "${oldPassword.toString()}"
                        )
                    user?.reauthenticate(credential)

                        ?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(view.context, "Auth Successful ", Toast.LENGTH_SHORT)
                                    .show()



                                //احط متغير
                                user.updatePassword("${newPassword.toString()}")
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(view.context, "isSuccessful , Update password", Toast.LENGTH_SHORT).show()


                                            view.etOldPassword_xml?.setText("")
                                            view?.etConfirmNewPassword_xml?.setText("")
                                            view?.etNewPassword_xml?.setText("")

                                        }
                                    }

                            } else {

                                Toast.makeText( view.context, "Auth Failed ", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                } else {
                  /*  Toast.makeText(
                        view.context,
                        "كلمة المرور القديمة غير صحيحه ",
                        Toast.LENGTH_SHORT
                    ).show()*/

                }

            } else {
               /* Toast.makeText(
                    view.context,
                    "New Password is not equals Confirm New Password.",
                    Toast.LENGTH_SHORT
                ).show()*/
            }

        } else {
//            Toast.makeText(view.context, "Please enter all the fields", Toast.LENGTH_LONG).show()
//
//            Toast.makeText( view.context, "Please enter all the fields", Toast.LENGTH_SHORT).show()
        }

    }

}

