package com.china.coolpopup

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.LogUtils
import androidx.core.app.RemoteInput


/**
 * Doc说明 (此类核心功能):
 * @date on 2021/12/9 14:55
 */

class NotificationService : NotificationListenerService() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        messageRemoved(sbn)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) {
        super.onNotificationRemoved(sbn, rankingMap)
        messageRemoved(sbn)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap,
        reason: Int
    ) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        messageRemoved(sbn)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        getNotifyData(sbn)
        LogUtils.v("onNotificationPosted")
        if (sbn.notification.actions != null) {
            LogUtils.v("rankingMap: " + sbn.notification.actions.size)
        }
        super.onNotificationPosted(sbn, rankingMap)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        getNotifyData(sbn)
        val packageName = sbn.packageName
        LogUtils.i("packageName: $packageName")
        val notification = sbn.notification
        LogUtils.i("NLService: " + notification.tickerText)
        if (sbn.notification.actions != null) {
            LogUtils.v("NLService: " + sbn.notification.actions.size)
            for (action in sbn.notification.actions) {
                LogUtils.v("onNotificationPosted: " + action.title.toString())
                if (action.title.toString().equals("Answer", ignoreCase = true)) {
                    val intent = action.actionIntent
                    try {
                        intent.send()
                    } catch (e: CanceledException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * 来消息
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotifyData(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification
            val extras = notification.extras
            val qqIcon = notification.getLargeIcon()
            val tickerText = notification.tickerText
            val title = extras.getString(Notification.EXTRA_TITLE, "")
            val content = extras.getString(Notification.EXTRA_TEXT, "")
            LogUtils.d("packageName=$packageName, title=$title, content=$content, tickerText=$tickerText, qqIcon=$qqIcon")
            if (packageName == "com.tencent.mm") {
                send(sbn)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            e.message?.let { LogUtils.e(it) }
        }
    }

    /**
     * 删除消息
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun messageRemoved(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification
            val extras = notification.extras
            val resId = notification.icon
            val tickerText = notification.tickerText
            val title = extras.getString(Notification.EXTRA_TITLE, "")
            val content = extras.getString(Notification.EXTRA_TEXT, "")
            LogUtils.d("packageName=$packageName, title=$title, content=$content, tickerText=$tickerText")
        } catch (e: Throwable) {
            e.printStackTrace()
            e.message?.let { LogUtils.e(it) }
        }
    }

    private var pendingIntent: PendingIntent? = null
    private var key: String? = null
    private var statusBarNotification: StatusBarNotification? = null

    /**
     * 这就是发送方法，大部分都是为了打印日志，精简一下就是MainActivity里面的send方法。
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun send(sbn: StatusBarNotification) {
        LogUtils.v("send 来了一条消息")
        if (statusBarNotification == sbn) {
            LogUtils.v("相同的消息！！！")
            return
        }
        statusBarNotification = sbn
        pendingIntent = sbn.notification.contentIntent
        val text =
            sbn.notification.extras.getCharSequence(NotificationCompat.EXTRA_TEXT, "").toString()
        LogUtils.v(text)
        val notification: Notification = sbn.notification
        val mCarExtender: NotificationCompat.CarExtender =
            NotificationCompat.CarExtender(notification)

        LogUtils.v("mCarExtender=$mCarExtender")
        val carBundle: Bundle = notification.extras
        val carExtender = NotificationCompat.CarExtender()
        val title = carBundle.getString(Notification.EXTRA_TITLE, "")
        LogUtils.v("title=$title")
        try {
            if (carBundle != null) {
                try {
                    val mLargeIcon = carBundle.getParcelable<Icon>("large_icon")
                    LogUtils.d("largeIcon=$mLargeIcon")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val b2: Bundle? = carBundle.getBundle("android.car.EXTENSIONS")
                LogUtils.d("android.car.EXTENSIONS = $b2")
                val b: Bundle? = b2?.getBundle("car_conversation")
                LogUtils.d("car_conversation = $b")
                LogUtils.v("on_read= ${b?.getParcelable<PendingIntent>("on_read")}")
                val list = b2?.getParcelableArrayList<Bundle>("messages")
                LogUtils.json(list)
                val time = b2?.getInt("timestamp")
                LogUtils.v("time =$time")
                // 此处注意RemoteInput的包名。
                LogUtils.v("remote_input= ${b?.getParcelable<android.app.RemoteInput>("remote_input")}")
                LogUtils.v("on_reply= ${b?.getParcelable<PendingIntent>("on_reply")}")
                LogUtils.v("participants= ${b?.getStringArray("participants")?.get(0)}")
                try {
                    carExtender.largeIcon = carBundle.getParcelable("android.largeIcon")
                    LogUtils.d("android.largeIcon=${carExtender.largeIcon}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        LogUtils.v("原始版本：$carBundle")
        // timestamp
        carBundle.putString("KEY_NOTIFICATION_KEY", key)
        LogUtils.v("插入版本：$carBundle")
        val conversation: NotificationCompat.CarExtender.UnreadConversation =
            mCarExtender.unreadConversation!!
        val pendingReply: PendingIntent = conversation.replyPendingIntent!!
        val remoteInput: RemoteInput = conversation.remoteInput!!

        LogUtils.v("m1751 resultKey=${remoteInput.resultKey}")
        if (key == remoteInput.resultKey) {
            LogUtils.d("m1751 key=$key")
        } else {
            key = remoteInput.resultKey
        }
        val localIntent = Intent()
        val resultBundle = Bundle()
        val list3 = text.split(":")
        LogUtils.json(list3)

        resultBundle.putString(key, "你给我发送了[${list3[list3.size - 1].trim()}]，我给你的备注是= $title")
        RemoteInput.addResultsToIntent(
            arrayOf(RemoteInput.Builder(key!!).build()),
            localIntent,
            resultBundle
        )
        try {
            pendingReply.send(applicationContext, 0, localIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}