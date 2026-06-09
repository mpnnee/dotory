package com.example.dotory.data.model

import com.kakao.vectormap.LatLng

data class Dot(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val title: String,           // 장소 이름 (예: "을지로 국밥집")
    val comment: String,         // 한 줄 코멘트 (최대 100자)
    val photoUri: String,        // 로컬 이미지 URI
    val category: DotCategory,
    val createdAt: Long          // System.currentTimeMillis()
) {
    // 카카오 지도 마커 표시를 위한 편의용 확장 함수 추가
    fun toLatLng(): LatLng = LatLng.from(latitude, longitude)
}

enum class DotCategory(val label: String, val emoji: String, val colorHex: Long) {
    FOOD("음식점", "🍣", 0xFFEE8130),
    CAFE("카페", "☕️", 0xFF6F4E37),
    TRAVEL("여행", "✈️", 0xFF6390F0),
    NATURE("자연", "🌿", 0xFF7AC74C),
    ETC("기타", "📍", 0xFFA8A77A)
}
