package io.torder.overlayview

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import service.UndeadService

// 안드로이드 8.1 부터 background service 제약사항이 생기고
// foregroundserveice의 경우에는 노티피케이션에 항상 표시되어야 하는 제약이 생김
// 본프로젝트는 foreground service 제약을 지키며 죽지 않는 서비스를 구동시킴

class MainActivity : AppCompatActivity() {

    var foregroundServiceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun mStart(view: View) { // 시작하기를 누르면

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 설정에서 오버레이 허용여부확인하고
            if(Settings.canDrawOverlays(this)) {
                startUndeadService()
            } else {
                // 허용하지 않을시 설정페이지로 이동
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 1)
            }
        } else {
            // 안드로이드 M미만에서는 매니페스트에 퍼미션만 넣으면 되므로 별도 처리 필요하지 않음
            startUndeadService()
        }
    }

    // 죽지않는 서비스 실행
    private fun startUndeadService() {
        if (null == UndeadService.serviceIntent) {
            // 서비스를 시작시킴
            foregroundServiceIntent = Intent(this, UndeadService::class.java)
            startService(foregroundServiceIntent)
            Toast.makeText(
                this,
                "start service | ${foregroundServiceIntent == null} ${UndeadService.serviceIntent == null}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // 서비스가 켜져있으면 인텐트만 새로 연결함
            foregroundServiceIntent = UndeadService.serviceIntent
            Toast.makeText(this, "already", Toast.LENGTH_SHORT).show()
        }
    }

    fun mStop(view: View) {  // 중지하기를 누르면
        if (null != foregroundServiceIntent) {
            // 서비스가 돌고있으면 멈추게함
            stopService(foregroundServiceIntent)
            foregroundServiceIntent = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != foregroundServiceIntent) {
            stopService(foregroundServiceIntent)
            foregroundServiceIntent = null
        }
    }
}
