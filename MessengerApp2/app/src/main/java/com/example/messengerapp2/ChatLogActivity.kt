package com.example.messengerapp2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter=GroupAdapter<ViewHolder>()
    var toUser: User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        RecyclerViewChatLog.adapter=adapter
        toUser = intent.getParcelableExtra<User>(NewMessegeActivity.USER_KEY)

        //supportActionBar?.title="Chat Log"
        ///val user = intent.getParcelableExtra<User>(NewMessegeActivity.USER_KEY)
        //supportActionBar?.title=user.username
        //val username= intent.getStringExtra(NewMessegeActivity.USER_KEY)
        supportActionBar?.title= toUser?.username


        //setupDummyData()
        listenForMessages()


        SendButtonChatLog.setOnClickListener {
            Log.d(TAG, "Attempt to send message....")
            performSendMessage()
        }
    }

    private fun listenForMessages()
    {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        //val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        val ref=FirebaseDatabase.getInstance().getReference("/user-messeges/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatToItem(chatMessage.text))
                    } else {
                        adapter.add(ChatFromItem(chatMessage.text))
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long) {
        constructor() : this("", "", "", "", -1)
    }

    private fun performSendMessage()
    {
        val text=EditTextChatLog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessegeActivity.USER_KEY)
        val toId = user?.uid

        if (fromId == null) return

//        val reference=FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference=FirebaseDatabase.getInstance().getReference("/user-messeges/$fromId/$toId").push()
        val toReference=FirebaseDatabase.getInstance().getReference("/user-messeges/$toId/$fromId").push()
        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId!!, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")

                EditTextChatLog.text.clear()
                RecyclerViewChatLog.scrollToPosition(adapter.itemCount-1)
            }
        toReference.setValue(chatMessage)



    }
//    private fun setupDummyData()
//    {
//        val adapter=GroupAdapter<ViewHolder>()
//        adapter.add(ChatFromItem())
//        adapter.add(ChatToItem())
//        adapter.add(ChatFromItem())
//        adapter.add(ChatToItem())
//        adapter.add(ChatFromItem())
//        adapter.add(ChatToItem())
//        adapter.add(ChatFromItem())
//        adapter.add(ChatToItem())
//        adapter.add(ChatFromItem())
//        adapter.add(ChatToItem())
//        adapter.add(ChatFromItem())
//        adapter.add(ChatToItem())
//
//        RecyclerViewChatLog.adapter=adapter
//    }
}

class ChatFromItem(val text:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView4.text=text


    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView3.text=text

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}