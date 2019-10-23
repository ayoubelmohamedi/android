/**
 * ownCloud Android client application
 *
 * @author David González Verdugo
 * Copyright (C) 2019 ownCloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.presentation.viewmodels.capabilities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owncloud.android.domain.capabilities.model.OCCapability
import com.owncloud.android.domain.capabilities.usecases.GetStoredCapabilitiesUseCase
import com.owncloud.android.domain.capabilities.usecases.RefreshCapabilitiesFromServerAsyncUseCase
import com.owncloud.android.presentation.UIResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * View Model to keep a reference to the capability repository and an up-to-date capability
 */
class OCCapabilityViewModel(
    private val accountName: String,
    getStoredCapabilitiesUseCase: GetStoredCapabilitiesUseCase,
    private val refreshCapabilitiesFromServerUseCase: RefreshCapabilitiesFromServerAsyncUseCase
) : ViewModel() {

    private val _capabilities = MediatorLiveData<UIResult<OCCapability>>()
    val capabilities: LiveData<UIResult<OCCapability>> = _capabilities

    init {
        _capabilities.addSource(
            getStoredCapabilitiesUseCase.execute(
                GetStoredCapabilitiesUseCase.Params(
                    accountName = accountName
                )
            )
        ) { capabilitiesFromDatabase ->
            capabilitiesFromDatabase?.let {
                _capabilities.postValue(UIResult.Success(capabilitiesFromDatabase))
            }
        }
    }

    fun refreshCapabilitiesFromNetwork() {
        viewModelScope.launch {
            _capabilities.postValue(
                UIResult.Loading()
            )

            val useCaseResult = withContext(Dispatchers.IO) {
                refreshCapabilitiesFromServerUseCase.execute(
                    RefreshCapabilitiesFromServerAsyncUseCase.Params(
                        accountName = accountName
                    )
                )
            }

            if (!useCaseResult.isSuccess) {
                _capabilities.postValue(
                    UIResult.Error(useCaseResult.getThrowableOrNull())
                )
            }
        }
    }
}
