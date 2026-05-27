package com.example.safekidsmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safekidsmobile.data.model.Device
import com.example.safekidsmobile.data.model.LinkDeviceResponse
import com.example.safekidsmobile.data.model.LocationData
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
    val nameInput: String = "",
    val isSuccess: Boolean = false,
    val error: String? = null,
    val pairedDevice: LinkDeviceResponse? = null
)

data class SettingsUiState(
    val locationIntervalMin: Int = 30,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

data class LocationHistoryUiState(
    val locations: List<LocationData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
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

    init { loadDevices() }

    fun loadDevices() {
        viewModelScope.launch {
            _deviceState.value = _deviceState.value.copy(isLoading = true, error = null)
            when (val result = deviceRepository.listDevices()) {
                is DeviceResult.Success -> _deviceState.value = DeviceUiState(
                    devices = result.data,
                    selectedDevice = result.data.firstOrNull()
                )
                is DeviceResult.Error -> _deviceState.value = DeviceUiState(
                    error = result.exception.message ?: "Unknown error"
                )
                else -> {}
            }
        }
    }

    fun selectDevice(device: Device) {
        _deviceState.value = _deviceState.value.copy(selectedDevice = device)
    }

    fun pairDevice(pin: String, name: String) {
        viewModelScope.launch {
            _pairingState.value = _pairingState.value.copy(isPairing = true, error = null, isSuccess = false)
            when (val result = deviceRepository.linkDevice(pin, name)) {
                is DeviceResult.Success -> {
                    _pairingState.value = PairingUiState(isSuccess = true, pairedDevice = result.data)
                    loadDevices()
                }
                is DeviceResult.Error -> _pairingState.value = PairingUiState(
                    error = result.exception.message ?: "Pairing failed"
                )
                else -> {}
            }
        }
    }

    fun updatePinInput(pin: String) {
        _pairingState.value = _pairingState.value.copy(pinInput = pin)
    }

    fun updateNameInput(name: String) {
        _pairingState.value = _pairingState.value.copy(nameInput = name)
    }

    fun clearPairingState() { _pairingState.value = PairingUiState() }

    fun updateLocationInterval(deviceId: String, intervalMin: Int) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isSaving = true, error = null, isSuccess = false)
            when (val result = deviceRepository.updateDeviceSettings(deviceId, intervalMin)) {
                is DeviceResult.Success -> _settingsState.value = SettingsUiState(
                    locationIntervalMin = intervalMin,
                    isSuccess = true
                )
                is DeviceResult.Error -> _settingsState.value = SettingsUiState(
                    error = result.exception.message ?: "Failed to update settings"
                )
                else -> {}
            }
        }
    }

    fun unpairDevice(deviceId: String) {
        viewModelScope.launch {
            _deviceState.value = _deviceState.value.copy(isLoading = true)
            when (val result = deviceRepository.unpairDevice(deviceId)) {
                is DeviceResult.Success -> loadDevices()
                is DeviceResult.Error -> _deviceState.value = _deviceState.value.copy(
                    isLoading = false,
                    error = result.exception.message ?: "Failed to unpair device"
                )
                else -> {}
            }
        }
    }

    fun loadDevice(deviceId: String) {
        viewModelScope.launch {
            when (val result = deviceRepository.getDevice(deviceId)) {
                is DeviceResult.Success -> _deviceState.value = _deviceState.value.copy(
                    selectedDevice = result.data
                )
                else -> {}
            }
        }
    }

    fun loadCurrentLocation(deviceId: String) {
        viewModelScope.launch {
            when (val result = deviceRepository.getCurrentLocation(deviceId)) {
                is DeviceResult.Success -> _locationHistoryState.value = LocationHistoryUiState(
                    locations = listOf(result.data)
                )
                else -> {}
            }
        }
    }

    fun requestLocation(deviceId: String) {
        viewModelScope.launch {
            deviceRepository.requestLocation(deviceId)
        }
    }

    fun loadLocationHistory(deviceId: String, from: String, to: String) {
        viewModelScope.launch {
            _locationHistoryState.value = LocationHistoryUiState(isLoading = true)
            when (val result = deviceRepository.getLocationHistory(deviceId, from, to)) {
                is DeviceResult.Success -> _locationHistoryState.value = LocationHistoryUiState(
                    locations = result.data
                )
                is DeviceResult.Error -> _locationHistoryState.value = LocationHistoryUiState(
                    error = result.exception.message ?: "Failed to load location history"
                )
                else -> {}
            }
        }
    }
}
