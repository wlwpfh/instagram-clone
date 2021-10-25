package com.godwpfh.instagram.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.godwpfh.instagram.LogInActivity
import com.godwpfh.instagram.MainActivity
import com.godwpfh.instagram.R
import com.godwpfh.instagram.navigation.model.AlarmDTO
import com.godwpfh.instagram.navigation.model.ContentDTO
import com.godwpfh.instagram.navigation.model.FollowDTO
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
    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    } //static처럼
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView= LayoutInflater.from(activity).inflate(R.layout.fragment_user,container, false);

        uid=arguments?.getString("destinationUid")
        firestore= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()
        currentUserUid=auth?.currentUser?.uid

        if(uid==currentUserUid){
            //내 계정 페이지일 때 - follow 버튼 -> 로그아웃 버튼으로 바꾸기
            fragmentView?.account_follow_signout?.text="프로필 편집하기"
            fragmentView?.account_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,EditProfileActivity::class.java))
            }
            fragmentView?.account_signout?.setOnClickListener {
                startActivity(Intent(activity, LogInActivity::class.java))
                auth?.signOut()
            }
            fragmentView?.account_email?.text=auth?.currentUser?.email
        }else{
            //다른 사람 계정 페이지일 때 - follow버튼으로
            fragmentView?.account_follow_signout?.text=getText(R.string.follow)
            var mainactivity = (activity as MainActivity)
            //mainactivity?.toolbar_username?.text=arguments?.getString("userID")
            mainactivity?.toolbar_back?.setOnClickListener{
                mainactivity.bottom_navigation.selectedItemId=R.id.action_home
            }
            mainactivity?.toolbar_logo?.visibility=View.GONE
            //mainactivity?.toolbar_username?.visibility=View.VISIBLE
            mainactivity?.toolbar_back?.visibility=View.VISIBLE

            fragmentView?.account_follow_signout?.setOnClickListener {
                requestFollow()
            }
            fragmentView?.account_signout?.visibility=View.GONE

        }

        fragmentView?.account_recycler?.adapter=UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recycler?.layoutManager=GridLayoutManager(activity, 3)

        fragmentView?.account_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type="images/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)

        }
        getProfileImage()
        getFollowerAndFollowing()
        return fragmentView
    }
    fun requestFollow(){
        //내 계정에서 누구를 팔로우 하는지
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO==null){
                followDTO= FollowDTO()
                followDTO!!.followingCount=1
                followDTO!!.followings[uid!!]=true
                followerAlarm(uid!!)
                transaction.set(tsDocFollowing,followDTO)
                return@runTransaction
            }
            if(followDTO.followings.containsKey(uid)){ //내가 이미 팔로우한 상태
                //팔로잉 취소
                followDTO?.followingCount=followDTO?.followingCount-1
                followDTO?.followings?.remove(uid)
            }else{
                //팔로잉을 하는 경우
                followDTO?.followingCount=followDTO?.followingCount+1
                followDTO?.followings[uid!!]=true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }
        //상대방 계정에 타인이 팔로우 하는 부분
        var tsDocFollower=firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction {  transaction ->
            var followDTO= transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO==null){
                followDTO=FollowDTO()
                followDTO!!.followerCount=1
                followDTO!!.followers[currentUserUid!!]=true

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }
            if(followDTO!!.followers.containsKey(currentUserUid)){
                //사대방 계정을 내가 팔로우한 경우
                followDTO!!.followerCount=followDTO!!.followerCount-1
                followDTO!!.followers.remove(currentUserUid!!)
            }else {
                followDTO!!.followerCount=followDTO!!.followerCount+1
                followDTO!!.followers[currentUserUid!!]=true
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }
    fun getFollowerAndFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { value, error ->
            //변경된 팔로워 팔로잉 수를 실시간으로 불러오기
            if(value==null){
                return@addSnapshotListener
            }
            var followDTO=value.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount !=null){
                fragmentView?.account_following_count?.text= followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount!=null){
                fragmentView?.account_follower_count?.text=followDTO?.followerCount?.toString()

                if(followDTO?.followers?.containsKey(currentUserUid!!)){
                    //팔로우하고 있는 경우 버튼 글자 변경
                    fragmentView?.account_follow_signout?.text=getString(R.string.follow_cancel)
                    fragmentView?.account_follow_signout?.background?.
                    setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorLightGray)
                            ,PorterDuff.Mode.MULTIPLY)

                }else{
                    if(uid != currentUserUid){
                        fragmentView?.account_follow_signout?.text=getString(R.string.follow)
                        fragmentView?.account_follow_signout?.background?.colorFilter=null
                    }
                }
            }
        }
    }
    fun followerAlarm(destinationUid :String){ //팔로우 클릭시 알람
        var alarmDTO= AlarmDTO()
        alarmDTO.destinationUid=destinationUid
        alarmDTO.userId=auth?.currentUser?.email
        alarmDTO.uid=auth?.currentUser?.uid
        alarmDTO.kind=2
        alarmDTO.timestamp=System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }
    fun getProfileImage(){
        firestore?.collection("profileUserImage")?.document(uid!!)?.addSnapshotListener { value, error ->
            if(value == null) return@addSnapshotListener
            if(value.data!=null){
                //이미지 주소 받아오기
                var url=value?.data!!["images"]
                //다운받아서 바로 적용하기
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop())
                        .into(fragmentView?.account_profile!!)
            }
        }
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

            fragmentView?.account_email?.text=ContentDTOs[position].userId
        }

        override fun getItemCount(): Int {
            return ContentDTOs.size
        }

    }
}