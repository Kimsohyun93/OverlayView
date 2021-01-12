package service

import AlarmReceiver
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import io.torder.overlayview.MainActivity
import io.torder.overlayview.R
import io.torder.overlayview.SecActivity
import kotlinx.android.synthetic.main.view_overlay.view.*
import java.lang.Exception
import java.util.*


class UndeadService : Service() {

    lateinit var mView: View
    // 드래그앤드롭으로 위젯이동 구현
    var mTouchX: Float = 0f
    var mTouchY: Float = 0f
    var mViewX: Int = 0
    var mViewY: Int = 0

    companion object {
        // 스태틱 변수
        var serviceIntent: Intent? = null
    }

    // Binder given to clients
    private val binder = UndeadBinder()

    // Random number generator
    private val mGenerator = Random()

    /** method for clients  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    inner class UndeadBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): UndeadService = this@UndeadService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // 오버레이 뷰(아이콘) 생성
        var mInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mView = mInflater.inflate(R.layout.view_overlay, null)

        var layoutFlag: Int = 0

        // 8.1 이상에서는 TYPE APPLICATION OVERLAY 로 사용하고
        // 그 이전 버전에서는 TYPE_PHONE 을 사용함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        var expendable = true //펼쳐진 상태
        mView.overlayHeader.setOnClickListener {
            if(expendable){
                mView.overlayContents.visibility = GONE
                expendable = false
            }else{
                mView.overlayContents.visibility = VISIBLE
                expendable = true
            }
        }


        // 크기, 범위, 배경등 지정
        var mParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,

            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // 윈도우 매니저(최상위 화면)에 오버레이뷰를 추가함
        var mManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mManager.addView(mView, mParams)

        mView.transView.setOnClickListener {
            var intent = Intent(this, SecActivity::class.java)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent!!.addCategory(Intent.CATEGORY_LAUNCHER)
            startActivity(intent)
            mView.overlayContents.visibility = GONE

        }

        mView.closeBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("타이틀")
            builder.setPositiveButton("확인"){ dialogInterface: DialogInterface, i: Int ->

            }

        }
//
//        mView.setOnClickListener {
//            try {
//                var intent = packageManager.getLaunchIntentForPackage("com.torder.orderhae")
//                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent!!.addCategory(Intent.CATEGORY_LAUNCHER)
//                startActivity(intent)
//            } catch (e: Exception) {
//                Log.e("mView.onclick", e.toString())
//            }
//        }

        // 뷰 이동
//        mView.setOnTouchListener(object : View.OnTouchListener {
//            @SuppressLint("NewApi")
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                when (event?.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        mTouchX = event.rawX
//                        mTouchY = event.rawY
//                        mViewX = mParams.x
//                        mViewY = mParams.y
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        var x = (event.rawX - mTouchX).toInt()
//                        var y = (event.rawY - mTouchY).toInt()
//
//                        mParams.x = mViewX + x;
//                        mParams.y = mViewY + y;
//
//                        mManager.updateViewLayout(mView, mParams)
//                    }
//                }
//                return v?.onTouchEvent(event) ?: true
//            }
//        })
    }

    // 포그라운드 서비스 시작시 노티피케이션 생성 및 제어
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceIntent = intent
        initializeNotification()
        return START_STICKY
    }

    // 노티피케이션 이니셜라이즈
    private fun initializeNotification() {
        var builder = NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.mipmap.ic_launcher)

        var bigStyle = NotificationCompat.BigTextStyle();
        bigStyle.bigText("설정을 보려면 누르세요")
        bigStyle.setBigContentTitle(null)
        bigStyle.setSummaryText("서비스 동작중")

        builder.setContentText(null)
        builder.setContentTitle(null)

        // 노티피케이션에서 스와이프로 제거 불가능
        builder.setOngoing(true)

        builder.setStyle(bigStyle)
        builder.setWhen(0)
        builder.setShowWhen(false)

        var notificationIntent = Intent(this, MainActivity::class.java)
        var pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        builder.setContentIntent(pendingIntent)
        var manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 8.0 이상에서 노티피케이션 채널 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    "1",
                    "undead_service",
                    NotificationManager.IMPORTANCE_NONE
                )
            )
        }

        var notification = builder.build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()

        // 강제로 종료시에도, 3초 후 재가동하도록 알람매니저 등록
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 3)
        var intent = Intent(this, AlarmReceiver::class.java)
        var sender = PendingIntent.getBroadcast(this, 0, intent, 0)

        var alramManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alramManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // 어떤 원인으로 서비스가 꺼지는 경우, 3초 후 재가동하도록 알람매니저 등록
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 3)
        var intent = Intent(this, AlarmReceiver::class.java)
        var sender = PendingIntent.getBroadcast(this, 0, intent, 0)

        var alramManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alramManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
    }

    fun showIconView() {
        try {
            mView.visibility = VISIBLE
        } catch (e: Exception) {

        }
    }

    fun hideIconView() {
        try {
            mView.visibility = GONE
        } catch (e: Exception) {

        }
    }

    fun startMainActivity() {
        var intent = Intent(this, MainActivity::class.java)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}