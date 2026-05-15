package com.example.namma_homestay

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomestayViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _profile = MutableStateFlow(HomestayProfile())
    val profile: StateFlow<HomestayProfile> = _profile.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profile.value = repository.fetchProfile()
        }
    }

    fun toggleChecklistItem(item: String, isChecked: Boolean) {
        val currentProfile = _profile.value
        val updatedChecklist = currentProfile.checklist.toMutableMap()
        updatedChecklist[item] = isChecked
        
        _profile.value = currentProfile.copy(checklist = updatedChecklist)
        
        viewModelScope.launch {
            repository.updateChecklist(updatedChecklist)
        }
    }

    fun addRoomPhoto(uri: Uri) {
        val current = _profile.value
        val localUri = uri.toString()
        _profile.value = current.copy(
            roomPhotos = current.roomPhotos + localUri,
            photos = (current.photos + localUri).distinct()
        )
        viewModelScope.launch {
            val uploadedUrl = repository.uploadImage(uri)
            val updated = _profile.value.copy(
                roomPhotos = _profile.value.roomPhotos.map { if (it == localUri) uploadedUrl else it },
                photos = _profile.value.photos.map { if (it == localUri) uploadedUrl else it }.distinct()
            )
            _profile.value = updated
            repository.updateProfilePhotos(updated.roomPhotos, updated.livingAreaPhotos)
        }
    }

    fun addLivingAreaPhoto(uri: Uri) {
        val current = _profile.value
        val localUri = uri.toString()
        _profile.value = current.copy(
            livingAreaPhotos = current.livingAreaPhotos + localUri,
            photos = (current.photos + localUri).distinct()
        )
        viewModelScope.launch {
            val uploadedUrl = repository.uploadImage(uri)
            val updated = _profile.value.copy(
                livingAreaPhotos = _profile.value.livingAreaPhotos.map { if (it == localUri) uploadedUrl else it },
                photos = _profile.value.photos.map { if (it == localUri) uploadedUrl else it }.distinct()
            )
            _profile.value = updated
            repository.updateProfilePhotos(updated.roomPhotos, updated.livingAreaPhotos)
        }
    }

    fun removeRoomPhoto(uriString: String) {
        val current = _profile.value
        val updated = current.copy(
            roomPhotos = current.roomPhotos - uriString,
            photos = current.photos - uriString
        )
        _profile.value = updated
        viewModelScope.launch {
            repository.updateProfilePhotos(updated.roomPhotos, updated.livingAreaPhotos)
        }
    }

    fun removeLivingAreaPhoto(uriString: String) {
        val current = _profile.value
        val updated = current.copy(
            livingAreaPhotos = current.livingAreaPhotos - uriString,
            photos = current.photos - uriString
        )
        _profile.value = updated
        viewModelScope.launch {
            repository.updateProfilePhotos(updated.roomPhotos, updated.livingAreaPhotos)
        }
    }
}

class MenuViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    init {
        loadMenu()
    }

    private fun loadMenu() {
        viewModelScope.launch {
            _menuItems.value = repository.fetchMenuItems()
        }
    }

    fun toggleAvailability(id: String, isAvailable: Boolean) {
        val updatedItems = _menuItems.value.map {
            if (it.id == id) it.copy(isAvailable = isAvailable) else it
        }
        _menuItems.value = updatedItems
        
        viewModelScope.launch {
            val itemToUpdate = updatedItems.find { it.id == id }
            itemToUpdate?.let { repository.saveMenuItem(it) }
        }
    }

    fun addMenuItem(name: String, description: String, price: Double, imageUri: Uri?) {
        viewModelScope.launch {
            val imageUrl = imageUri?.let { repository.uploadImage(it) }
            val newItem = MenuItem(
                id = System.currentTimeMillis().toString(),
                name = name,
                description = description,
                price = price,
                imageUrl = imageUrl,
                isAvailable = true
            )
            val currentList = _menuItems.value.toMutableList()
            currentList.add(newItem)
            _menuItems.value = currentList
            repository.saveMenuItem(newItem)
        }
    }
}

class InquiryViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _inquiries = MutableStateFlow<List<Inquiry>>(emptyList())
    val inquiries: StateFlow<List<Inquiry>> = _inquiries.asStateFlow()

    init {
        loadInquiries()
    }

    private fun loadInquiries() {
        viewModelScope.launch {
            _inquiries.value = repository.fetchInquiries()
        }
    }

    fun sendMessage(inquiryId: String, text: String) {
        val currentInquiries = _inquiries.value.toMutableList()
        val index = currentInquiries.indexOfFirst { it.id == inquiryId }
        if (index != -1) {
            val inquiry = currentInquiries[index]
            val newMessage = ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "host",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            val updatedMessages = inquiry.messages + newMessage
            currentInquiries[index] = inquiry.copy(messages = updatedMessages)
            _inquiries.value = currentInquiries
            viewModelScope.launch {
                repository.saveInquiryMessages(inquiryId, updatedMessages)
            }
        }
    }
}

class GuideViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _guideSpots = MutableStateFlow<List<GuideSpot>>(emptyList())
    val guideSpots: StateFlow<List<GuideSpot>> = _guideSpots.asStateFlow()

    init {
        loadGuideSpots()
    }

    private fun loadGuideSpots() {
        viewModelScope.launch {
            _guideSpots.value = repository.fetchGuideSpots()
        }
    }

    fun addGuideSpot(name: String, distance: String, description: String) {
        val newSpot = GuideSpot(
            id = System.currentTimeMillis().toString(),
            name = name,
            distance = distance,
            description = description
        )
        _guideSpots.value = _guideSpots.value + newSpot
        viewModelScope.launch {
            repository.saveGuideSpot(newSpot)
        }
    }
}

class DashboardViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _stats.value = repository.fetchDashboardStats()
            _bookings.value = repository.fetchBookings()
        }
    }
}

class CalendarViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _calendarDays = MutableStateFlow<Map<String, CalendarDay>>(emptyMap())
    val calendarDays: StateFlow<Map<String, CalendarDay>> = _calendarDays.asStateFlow()

    init {
        loadCalendar()
    }

    private fun loadCalendar() {
        viewModelScope.launch {
            _calendarDays.value = repository.fetchCalendarDays()
        }
    }

    fun toggleAvailability(dateString: String) {
        val currentMap = _calendarDays.value.toMutableMap()
        val day = currentMap[dateString]
        if (day != null) {
            currentMap[dateString] = day.copy(isAvailable = !day.isAvailable)
            _calendarDays.value = currentMap
            viewModelScope.launch {
                repository.saveCalendarDay(currentMap.getValue(dateString))
            }
        }
    }
}

class TravelerViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _homestays = MutableStateFlow<List<HomestayProfile>>(emptyList())
    val homestays: StateFlow<List<HomestayProfile>> = _homestays.asStateFlow()

    init {
        loadHomestays()
    }

    private fun loadHomestays() {
        viewModelScope.launch {
            _homestays.value = repository.fetchHomestays()
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _currentUser = MutableStateFlow(User())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = repository.getCurrentUser()
            if (user != null && user.role != UserRole.NONE) {
                _currentUser.value = user
                _isAuthenticated.value = true
            }
            _authUiState.value = AuthUiState(isLoading = false)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState(isLoading = false, errorMessage = "Enter your email and password.")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            runCatching {
                repository.signIn(email, password)
            }.onSuccess { user ->
                _currentUser.value = user
                _isAuthenticated.value = user.role != UserRole.NONE
                _authUiState.value = AuthUiState(isLoading = false)
            }.onFailure { error ->
                _authUiState.value = AuthUiState(isLoading = false, errorMessage = readableAuthError(error))
            }
        }
    }

    fun signUp(name: String, email: String, password: String, role: UserRole) {
        if (name.isBlank() || email.isBlank() || password.length < 6) {
            _authUiState.value = AuthUiState(
                isLoading = false,
                errorMessage = "Add your name, email, and a password with at least 6 characters."
            )
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            runCatching {
                repository.signUp(name, email, password, role)
            }.onSuccess { user ->
                _currentUser.value = user
                _isAuthenticated.value = true
                _authUiState.value = AuthUiState(isLoading = false)
            }.onFailure { error ->
                _authUiState.value = AuthUiState(isLoading = false, errorMessage = readableAuthError(error))
            }
        }
    }

    fun logout() {
        repository.signOut()
        _currentUser.value = User()
        _isAuthenticated.value = false
        _authUiState.value = AuthUiState(isLoading = false)
    }

    fun clearError() {
        _authUiState.value = _authUiState.value.copy(errorMessage = null)
    }

    private fun readableAuthError(error: Throwable): String {
        return error.message
            ?.takeIf { it.isNotBlank() }
            ?: "Authentication failed. Check Firebase Authentication is enabled for Email/Password."
    }
}
