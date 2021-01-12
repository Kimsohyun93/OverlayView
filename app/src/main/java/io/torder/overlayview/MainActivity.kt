package io.torder.overlayview

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import service.UndeadService

// 안드로이드 8.1 부터 background service 제약사항이 생기고
// foregroundserveice의 경우에는 노티피케이션에 항상 표시되어야 하는 제약이 생김
// 본프로젝트는 foreground service 제약을 지키며 죽지 않는 서비스를 구동시킴

class MainActivity : AppCompatActivity() {

    private lateinit var mService: UndeadService
    private var mBound: Boolean = false
    var foregroundServiceIntent: Intent? = null

    lateinit var settings: WebSettings
    lateinit var webClient: WebChromeClient
    val webUrl = "http://api.inventory.torder.co.kr/callOtherAppTest.html"

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as UndeadService.UndeadBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setWebview()
    }

    public fun testCallOtherApp() {
        Toast.makeText(this, "id", Toast.LENGTH_SHORT).show()
    }

//    private fun setWebview() {
//        settings = wv.settings
//        settings.javaScriptEnabled = true
//        settings.domStorageEnabled = true // 로컬 스토리지 등 브라우저 저장소 활성화
//        settings.cacheMode = WebSettings.LOAD_DEFAULT // 캐시 활성화
//        settings.setAppCacheEnabled(true)
//        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
//
//        wv.setLayerType(View.LAYER_TYPE_HARDWARE, null) // 하드웨어 가속 활성
//        wv.setWebChromeClient(WebChromeClient())
//        wv.addJavascriptInterface(WebBridge(this), "Android")
//        Log.e("setWebViewSettings", webUrl)
//        wv.loadUrl(webUrl)
//    }


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

    override fun onResume() {
        super.onResume()
        Intent(this, UndeadService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        if (mBound) {
            mService.hideIconView()
        }
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show()
        if (mBound) {
            mService.showIconView()
            mService.startMainActivity()
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