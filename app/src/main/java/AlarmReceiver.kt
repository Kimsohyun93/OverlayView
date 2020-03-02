import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import service.UndeadService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var intent = Intent(context, UndeadService::class.java)
            context?.startForegroundService(intent)
        } else {
            var intent = Intent(context, UndeadService::class.java)
            context?.startService(intent)
        }
    }
}
