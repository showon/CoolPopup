
# 主要技术点

1. 打开Android auto模式；
1. 利用通知使用权限拿到Notification，调用post方法。

工程中有两个library：

- 悬浮窗是 https://github.com/princekin-f/EasyFloat/
- utils包 https://github.com/Blankj/AndroidUtilCode/

主要功能已经完成，弹窗的UI还没有，如果想任意页面都可进行消息回复，可以自行开发一个聊天UI，利用悬浮窗弹出。

### 第一步：如何用代码打开Android auto模式呢？又不能弹出提醒被用户感知到。

```kotlin
fun open(b: Boolean) {
        // EXTENSIONS
        val uiManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (b) {
            uiManager.enableCarMode(UiModeManager.MODE_NIGHT_AUTO)
            Toast.makeText(this, "开启了 ${uiManager.currentModeType}", Toast.LENGTH_SHORT).show()
        } else {
            uiManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME)
            Toast.makeText(this, "关闭了", Toast.LENGTH_SHORT).show()
        }
        Handler(Looper.getMainLooper())
            .postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
            }, 1000)
        finish()
    }
```

就是这么简单就可以无声无息打开Android auto模式；

但是有个前提，很多国产手机都阉割了，并没有安装Android Auto包。也不要紧，只要自己做一个空包安装就可以，开启驾驶模式只检测有无包名。


> Android Auto的包名：applicationId "com.google.android.projection.gearhead"
> 做一个空包，桌面没有图标。


比如市场上有一些悬浮菜单、女娲石之类的app，好几十块钱开一个月会员，才能使用这个功能。其实就没几行代码。


### 第二步：如何知道微信来消息了。




```kotlin
class NotificationService : NotificationListenerService() {
	//	继承这个类就可以，开启通知使用权限。
}
```

```xml
 <!--通知栏权限-->
        <service
            android:name=".NotificationService"
            android:exported="true"
            android:label="@string/app_name"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
```

```kotlin
/**
这是接收消息的回掉，这里可以处理所有的消息。
*/
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

```

我们能够拿到通知消息，能够使用，也能够取消关闭它，所以自己还可以做一个消息样式。
举例说明：某app来了一条消息，你取消它，拿到消息体和intent，自己迅速弹出来一个消息。此消息的花样可以自己定义，比如各种背景花色，各种提示音。
![image](https://upload-images.jianshu.io/upload_images/4163716-b558c9b3505cd683.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/300)

![image](https://upload-images.jianshu.io/upload_images/4163716-2444eaff944a1ffb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/300)


### 第三步：监听到了消息，如何发送一个文本出去呢？

```kotlin
 	/**
     * 自动回复微信消息的方法。可以使用在任意位置。
     */
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
        resultBundle.putString(key, "你给我发送了[${list3[list3.size - 1].trim()}]，我给你的备注是= $title")
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

```

这样就可以搞定了。。
- 比如正在看小说的时候，弹出一个微信聊天窗口，输入消息发送出去。

- 比如正在购物的时候，来消息了，弹出一个微信窗口，输入消息发送出去。

只要你想象力够丰富，可以用在任意场景。
- 如果在任意位置弹窗，需要再做一个悬浮窗。
- 当前也可以设置一条自动回复消息，有人给你发消息，就自动回复：机主正在忙，上后回复……


还可以做各种炫酷的通知样式，再也不需要为了这点小小的功能去花几十大洋了。。

![image](https://upload-images.jianshu.io/upload_images/4163716-79ff7f3559effa73.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/300)


