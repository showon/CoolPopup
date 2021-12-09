package com.china.coolpopup

import android.app.Notification
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.blankj.utilcode.util.LogUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun start(view: View) {
        // 我们需要通知使用权限，类似于运动App转发给手环，完全一样。用来读取微信的消息。当然可以用来读取任意通知。
        toggleNotificationListenerService(this)
        if (!isNotificationListenersEnabled()) {
            gotoNotificationAccessSetting()
        }
    }

    fun openPermission(view: View) {
        if (Build.VERSION.SDK_INT >= 23) {//6.0以上
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            } catch (e:Exception) {
                e.printStackTrace();
            }
        }
    }

    fun mo23397(z: Boolean) {
        // EXTENSIONS
        val uiManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (z) {
            uiManager.enableCarMode(UiModeManager.MODE_NIGHT_AUTO)
            Toast.makeText(this, "开启了 ${uiManager.currentModeType}", Toast.LENGTH_SHORT).show()
//                uiManager.nightMode = UiModeManager.MODE_NIGHT_YES
        } else {
            uiManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME)
            Toast.makeText(this, "关闭了", Toast.LENGTH_SHORT).show()
//                uiManager.nightMode = UiModeManager.MODE_NIGHT_NO
        }
        Handler(Looper.getMainLooper())
            .postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
            }, 1000)
        finish()
    }

    //=========================================================


    /**
     * 先 disable 再 enable ，重新触发系统的 rebind 操作
     */
    private fun toggleNotificationListenerService(context: Context) {
        val pm: PackageManager = context.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(context, NotificationService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(context, NotificationService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * 转到通知访问设置
     */
    private fun gotoNotificationAccessSetting(): Boolean {
        return try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (e: Exception) {
            //普通情况下找不到的时候需要再特殊处理找一次
            try {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val cn = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$NotificationAccessSettingsActivity"
                )
                intent.component = cn
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                startActivity(intent)
                return true
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
            Toast.makeText(this, "对不起，您的手机暂不支持", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            false
        }
    }

    /**
     * 是否启用通知侦听器
     */
    private fun isNotificationListenersEnabled(): Boolean {
        val pkgName = packageName
        val flat: String = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        LogUtils.v("-----flat--------> $flat")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }


    /**
     * 自动回复微信消息的方法。可以使用在任意位置。
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun send(sbn: StatusBarNotification) {
        val notification: Notification = sbn.notification
        val mCarExtender: NotificationCompat.CarExtender =
            NotificationCompat.CarExtender(notification)
        val conversation: NotificationCompat.CarExtender.UnreadConversation =
            mCarExtender.unreadConversation!!
        val pendingReply: PendingIntent = conversation.replyPendingIntent!!
        val remoteInput: RemoteInput = conversation.remoteInput!!
        val key = remoteInput.resultKey
        val localIntent = Intent()
        val resultBundle = Bundle()
        resultBundle.putString(key, "自动回复的内容[$]")
        RemoteInput.addResultsToIntent(
            arrayOf(RemoteInput.Builder(key).build()),
            localIntent,
            resultBundle
        )
        try {
            pendingReply.send(this@MainActivity, 0, localIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}