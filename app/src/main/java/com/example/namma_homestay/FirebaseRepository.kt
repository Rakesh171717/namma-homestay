package com.example.namma_homestay

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth = runCatching { Firebase.auth }.getOrNull()
    private val firestore = runCatching { Firebase.firestore }.getOrNull()

    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth?.currentUser ?: return null
        return fetchUser(firebaseUser.uid) ?: User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            role = UserRole.NONE
        )
    }

    suspend fun signIn(email: String, password: String): User {
        val firebaseUser = auth
            ?.signInWithEmailAndPassword(email.trim(), password)
            ?.await()
            ?.user
            ?: error("Firebase Authentication is not configured.")

        return fetchUser(firebaseUser.uid) ?: User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            role = UserRole.TRAVELER
        )
    }

    suspend fun signUp(name: String, email: String, password: String, role: UserRole): User {
        val firebaseUser = auth
            ?.createUserWithEmailAndPassword(email.trim(), password)
            ?.await()
            ?.user
            ?: error("Firebase Authentication is not configured.")

        val user = User(
            id = firebaseUser.uid,
            name = name.trim(),
            email = email.trim(),
            role = role
        )
        saveUser(user)
        if (role == UserRole.HOST) {
            ensureHostProfile(user)
        }
        return user
    }

    fun signOut() {
        auth?.signOut()
    }

    private suspend fun fetchUser(userId: String): User? {
        return runCatching {
            firestore
                ?.collection(USERS)
                ?.document(userId)
                ?.get()
                ?.await()
                ?.toObject(User::class.java)
                ?.copy(id = userId)
        }.getOrNull()
    }

    private suspend fun saveUser(user: User) {
        runCatching {
            firestore
                ?.collection(USERS)
                ?.document(user.id)
                ?.set(user)
                ?.await()
        }
    }

    private suspend fun ensureHostProfile(user: User) {
        val profile = MockData.profile.copy(
            id = user.id,
            name = if (user.name.isNotBlank()) "${user.name}'s Homestay" else MockData.profile.name
        )
        runCatching {
            firestore
                ?.collection(PROFILES)
                ?.document(MAIN_PROFILE)
                ?.set(profile)
                ?.await()
        }
    }

    suspend fun fetchProfile(): HomestayProfile {
        return runCatching {
            firestore
                ?.collection(PROFILES)
                ?.document(MAIN_PROFILE)
                ?.get()
                ?.await()
                ?.toObject(HomestayProfile::class.java)
        }.getOrNull() ?: MockData.profile
    }

    suspend fun fetchHomestays(): List<HomestayProfile> {
        return runCatching {
            firestore
                ?.collection(PROFILES)
                ?.get()
                ?.await()
                ?.documents
                ?.mapNotNull { document ->
                    document.toObject(HomestayProfile::class.java)?.copy(id = document.id)
                }
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull() ?: MockData.mockHomestays
    }

    suspend fun updateChecklist(checklist: Map<String, Boolean>) {
        runCatching {
            firestore
                ?.collection(PROFILES)
                ?.document(MAIN_PROFILE)
                ?.update("checklist", checklist)
                ?.await()
        }
    }

    suspend fun updateProfilePhotos(roomPhotos: List<String>, livingAreaPhotos: List<String>) {
        runCatching {
            firestore
                ?.collection(PROFILES)
                ?.document(MAIN_PROFILE)
                ?.update(
                    mapOf(
                        "roomPhotos" to roomPhotos,
                        "livingAreaPhotos" to livingAreaPhotos,
                        "photos" to (roomPhotos + livingAreaPhotos).distinct()
                    )
                )
                ?.await()
        }
    }

    suspend fun fetchMenuItems(): List<MenuItem> {
        return runCatching {
            firestore
                ?.collection(MENU)
                ?.get()
                ?.await()
                ?.documents
                ?.mapNotNull { document ->
                    document.toObject(MenuItem::class.java)?.copy(id = document.id)
                }
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull() ?: MockData.menuItems
    }

    suspend fun saveMenuItem(item: MenuItem) {
        runCatching {
            firestore
                ?.collection(MENU)
                ?.document(item.id.ifBlank { System.currentTimeMillis().toString() })
                ?.set(item)
                ?.await()
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        return uri.toString()
    }

    suspend fun fetchInquiries(): List<Inquiry> {
        return runCatching {
            firestore
                ?.collection(INQUIRIES)
                ?.get()
                ?.await()
                ?.documents
                ?.mapNotNull { document ->
                    document.toObject(Inquiry::class.java)?.copy(id = document.id)
                }
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull() ?: MockData.inquiries
    }

    suspend fun saveInquiryMessages(inquiryId: String, messages: List<ChatMessage>) {
        runCatching {
            firestore
                ?.collection(INQUIRIES)
                ?.document(inquiryId)
                ?.update("messages", messages)
                ?.await()
        }
    }

    suspend fun fetchGuideSpots(): List<GuideSpot> {
        return runCatching {
            firestore
                ?.collection(GUIDES)
                ?.get()
                ?.await()
                ?.documents
                ?.mapNotNull { document ->
                    document.toObject(GuideSpot::class.java)?.copy(id = document.id)
                }
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull() ?: MockData.guideSpots
    }

    suspend fun saveGuideSpot(spot: GuideSpot) {
        runCatching {
            firestore
                ?.collection(GUIDES)
                ?.document(spot.id.ifBlank { System.currentTimeMillis().toString() })
                ?.set(spot)
                ?.await()
        }
    }

    suspend fun fetchDashboardStats(): DashboardStats {
        return runCatching {
            val inquiries = fetchInquiries()
            val menuItems = fetchMenuItems()
            val profile = fetchProfile()
            DashboardStats(
                unreadInquiries = inquiries.size,
                activeMenuItems = menuItems.count { it.isAvailable },
                profileCompletion = calculateProfileCompletion(profile)
            )
        }.getOrDefault(MockData.stats)
    }

    suspend fun fetchBookings(): List<Booking> {
        return runCatching {
            firestore
                ?.collection(BOOKINGS)
                ?.get()
                ?.await()
                ?.documents
                ?.mapNotNull { document ->
                    document.toObject(Booking::class.java)?.copy(id = document.id)
                }
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull() ?: MockData.upcomingBookings
    }

    suspend fun fetchCalendarDays(): Map<String, CalendarDay> {
        return runCatching {
            firestore
                ?.collection(CALENDAR)
                ?.get()
                ?.await()
                ?.documents
                ?.mapNotNull { document ->
                    document.toObject(CalendarDay::class.java)?.let { document.id to it.copy(dateString = document.id) }
                }
                ?.toMap()
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull() ?: MockData.calendarDays
    }

    suspend fun saveCalendarDay(day: CalendarDay) {
        runCatching {
            firestore
                ?.collection(CALENDAR)
                ?.document(day.dateString)
                ?.set(day)
                ?.await()
        }
    }

    private fun calculateProfileCompletion(profile: HomestayProfile): Int {
        val checks = listOf(
            profile.name.isNotBlank(),
            profile.location.isNotBlank(),
            profile.description.isNotBlank(),
            profile.phoneNumber.isNotBlank(),
            profile.photos.isNotEmpty() || profile.roomPhotos.isNotEmpty() || profile.livingAreaPhotos.isNotEmpty(),
            profile.checklist.values.any { it }
        )
        return (checks.count { it } * 100) / checks.size
    }

    private companion object {
        const val USERS = "users"
        const val PROFILES = "profiles"
        const val MAIN_PROFILE = "main"
        const val MENU = "menu"
        const val INQUIRIES = "inquiries"
        const val GUIDES = "guides"
        const val BOOKINGS = "bookings"
        const val CALENDAR = "calendar"
    }
}
