package com.example.safekidsmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safekidsmobile.data.model.Device
import com.example.safekidsmobile.data.model.LocationData
import com.example.safekidsmobile.data.model.PairDeviceResponse
import com.example.safekidsmobile.data.repository.DeviceRepository
import com.example.safekidsmobile.data.repository.DeviceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDevice: Device? = null
)

data class PairingUiState(
    val isPairing: Boolean = false,
    val pinInput: String = "",
    val isSuccess: Boolean = false,
    val error: String? = null,
    val pairedDevice: PairDeviceResponse? = null
)

data class SettingsUiState(
    val locationIntervalMin: Int = 5,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

data class LocationHistoryUiState(
    val locations: List<LocationData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val startDate: String = "",
    val endDate: String = ""
)

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    
    private val _deviceState = MutableStateFlow(DeviceUiState())
    val deviceState: StateFlow<DeviceUiState> = _deviceState.asStateFlow()
    
    private val _pairingState = MutableStateFlow(PairingUiState())
    val pairingState: StateFlow<PairingUiState> = _pairingState.asStateFlow()
    
    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()
    
    private val _locationHistoryState = MutableStateFlow(LocationHistoryUiState())
    val locationHistoryState: StateFlow<LocationHistoryUiState> = _locationHistoryState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _deviceState.value = _deviceState.value.copy(isLoading = true, error = null)
            val result = deviceRepository.listDevices()
            when (result) {
                is DeviceResult.Success -> {
                    _deviceState.value = _deviceState.value.copy(
                        devices = result.data,
                        isLoading = false,
                        selectedDevice = result.data.firstOrNull()
                    )
                }
                is DeviceResult.Error -> {
                    _deviceState.value = _deviceState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Unknown error"
                    )
                }
                else -> {}
            }
        }
    }

    fun selectDevice(device: Device) {
        _deviceState.value = _deviceState.value.copy(selectedDevice = device)
    }

    fun pairDevice(pin: String) {
        viewModelScope.launch {
            _pairingState.value = _pairingState.value.copy(isPairing = true, error = null, isSuccess = false)
            val result = deviceRepository.pairDevice(pin)
            when (result) {
                is DeviceResult.Success -> {
                    _pairingState.value = _pairingState.value.copy(
                        isPairing = false,
                        pairedDevice = result.data,
                        isSuccess = true,
                        pinInput = ""
                    )
                    // Reload device list
                    loadDevices()
                }
                is DeviceResult.Error -> {
                    _pairingState.value = _pairingState.value.copy(
                        isPairing = false,
                        error = result.exception.message ?: "Pairing failed"
                    )
                }
                else -> {}
            }
        }
    }

    fun updatePinInput(pin: String) {
        _pairingState.value = _pairingState.value.copy(pinInput = pin)
    }

    fun clearPairingState() {
        _pairingState.value = PairingUiState()
    }

    fun updateLocationInterval(deviceId: String, intervalMin: Int) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isSaving = true, error = null, isSuccess = false)
            val result = deviceRepository.updateDeviceSettings(deviceId, intervalMin)
            when (result) {
                is DeviceResult.Success -> {
                    _settingsState.value = _settingsState.value.copy(
                        isSaving = false,
                        locationIntervalMin = intervalMin,
                        isSuccess = true
                    )
                }
                is DeviceResult.Error -> {
                    _settingsState.value = _settingsState.value.copy(
                        isSaving = false,
                        error = result.exception.message ?: "Failed to update settings"
                    )
                }
                else -> {}
            }
        }
    }

    fun unpairDevice(deviceId: String) {
        viewModelScope.launch {
            _deviceState.value = _deviceState.value.copy(isLoading = true)
            val result = deviceRepository.unpairDevice(deviceId)
            when (result) {
                is DeviceResult.Success -> {
                    // Reload device list after unpair
                    loadDevices()
                }
                is DeviceResult.Error -> {
                    _deviceState.value = _deviceState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to unpair device"
                    )
                }
                else -> {}
            }
        }
    }

    fun loadLocationHistory(deviceId: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            _locationHistoryState.value = _locationHistoryState.value.copy(isLoading = true, error = null)
            val result = deviceRepository.getLocationHistory(deviceId, startDate, endDate)
            when (result) {
                is DeviceResult.Success -> {
                    _locationHistoryState.value = _locationHistoryState.value.copy(
                        locations = result.data,
                        isLoading = false,
                        startDate = startDate,
                        endDate = endDate
                    )
                }
                is DeviceResult.Error -> {
                    _locationHistoryState.value = _locationHistoryState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to load location history"
                    )
                }
                else -> {}
            }
        }
    }
}
