package com.example.c001apk.compose.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.model.HomeMenu
import com.example.c001apk.compose.logic.repository.HomeMenuRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeMenuRepo: HomeMenuRepo,
) : ViewModel() {

    val homeMenus: Flow<List<HomeMenu>> = homeMenuRepo.loadAllListFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            homeMenuRepo.upsertList(normalizeMenus(homeMenuRepo.loadAllList()))
        }
    }

    fun setEnabledTabs(enabledTabs: Set<TabType>) {
        if (enabledTabs.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val enabledNames = enabledTabs.mapTo(mutableSetOf()) { it.name }
            val menus = normalizeMenus(homeMenuRepo.loadAllList()).map { menu ->
                menu.copy(isEnable = menu.title in enabledNames)
            }
            homeMenuRepo.upsertList(menus)
        }
    }

    private fun normalizeMenus(storedMenus: List<HomeMenu>): List<HomeMenu> {
        val storedByTitle = storedMenus.associateBy(HomeMenu::title)
        return TabType.entries.mapIndexed { index, type ->
            storedByTitle[type.name]?.copy(position = index)
                ?: HomeMenu(position = index, title = type.name, isEnable = true)
        }
    }
}
