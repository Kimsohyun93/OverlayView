# Overlay view

**kotlin**

+ 최상위에 항상 떠있는 뷰(아이콘)를 생성
+ 드래그앤드롭으로 이동가능
+ 클릭시 액션(현재는 티오더-오더해앱을 띄우도록 동작중)
+ 안드로이드 8.1 부터 background service 제약사항이 생김(지속불가능하고 죽을 수 있음)
+ foregroundserveice의 경우에는 노티피케이션에 항상 표시되어야 함
+ 본프로젝트는 foreground service 제약을 지키며 죽지 않는 서비스를 구동시킴



**Version Control**

1. 오버레이관련

+ Under M - 제약 없음. 매니페스트 퍼미션만 확인

+ Else - 설정에서 오버레이 동의가 필요함

  ```kotlin
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
  ```



2. 서비스 구동

+ Under O - startService로 제어

+ Over O - startForegroundService로 구동

  ```kotlin
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      var intent = Intent(context, UndeadService::class.java)
      context?.startForegroundService(intent)
  } else {
      var intent = Intent(context, UndeadService::class.java)
      context?.startService(intent)
  }
  ```



3. 노티피케이션 채널

+ Under O - 채널설정없음

+ Over O - 노티피케이션 채널설정권장~필수

  ```kotlin
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
  ```



**MainActivtiy**

+ 시작하기와 중지하기 버튼만 존재함
+ 시작하기를 누르면 서비스를 구동시킴



**UndeadService**

+ Foreground Service
+ 오버레이 뷰 생성, 드래그앤드롭 이동, 클릭이벤트 존재
+ 종료가 호출되는 모든 경우에 Alarm Revicer를 구동함



**AlarmReceiver**

+ Broadcast Receiver
+ UndeadService 가 종료되는 경우 AlarmReceiver를 불러서 3초후 가동하도록 함
+ AlarmReceiver는 3초후 UndeadService를 다시 구동시켜서 죽지 않도록 함 
