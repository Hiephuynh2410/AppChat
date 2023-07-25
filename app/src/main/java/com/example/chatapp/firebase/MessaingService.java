//package com.example.chatapp.firebase;
//
//import android.Manifest;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.ActivityCompat;
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//
//import com.example.chatapp.R;
//import com.example.chatapp.activities.chatActivity;
//import com.example.chatapp.models.User;
//import com.example.chatapp.utilities.constant;
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//
//import java.util.Random;
//
//public class MessaingService extends FirebaseMessagingService {
//    @Override
//    public void onNewToken(@NonNull String token) {
//        super.onNewToken(token);
//    }
//
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage message) {
//        super.onMessageReceived(message);
////        User user = new User();
////        user.id = message.getData().get(constant.KEY_USER_ID);
////        user.name = message.getData().get(constant.KEY_NAME);
////        user.token = message.getData().get(constant.KEY_FCM_TOKEN);
////
////        int notifiid = new Random().nextInt();
////        String channelId = "Chat_mess";
////
////        Intent intent = new Intent(this, chatActivity.class);
////        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////        intent.putExtra(constant.KEY_USER, user);
////        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
////
////        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
////        builder.setSmallIcon(R.drawable.round_notifications_24);
////        builder.setContentTitle(user.name);
////        builder.setContentText(message.getData().get(constant.KEY_MESSAGE));
////        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
////                message.getData().get(constant.KEY_MESSAGE)
////        ));
////        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
////        builder.setContentIntent(pendingIntent);
////        builder.setAutoCancel(true);
////
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            CharSequence name = "ChatApp";
////            String description = "Chat Messages";
////            int importance = NotificationManager.IMPORTANCE_DEFAULT;
////            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
////            channel.setDescription(description);
////            NotificationManager notificationManager = getSystemService(NotificationManager.class);
////            notificationManager.createNotificationChannel(channel);
////        }
////        // Define a unique notification ID for each notification
////        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
////        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
////            return;
////        }
////        notificationManagerCompat.notify(notifiid, builder.build());
//    }
//
//}
//
