package com.example.ma2025.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.ma2025.R;
import com.example.ma2025.activity.AllianceInvitationActivity;
import com.example.ma2025.model.AllianceInvitation;

public class NotificationHelper {

    private static final String CHANNEL_ID = "alliance_invitations";
    private static final String CHANNEL_NAME = "Pozivi za savez";
    private static final String CHANNEL_DESC = "Notifikacije za pozive u savez";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showInvitationNotification(AllianceInvitation invitation) {
        Intent intent = new Intent(context, AllianceInvitationActivity.class);
        intent.putExtra("invitation_id", invitation.getId());
        intent.putExtra("alliance_id", invitation.getAllianceId());
        intent.putExtra("alliance_name", invitation.getAllianceName());
        intent.putExtra("from_username", invitation.getFromUsername());
        intent.putExtra("from_email", invitation.getFromUserEmail());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                invitation.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Poziv u savez")
                .setContentText(invitation.getFromUsername() + " te je pozvao/la u savez: " + invitation.getAllianceName())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false) // Ne može se skloniti dok se ne prihvati/odbije
                .setOngoing(true) // Sticky notifikacija
                .setContentIntent(pendingIntent);

        notificationManager.notify(invitation.getId().hashCode(), builder.build());
    }

    public void cancelInvitationNotification(String invitationId) {
        notificationManager.cancel(invitationId.hashCode());
    }

    public void showAcceptedNotification(String username, String allianceName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Alliance Invitation Accepted")
                .setContentText(username + " has accepted your invitation to join the alliance: " + allianceName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Korisnik može obrisati notifikaciju prevlačenjem
                .setOngoing(false);  // Nije "sticky" — može se skloniti ručno

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

}