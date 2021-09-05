package com.godwpfh.instagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.godwpfh.instagram.LogInActivity
import com.godwpfh.instagram.MainActivity
import com.godwpfh.instagram.R
import com.godwpfh.instagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() { //내 계정에 대한 정보, 상대 계정에 대한 정보
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView= LayoutInflater.from(activity).inflate(R.layout.fragment_user,container, false);

        uid=arguments?.getString("destinationUid")
        firestore= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()
        currentUserUid=auth?.currentUser?.uid

        if(uid==currentUserUid){
            //내 계정 페이지일 때 - follow 버튼 -> 로그아웃 버튼으로 바꾸기
            fragmentView?.account_follow_signout?.text=getText(R.string.signout)
            fragmentView?.account_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,LogInActivity::class.java))
                auth?.signOut()
            }

        }else{
            //다른 사람 계정 페이지일 때 - follow버튼으로
            fragmentView?.account_follow_signout?.text=getText(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity?.toolbar_username?.text=arguments?.getString("userID")
            mainactivity?.toolbar_back?.setOnClickListener{
                mainactivity.bottom_navigation.selectedItemId=R.id.action_home
            }
            mainactivity?.toolbar_logo?.visibility=View.GONE
            mainactivity?.toolbar_username?.visibility=View.VISIBLE
            mainactivity?.toolbar_back?.visibility=View.VISIBLE
       }

        fragmentView?.account_recycler?.adapter=UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recycler?.layoutManager=GridLayoutManager(activity, 3)
        return fragmentView
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var ContentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init{
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { value, error ->
                if(value==null) return@addSnapshotListener

                //정보 가져오기
                for(snapshot in value.documents){
                    ContentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                fragmentView?.account_post_count?.text=ContentDTOs.size.toString()
                notifyDataSetChanged()
            }
        } //사진 가져오기
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width=resources.displayMetrics.widthPixels/3

            //width크기의 정사각형 이미지 만들기
            var imageview= ImageView(parent.context)
            imageview.layoutParams=LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //data mapping
            var imageview=(holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(ContentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return ContentDTOs.size
        }

    }
}