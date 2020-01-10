/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.luxoft.supplychain.sovrinagentapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.NOTIFICATION_CHANNEL_ID
import com.luxoft.supplychain.sovrinagentapp.application.NOTIFICATION_CHANNEL_NAME

fun showNotification(context: Context, title: String, message: String) {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.something)

    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(true)
        .setLargeIcon(bitmap)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setContentTitle(title)
        .setContentText(message)
        .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

        builder.setSmallIcon(R.drawable.ic_notification)
        builder.color = ContextCompat.getColor(context, R.color.colorPrimary)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    notificationManager?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance)
            notificationChannel.enableVibration(true)
            builder.setChannelId(NOTIFICATION_CHANNEL_ID)

            it.createNotificationChannel(notificationChannel)
        }

        it.notify(100, builder.build())
    }
}