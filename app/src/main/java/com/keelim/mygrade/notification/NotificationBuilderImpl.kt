package com.keelim.mygrade.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.keelim.mygrade.R
import com.keelim.mygrade.ui.center.CenterActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class NotificationBuilderImpl @Inject constructor(
    @ApplicationContext val ctx: Context
) : NotificationBuilder {

    override fun showNotification(action: NotificationCompat.Action?) = ctx.run {
        NotificationSpecs.notifyLegacy(this) {
            setStyle(NotificationCompat.BigTextStyle())
            setSmallIcon(R.mipmap.ic_launcher_round)
            setContentTitle(buildSpannedString { bold { append("성적을 확인해보아요") } })
            setContentText("안녕하세요 마이그레이드 입니다.")
            setAutoCancel(true)
            action?.let {
                addAction(it)
            }
            setContentIntent(createLauncherIntent())
        }
    }

    override fun remoteNotification() {
        val remoteInput = RemoteInput.Builder(CenterActivity.KEY_TEXT_REPLY)
            .setLabel("Enter your reply here")
            .build()

        val replyAction = NotificationCompat.Action.Builder(
            IconCompat.createWithResource(
                ctx,
                android.R.drawable.ic_dialog_info
            ),
            "Reply", PendingIntent.getActivity(
                ctx,
                0,
                Intent(ctx, CenterActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        )
            .addRemoteInput(remoteInput)
            .build()

        showNotification(replyAction)
    }

    private fun Context.createLauncherIntent(): PendingIntent {
        val intent = Intent(this, CenterActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
    }
}