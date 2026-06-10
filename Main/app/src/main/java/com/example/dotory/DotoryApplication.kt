package com.example.dotory

import android.app.Application
import com.example.dotory.data.local.DotDatabase
import com.example.dotory.data.repository.DotRepository
import com.kakao.vectormap.KakaoMapSdk

class DotoryApplication : Application() {
    val database by lazy { DotDatabase.getDatabase(this) }
    val repository by lazy { DotRepository(database.dotDao()) }

    override fun onCreate() {
        super.onCreate()
        // 카카오 지도 SDK v2 초기화
        KakaoMapSdk.init(this, "fa08dce1e6d3fd2a1e006c7de4d188de")
    }
}
