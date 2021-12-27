package com.albasil.finalprojectkotlinbootcamp.SecondFragment

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.albasil.finalprojectkotlinbootcamp.Adapter.ArticleUserProfileAdapter
import com.albasil.finalprojectkotlinbootcamp.Firebase.FirestoreUser
import com.albasil.finalprojectkotlinbootcamp.R
import com.albasil.finalprojectkotlinbootcamp.data.Article
import com.albasil.finalprojectkotlinbootcamp.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firestore.v1.FirestoreGrpc
import kotlinx.android.synthetic.main.fragment_home_page.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.fragment_user_profile.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UserProfile : Fragment() {

    lateinit var binding: FragmentUserProfileBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var articleList: ArrayList<Article>
    private lateinit var articleAdapter: ArticleUserProfileAdapter
    private lateinit var myFollowers: String
    private lateinit var myFollowing: String

    private lateinit var fireStore: FirebaseFirestore


    private val args by navArgs<UserProfileArgs>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)


        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.userRecyclerView_xml)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.setHasFixedSize(true)
        articleList = arrayListOf()
        articleAdapter = ArticleUserProfileAdapter(articleList)

        recyclerView.adapter = articleAdapter

        val myId = FirebaseAuth.getInstance().currentUser?.uid

        getMyInfo(myId.toString())


        //***************************************
        getArticlesUser("${args.userID.toString()}")
        getUserPhoto(args.userID.toUri())
        getUserInfo(args.userID.toString())

        //***************************************


        binding.userInforCallXml.setOnClickListener {

            expandedCallUser()

        }//linearLayOutUserInfo_xml
        binding.showUserInfoXml.setOnClickListener {

            expandedUserInfo()

        }


        //---------------------------------------------------------
        followingOrNot(args.userID.toString())

        binding.btnFollowXml.setOnClickListener {

            followersUser(args.userID.toString())

        }


    }

    fun followingOrNot(userId: String) = CoroutineScope(Dispatchers.IO).launch {

        val myId = FirebaseAuth.getInstance().currentUser?.uid

        fireStore.collection("Users").document("$userId")
            .collection("Followers").document(myId.toString())
            .get().addOnCompleteListener {
                it
                if (it.result?.exists()!!) {


                    binding.btnFollowXml.setText("Following")
                    binding.btnFollowXml.setBackgroundColor(Color.GRAY)


                } else {

                    binding.btnFollowXml.setText("Follow")
                    binding.btnFollowXml.setBackgroundColor(Color.CYAN)

                }

            }
    }

    fun followersUser(userId: String) = CoroutineScope(Dispatchers.IO).launch {

        val myId = FirebaseAuth.getInstance().currentUser?.uid

        fireStore.collection("Users").document("$userId")
            .collection("Followers").document(myId.toString())
            .get().addOnCompleteListener {
                it
                if (it.result?.exists()!!) {

                    unfollowDialog(myId.toString(), userId.toString())
                    //showdialoguserInfo(myId.toString(),userId.toString())


                } else {
                    binding.btnFollowXml.setText("Following")
                    binding.btnFollowXml.setBackgroundColor(Color.GRAY)
                    //addFollowers
                    addFollowers(myId.toString(), userId.toString())
                    addFollowing(myId.toString(), userId.toString())


                }
            }
    }

    fun unfollowDialog(myId: String, userId: String) {

        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Unfollow User")
        builder.setMessage("Are you sure to unfollow?")
        builder.setIcon(R.drawable.ic_baseline_cancel_24)

        builder.setPositiveButton("Yes") { _, _ ->

            binding.btnFollowXml.setText("Follow")
            binding.btnFollowXml.setBackgroundColor(Color.CYAN)
            deleteFollowing(myId.toString(), userId.toString())
            deleteFollowers(myId.toString(), userId.toString())


        }

        builder.setNegativeButton("Cancel", { _, _ -> })
        builder.show()
    }


    fun addFollowers(myId: String, userId: String) {

        val upDateFollowers = hashMapOf(
            "userId" to "${myId.toString()}",
        )
        fireStore.collection("Users").document("${userId}")
            .collection("Followers").document("${myId}").set(upDateFollowers)


        //***************************************************************************************
        var upDateFollowing = binding.userFollowersXml.text.toString().toInt()
        upDateFollowing++
        binding.userFollowersXml.text = upDateFollowing.toString()

        fireStore.collection("Users").document(userId.toString())
            .update("followers", upDateFollowing)
        //***************************************************************************************

    }


    fun deleteFollowers(myId: String, userId: String) {
        fireStore.collection("Users").document("${userId}")
            .collection("Followers").document("${myId}").delete()

        //***************************************************************************************
        var upDateFollowing = binding.userFollowersXml.text.toString().toInt()
        upDateFollowing--
        binding.userFollowersXml.text = upDateFollowing.toString()


        Toast.makeText(context, "upDateFollowing ${upDateFollowing}", Toast.LENGTH_LONG).show()
        fireStore.collection("Users").document(userId.toString())
            .update("followers", upDateFollowing)
        //***************************************************************************************

    }

    fun addFollowing(myId: String, userId: String) {

        val upDateFollowing = hashMapOf("userId" to "${userId.toString()}")

        fireStore.collection("Users").document("${myId}")
            .collection("Following").document("${userId}").set(upDateFollowing)

        // ***************************************************************************************


        var myFollowing = myFollowing.toInt()
        myFollowing++

        fireStore.collection("Users").document(myId.toString())
            .update("following", myFollowing)
        //***************************************************************************************

    }

    fun deleteFollowing(myId: String, userId: String) {
        fireStore.collection("Users").document("${myId}")
            .collection("Following").document("${userId}").delete()


        var myFollowing = myFollowing.toInt()
        myFollowing--
        fireStore.collection("Users").document(myId.toString())
            .update("following", myFollowing)
    }


    private fun getArticlesUser(userId: String) {

        fireStore = FirebaseFirestore.getInstance()
        fireStore.collection("Articles").whereEqualTo("userId", "${userId}")
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
                    binding.userArticlesXml.setText(articleList.size.toString())


                    articleAdapter.notifyDataSetChanged()


                }

            })

    }


    fun getUserInfo(userID: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            //coroutine
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .document("$userID")
                .get().addOnCompleteListener {
                    it

                    if (it.result?.exists()!!) {

                        //+++++++++++++++++++++++++++++++++++++++++
                        var name = it.result!!.getString("userName")
                        var userEmail = it.result!!.getString("userEmail")
                        var userFollowing = it.result!!.get("following")
                        var userFollowers = it.result!!.get("followers")
                        var userPhone = it.result!!.getString("userPhone")

                        Log.e("user Info", "userName ${name.toString()} \n ${userEmail.toString()}")


                        binding.userNameXml.text = "${name.toString()}"
                        binding.userInfoXml.text = "${userEmail.toString()}"
                        binding.userFollowersXml.text = "${userFollowers?.toString()}"
                        binding.userFollowingXml.text = "${userFollowing?.toString()}"

                        binding.userCallXml.setOnClickListener {

                            callUser("${userPhone.toString()}")

                        }
                        binding.userEmailXml.setOnClickListener {

                            userEmail(userEmail.toString())

                        }

                    } else {
                        Log.e("error \n", "errooooooorr")
                    }


                }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // Toast.makeText(coroutineContext,0,0, e.message, Toast.LENGTH_LONG).show()
                Log.e("FUNCTION createUserFirestore", "${e.message}")
            }
        }


    }


    fun getMyInfo(userID: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .document("$userID")
                .get().addOnCompleteListener {
                    it
                    if (it.result?.exists()!!) {
                        //+++++++++++++++++++++++++++++++++++++++++
                        var name = it.result!!.getString("userName")
                        var userEmail = it.result!!.getString("userEmail")
                        var userFollowing = it.result!!.get("following")
                        var userFollowers = it.result!!.get("followers")
                        var userInfo = it.result!!.getString("moreInfo")



                        myFollowers = userFollowers.toString()
                        myFollowing = userFollowing.toString()


                    } else {
                        Log.e("error \n", "errooooooorr")
                    }
                }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // Toast.makeText(coroutineContext,0,0, e.message, Toast.LENGTH_LONG).show()
                Log.e("FUNCTION createUserFirestore", "${e.message}")
            }
        }


    }

    fun getUserPhoto(userPhoto: Uri) {


        val storageRef = FirebaseStorage.getInstance().reference
            .child("imagesUsers/$userPhoto")


        val localFile = File.createTempFile("tempImage", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {


            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)

            binding.userImageXml.load(bitmap)


        }.addOnFailureListener {
            /*if (progressDialog.isShowing)
                progressDialog.dismiss()*/

            //   Toast.makeText(context,"Failed image ",Toast.LENGTH_SHORT).show()

        }
    }


    fun callUser(userNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${userNumber.toString()}")
        startActivity(intent)

    }

    fun userEmail(userEmail: String) {


        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, "${userEmail.toString()}")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")

        if (activity?.let { it -> intent.resolveActivity(it.packageManager) } != null) {
            startActivity(intent)
        }


    }


    fun expandedCallUser() {

        if (binding.linearLayoutCallUserXml.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(
                binding.linearLayoutCallUserXml,
                AutoTransition()
            )
            binding.linearLayoutCallUserXml.visibility = View.VISIBLE

        } else {
            TransitionManager.beginDelayedTransition(
                binding.linearLayoutCallUserXml,
                AutoTransition()
            )
            binding.linearLayoutCallUserXml.visibility = View.GONE
        }

    }


    fun expandedUserInfo() {

        if (binding.linearLayOutUserInfoXml.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(
                binding.linearLayOutUserInfoXml,
                AutoTransition()
            )
            binding.linearLayOutUserInfoXml.visibility = View.VISIBLE

        } else {
            TransitionManager.beginDelayedTransition(
                binding.linearLayOutUserInfoXml,
                AutoTransition()
            )
            binding.linearLayOutUserInfoXml.visibility = View.GONE
        }

    }


}