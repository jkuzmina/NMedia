package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.random.Random


class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val recipient = "recipientId"
    private var recipientId: Long? = null
    private var notification: String? = null
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {

        message.data[action]?.let {
           when (Action.valueOf(it)) {
              Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
           }
        }
        if(message.data.size > 0){
            var contentString = message.data.get(content)?.removePrefix("{")?.removeSuffix("}")
            if(contentString != null){
                val list = contentString.split(",")
                list.forEach {
                    val param = it.replace("\"", "")?.split(":")
                    if(param.size == 2){
                        when(param[0]){
                            recipient -> if (param[1].equals("null")) recipientId = null else recipientId = param[1].toLong()
                            content -> notification = param[1].toString()
                        }
                    }
                    /*if(param[0].equals(recipient) && param.size == 2){
                        if (param[1].equals("null")) recipientId = null else recipientId = param[1].toLong()
                    }
                    if(param[0].equals(content) && param.size == 2){
                        notification = param[1].toString()*/
                }
                if(AppAuth.getInstance().checkUserId(recipientId)){
                    notification?.let { handleContent(it) }
                }
                else{
                    AppAuth.getInstance().sendPushToken()
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun handleContent(content: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }
}

enum class Action {
    LIKE,
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

