package hu.bme.spacedumpling.worktimemanager.presentation.baseclasses.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import hu.bme.spacedumpling.worktimemanager.logic.repository.appsettings.AppSettingsRepository
import hu.bme.spacedumpling.worktimemanager.presentation.baseclasses.actions.FragmentAction
import hu.bme.spacedumpling.worktimemanager.presentation.baseclasses.actions.UIAction
import hu.bme.spacedumpling.worktimemanager.presentation.page.projects.MakeToast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class BaseViewModel(
    protected val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val UIActionFlow = MutableSharedFlow<UIAction>(1)

    protected val fragmentActionFlow = MutableSharedFlow<FragmentAction>(1)
    val fragmentActionLiveData = fragmentActionFlow.asLiveData()

    init{
        viewModelScope.launch {
            appSettingsRepository.networkErrorMessage.collect {
                fragmentActionFlow.tryEmit(MakeToast(it))
            }
        }
    }
}