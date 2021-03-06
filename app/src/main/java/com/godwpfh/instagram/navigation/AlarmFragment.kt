package com.godwpfh.instagram.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.godwpfh.instagram.R
import com.godwpfh.instagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment(){
    private lateinit var  callback: OnBackPressedCallback
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view= LayoutInflater.from(activity).inflate(R.layout.fragment_alarm,container, false);

        //adapter와 recyclerview연결
        view.alarmfragment_recyclerview.adapter=AlarmRecylcerViewAdapter()
        //어떻게 배치시킬 것인지
        view.alarmfragment_recyclerview.layoutManager=LinearLayoutManager(activity)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object  : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
    inner class AlarmRecylcerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init{ //adapter가 생성될 때 database를 읽어오도록
            var uid=FirebaseAuth.getInstance().currentUser?.uid

            //나에게 온 알람을 모아서 확인
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",uid)
                    .addSnapshotListener { value, error ->
                        alarmDTOList.clear()
                        if(value==null) return@addSnapshotListener

                        for(snapshot in value.documents){
                            alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                        }
                        notifyDataSetChanged()
                    }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //디자인 불러오기
            var view=LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }
        inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //종류에 따라서 알람이 다르게 가도록
            var view=holder.itemView

            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    var url=task.result!!["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.comment_profile_image)
                }
            }

            when(alarmDTOList[position].kind){
                0 -> {
                    val str_0=alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    view.comment_profile_email.text=str_0
                }
                1 -> {
                    val str_1=alarmDTOList[position].userId + " " + getString(R.string.alarm_comment)+ " of "+ alarmDTOList[position].message
                    view.comment_profile_email.text=str_1
                }
                2 -> {
                    val str_2=alarmDTOList[position].userId + " "+ getString(R.string.alarm_follow)
                    view.comment_profile_email.text=str_2
                }
            }
            view.comment_message.visibility=View.INVISIBLE
        }

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

    }
}