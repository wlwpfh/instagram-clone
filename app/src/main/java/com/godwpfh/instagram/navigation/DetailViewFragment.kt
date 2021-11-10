package com.godwpfh.instagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.godwpfh.instagram.R
import com.godwpfh.instagram.navigation.model.AlarmDTO
import com.godwpfh.instagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*
import java.util.*

class DetailViewFragment : Fragment(){
    var firestore : FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view=LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container, false);

        firestore= FirebaseFirestore.getInstance()
        uid=FirebaseAuth.getInstance().currentUser?.uid

        view.detail_fragment_recycler_view.adapter=DetailViewRecyclerViewAdapter()
        view.detail_fragment_recycler_view.layoutManager=LinearLayoutManager(activity) //보여지는 걸 linearlayout으로 보여지기 위해해
       return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                if(querySnapshot == null ) return@addSnapshotListener



                for(snapshot in querySnapshot!!.documents){ //들어오는 값 찍기
                    System.out.println(snapshot.data);

                   // if(firestore?.collection("users")!!.document(snapshot.id).toString()==firestore?.collection()) {
                        //내가 팔로우하는 사람들이 보이게끔
                        //게시글의 주인 == 내가 팔로우하는 사람
                    //  java.lang.NullPointerException: Attempt to invoke virtual method 'java.io.File java.io.File.getParentFile()' on a null object reference
                    // 밑의 if문에 대한 에러 메시지
                    //if(snapshot.data?.get(uid) in arrayOf(firestore?.collection("users")!!.document(uid!!).collection("following"))){
                      //  snapshot.data?.get(uid)
                        var item = snapshot.toObject(ContentDTO::class.java) //ContentDTO방식으로 casting
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    //}
                }
                Collections.reverse(contentDTOs)
                notifyDataSetChanged() //새로고침
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view=LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            //mapping 작업

            viewholder.detail_profile_name.text=contentDTOs!![position].userId
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewholder.detail_image_content)
            viewholder.detail_content_explain.text=contentDTOs!![position].explain
            viewholder.detail_heart_count.text= "Likes "+contentDTOs!![position].favoriteCount.toString()

            //좋아요 버튼에 이벤트 달기
            viewholder.detail_heart_click.setOnClickListener {
                favoriteEvent(position)
            }
            //좋아요 버튼 누른 수 적용
            if(contentDTOs!![position].favorites.containsKey(uid)){ //좋아요 버튼이 클릭
                viewholder.detail_heart_click.setImageResource(R.drawable.ic_favorite)
            }else{ //아직 클릭하지 않은 경우
                viewholder.detail_heart_click.setImageResource(R.drawable.ic_favorite_border)
            }

            //프로필 이미지 클릭된 경우
            viewholder.detail_profile_image.setOnClickListener {
                var fragment=UserFragment()
                var bundle=Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("userID",contentDTOs[position].userId)
                fragment.arguments=bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
            viewholder.detail_comment_click?.setOnClickListener { view ->
                var intent=Intent(view.context, CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[position])
                intent.putExtra("destinationUid",contentDTOs[position].uid)
                startActivity(intent)

            }
            if(itemCount==0)
                Toast.makeText(context, "팔로우를 하여 상대방의 게시물을 확인하세요",Toast.LENGTH_LONG).show();

       }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction {     transaction ->
                var uid= FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO=transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){ //클릭 O -> 클릭 X
                    contentDTO?.favoriteCount=contentDTO?.favoriteCount-1
                    contentDTO.favorites.remove(uid)
                }else{
                    //클릭X -> 클릭 O
                    contentDTO?.favoriteCount=contentDTO?.favoriteCount+1
                    contentDTO?.favorites[uid!!]=true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                //트랜잭션을 서버로 돌려주기
                transaction.set(tsDoc, contentDTO)
            }

        }
        fun favoriteAlarm(destinationUid: String){
            var alarmDTO= AlarmDTO()
            alarmDTO.destinationUid=destinationUid
            alarmDTO.userId=FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid=FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind=0
            alarmDTO.timestamp=System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)


        }
    }
}