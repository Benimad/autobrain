package com.example.autobrain.core.utils

object Constants {
    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val SERVICE_PROVIDERS_COLLECTION = "service_providers"
    const val CONVERSATIONS_COLLECTION = "conversations"
    const val MESSAGES_COLLECTION = "messages"
    const val BOOKINGS_COLLECTION = "bookings"
    const val BREAKDOWN_REQUESTS_COLLECTION = "breakdown_requests"
    const val CAR_LOGS_COLLECTION = "car_logs"
    const val MAINTENANCE_RECORDS_COLLECTION = "maintenance_records"
    const val DIAGNOSTICS_COLLECTION = "diagnostics"

    // Storage Paths
    const val PROFILE_IMAGES_PATH = "profile_images"
    const val MESSAGE_IMAGES_PATH = "message_images"
    const val CAR_IMAGES_PATH = "car_images"
    const val DIAGNOSTIC_AUDIO_PATH = "diagnostic_audio"
    const val DIAGNOSTIC_VIDEO_PATH = "diagnostic_video"
    const val DOCUMENT_PATH = "documents"

    // Preferences Keys
    const val PREFERENCES_NAME = "autobrain_preferences"
    const val KEY_USER_ID = "user_id"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_FCM_TOKEN = "fcm_token"

    // Notification Channels
    const val CHANNEL_ID_MESSAGES = "messages_channel"
    const val CHANNEL_ID_BOOKINGS = "bookings_channel"
    const val CHANNEL_ID_REMINDERS = "reminders_channel"
    const val CHANNEL_ID_BREAKDOWN = "breakdown_channel"

    // WorkManager Tags
    const val WORK_TAG_SYNC = "sync_work"
    const val WORK_TAG_REMINDERS = "reminders_work"
    const val WORK_TAG_CLEANUP = "cleanup_work"

    // API & ML Constants
    const val MAX_AUDIO_DURATION_SECONDS = 30
    const val MAX_VIDEO_DURATION_SECONDS = 60
    const val MAX_IMAGE_SIZE_MB = 5
    const val SEARCH_RADIUS_KM = 50.0
    const val DEFAULT_SEARCH_RADIUS_KM = 50.0
    const val BREAKDOWN_RESPONSE_TIME_MINUTES = 20

    // Age Restrictions
    const val MINIMUM_USER_AGE = 18

    // Supported Cities
    val SUPPORTED_CITIES = listOf(
        "Casablanca", "Rabat", "Fès", "Marrakech", "Agadir",
        "Tanger", "Meknès", "Oujda", "Kénitra", "Tétouan",
        "Safi", "Temara", "Mohammedia", "Khouribga", "El Jadida",
        "Béni Mellal", "Nador", "Taza", "Settat", "Khémisset"
    )
}
