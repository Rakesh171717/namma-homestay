package com.example.namma_homestay

enum class UserRole {
    HOST, TRAVELER, NONE
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.NONE
)
data class HomestayProfile(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val phoneNumber: String = "",
    val pricePerNight: Double = 1800.0,
    val photos: List<String> = emptyList(), // Use String for URLs instead of Int for resources
    val roomPhotos: List<String> = emptyList(),        // URIs / URLs for bedroom photos
    val livingAreaPhotos: List<String> = emptyList(),  // URIs / URLs for living area photos
    val checklist: Map<String, Boolean> = mapOf(
        "Cleanliness Verified" to false,
        "Essentials Provided" to false,
        "WiFi" to false,
        "Safety Guidelines" to false
    )
)
// class menu item
data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val isAvailable: Boolean = true,
    val imageUrl: String? = null
)

data class ChatMessage(
    val id: String = "",
    val sender: String = "", // "host" or "guest"
    val text: String = "",
    val timestamp: Long = 0L
)

data class Inquiry(
    val id: String = "",
    val guestName: String = "",
    val message: String = "",
    val dates: String = "",
    val phoneNumber: String = "",
    val messages: List<ChatMessage> = emptyList()
)

data class GuideSpot(
    val id: String = "",
    val name: String = "",
    val distance: String = "",
    val description: String = "",
    val imageUrl: String? = null
)

data class DashboardStats(
    val unreadInquiries: Int = 0,
    val activeMenuItems: Int = 0,
    val profileCompletion: Int = 0
)

data class Booking(
    val id: String = "",
    val guestName: String = "",
    val dates: String = "",
    val isConfirmed: Boolean = false
)

data class CalendarDay(
    val dateString: String = "", // e.g. "2026-05-15"
    val isAvailable: Boolean = true,
    val price: Double = 0.0
)

// We keep some mock data for preview/initial fallback if Firebase is not yet connected
object MockData {
    private const val HERO_IMAGE = "android.resource://com.example.namma_homestay/drawable/hero_homestay"
    private const val FOOD_IMAGE = "android.resource://com.example.namma_homestay/drawable/food_dosa"
    private const val GUIDE_IMAGE = "android.resource://com.example.namma_homestay/drawable/guide_waterfall"

    val profile = HomestayProfile(
        id = "profile-main",
        name = "Kaveri River Homestay",
        location = "Coorg, Karnataka",
        description = "A beautiful homestay situated near the Kaveri river. Experience local coffee and spices.",
        phoneNumber = "+919876543210",
        pricePerNight = 1800.0,
        photos = listOf(HERO_IMAGE),
        roomPhotos = listOf(HERO_IMAGE),
        livingAreaPhotos = listOf(HERO_IMAGE),
        checklist = mapOf(
            "Cleanliness Verified" to true,
            "Essentials Provided" to true,
            "WiFi" to false,
            "Safety Guidelines" to true
        )
    )

    val menuItems = listOf(
        MenuItem("1", "Akki Rotti & Curry", "Traditional rice flatbread with spicy bamboo shoot curry.", 150.0, true, FOOD_IMAGE),
        MenuItem("2", "Neer Dosa", "Soft rice crepes served with coconut chutney and chicken gassi.", 200.0, true, FOOD_IMAGE),
        MenuItem("3", "Filter Coffee", "Freshly brewed local estate coffee.", 50.0, true, FOOD_IMAGE)
    )

    val inquiries = listOf(
        Inquiry("1", "Rahul Sharma", "Hi, is the homestay available next weekend? We are a family of 4.", "Oct 12 - Oct 14", "+919876543210", 
            messages = listOf(
                ChatMessage("m1", "guest", "Hi, is the homestay available next weekend? We are a family of 4.", System.currentTimeMillis() - 86400000),
                ChatMessage("m2", "host", "Yes, it is available! Let me know if you need any details.", System.currentTimeMillis() - 3600000)
            )
        ),
        Inquiry("2", "Anita Desai", "Do you serve vegetarian meals exclusively?", "Nov 1 - Nov 3", "+919876543211",
            messages = listOf(
                ChatMessage("m3", "guest", "Do you serve vegetarian meals exclusively?", System.currentTimeMillis() - 7200000)
            )
        )
    )

    val stats = DashboardStats(
        unreadInquiries = 1,
        activeMenuItems = 3,
        profileCompletion = 85
    )

    val upcomingBookings = listOf(
        Booking("b1", "Kiran Kumar", "Oct 15 - Oct 18", true),
        Booking("b2", "Suresh Menon", "Oct 20 - Oct 22", true)
    )

    val calendarDays = (1..30).associate { day ->
        val dateString = "2026-06-${day.toString().padStart(2, '0')}"
        dateString to CalendarDay(dateString, isAvailable = day % 5 != 0, price = if (day % 7 == 0 || day % 7 == 6) 2500.0 else 1800.0)
    }

    val guideSpots = listOf(
        GuideSpot("1", "Abbey Falls", "8 km away", "A stunning waterfall surrounded by coffee plantations.", GUIDE_IMAGE),
        GuideSpot("2", "Raja's Seat", "5 km away", "Beautiful sunset viewpoint with a garden.", GUIDE_IMAGE),
        GuideSpot("3", "Dubare Elephant Camp", "15 km away", "Interact with elephants by the riverbank.", GUIDE_IMAGE)
    )

    // Mock data for Traveler Feed
    val mockHomestays = listOf(
        HomestayProfile(
            id = "profile-main",
            name = "Kaveri River Homestay",
            location = "Coorg, Karnataka",
            description = "A beautiful homestay situated near the Kaveri river. Experience local coffee and spices.",
            phoneNumber = "+919876543210",
            pricePerNight = 1800.0,
            photos = listOf(HERO_IMAGE),
            checklist = profile.checklist
        ),
        HomestayProfile(
            id = "profile-western-ghats",
            name = "Western Ghats Eco Stay",
            location = "Chikmagalur, Karnataka",
            description = "Stay amidst the lush green hills and wake up to the smell of fresh coffee.",
            phoneNumber = "+919876543211",
            pricePerNight = 2200.0,
            photos = listOf(HERO_IMAGE),
            checklist = profile.checklist
        ),
        HomestayProfile(
            id = "profile-heritage-farm",
            name = "Heritage Farm House",
            location = "Wayanad, Kerala",
            description = "Experience traditional agriculture and authentic home-cooked meals.",
            phoneNumber = "+919876543212",
            pricePerNight = 2000.0,
            photos = listOf(HERO_IMAGE),
            checklist = profile.checklist
        )
    )
}
