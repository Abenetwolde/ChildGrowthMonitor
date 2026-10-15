package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DashboardMetrics
import com.example.data.NutriScanRepository
import com.example.data.local.ChildEntity
import com.example.data.local.FollowUpEntity
import com.example.data.local.ScreeningEntity
import com.example.model.AppLanguage
import com.example.model.LocalizedStrings
import com.example.model.StringDictionary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    CHILDREN,
    SCREENING,
    FOLLOW_UPS,
    SETTINGS,
    CHILD_DETAIL,
    REGISTER_CHILD
}

class NutriScanViewModel(
    private val repository: NutriScanRepository
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedChild = MutableStateFlow<ChildEntity?>(null)
    val selectedChild: StateFlow<ChildEntity?> = _selectedChild.asStateFlow()

    private val _childScreenings = MutableStateFlow<List<ScreeningEntity>>(emptyList())
    val childScreenings: StateFlow<List<ScreeningEntity>> = _childScreenings.asStateFlow()

    private val _preselectedChildIdForScreening = MutableStateFlow<String?>(null)
    val preselectedChildIdForScreening: StateFlow<String?> = _preselectedChildIdForScreening.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val metrics: StateFlow<DashboardMetrics> = repository.metrics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardMetrics(0, 0, 0, 0)
        )

    val allChildren: StateFlow<List<ChildEntity>> = repository.allChildren
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentScreenings: StateFlow<List<ScreeningEntity>> = repository.recentScreenings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allFollowUps: StateFlow<List<FollowUpEntity>> = repository.allFollowUps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getStrings(): LocalizedStrings = StringDictionary.get(_currentLanguage.value)

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.SCREENING && _preselectedChildIdForScreening.value == null) {
            // New screening from scratch
        }
        _currentScreen.value = screen
    }

    fun openChildDetail(child: ChildEntity) {
        _selectedChild.value = child
        viewModelScope.launch {
            repository.getScreeningsForChild(child.id).collect {
                _childScreenings.value = it
            }
        }
        _currentScreen.value = AppScreen.CHILD_DETAIL
    }

    fun startScreeningForChild(childId: String?) {
        _preselectedChildIdForScreening.value = childId
        _currentScreen.value = AppScreen.SCREENING
    }

    fun registerChild(
        firstName: String,
        lastName: String,
        sex: String,
        dateOfBirth: String?,
        estimatedAgeMonths: Int,
        caregiverName: String,
        caregiverPhone: String,
        region: String,
        zone: String,
        woreda: String,
        kebele: String,
        gotCommunity: String,
        isDraft: Boolean,
        startScreeningImmediately: Boolean
    ) {
        viewModelScope.launch {
            val child = repository.registerChild(
                firstName = firstName,
                lastName = lastName,
                sex = sex,
                dateOfBirth = dateOfBirth,
                estimatedAgeMonths = estimatedAgeMonths,
                caregiverName = caregiverName,
                caregiverPhone = caregiverPhone,
                region = region,
                zone = zone,
                woreda = woreda,
                kebele = kebele,
                gotCommunity = gotCommunity,
                isDraft = isDraft
            )
            if (startScreeningImmediately) {
                _preselectedChildIdForScreening.value = child.id
                _currentScreen.value = AppScreen.SCREENING
            } else {
                _currentScreen.value = AppScreen.CHILDREN
            }
        }
    }

    fun saveScreening(
        childId: String,
        childFullName: String,
        category: String,
        hasEdema: Boolean,
        edemaGrade: String,
        muacCm: Double,
        weightKg: Double?,
        heightCm: Double?,
        dangerSignsPresent: Boolean,
        dangerSignsNotes: String,
        actionTaken: String,
        primaryReason: String,
        followUpDate: Long?,
        imageVerdict: String,
        manualOverride: Boolean
    ) {
        viewModelScope.launch {
            repository.saveScreening(
                childId = childId,
                childFullName = childFullName,
                clinicalCategory = category,
                hasEdema = hasEdema,
                edemaGrade = edemaGrade,
                muacCm = muacCm,
                weightKg = weightKg,
                heightCm = heightCm,
                dangerSignsPresent = dangerSignsPresent,
                dangerSignsNotes = dangerSignsNotes,
                actionTaken = actionTaken,
                primaryReason = primaryReason,
                nextFollowUpDate = followUpDate,
                imageVerdict = imageVerdict,
                manualOverride = manualOverride
            )
            _preselectedChildIdForScreening.value = null
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun completeFollowUp(id: String) {
        viewModelScope.launch {
            repository.completeFollowUp(id)
        }
    }

    fun syncNow() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        viewModelScope.launch {
            repository.syncAllRecords()
            _isSyncing.value = false
        }
    }

    class Factory(private val repository: NutriScanRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NutriScanViewModel(repository) as T
        }
    }
}
