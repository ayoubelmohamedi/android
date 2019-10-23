/**
 * ownCloud Android client application
 *
 * @author David González Verdugo
 * @author Abel García de Prada
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

package com.owncloud.android.presentation.capabilities.viewmodel

import android.accounts.Account
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.owncloud.android.domain.UseCaseResult
import com.owncloud.android.domain.capabilities.model.OCCapability
import com.owncloud.android.domain.capabilities.usecases.GetStoredCapabilitiesUseCase
import com.owncloud.android.domain.capabilities.usecases.RefreshCapabilitiesFromServerAsyncUseCase
import com.owncloud.android.presentation.viewmodels.capabilities.OCCapabilityViewModel
import com.owncloud.android.utils.AppTestUtil
import com.owncloud.android.utils.AppTestUtil.DUMMY_CAPABILITY
import com.owncloud.android.utils.TEST_TIMEOUT_IN_MS
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * TODO: Check liveData values on each test and fix commented tests that are not working
 */

@RunWith(JUnit4::class)
class OCCapabilityViewModelTest {
    private lateinit var ocCapabilityViewModel: OCCapabilityViewModel

    private lateinit var getStoredCapabilitiesUseCase: GetStoredCapabilitiesUseCase
    private lateinit var refreshCapabilitiesFromServerUseCase: RefreshCapabilitiesFromServerAsyncUseCase

    private val capabilityLiveData = MutableLiveData<OCCapability>()

    private var testAccount: Account = AppTestUtil.createAccount("admin@server", "test")

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun initTest() {
        getStoredCapabilitiesUseCase = spyk(mockkClass(GetStoredCapabilitiesUseCase::class))
        refreshCapabilitiesFromServerUseCase = spyk(mockkClass(RefreshCapabilitiesFromServerAsyncUseCase::class))

        every { getStoredCapabilitiesUseCase.execute(any()) } returns capabilityLiveData

        ocCapabilityViewModel = OCCapabilityViewModel(
            accountName = testAccount.name,
            getStoredCapabilitiesUseCase = getStoredCapabilitiesUseCase,
            refreshCapabilitiesFromServerUseCase = refreshCapabilitiesFromServerUseCase
        )
    }

    @Test
    fun getStoredCapabilitiesWithData() {
        initTest()

        val capability = DUMMY_CAPABILITY.copy(accountName = testAccount.name)
        capabilityLiveData.postValue(capability)

        coVerify(exactly = 0) { refreshCapabilitiesFromServerUseCase.execute(any()) }
        verify(exactly = 1) { getStoredCapabilitiesUseCase.execute(any()) }
    }

    @Test
    fun getStoredCapabilitiesWithoutData() {
        initTest()

        val capability = null
        capabilityLiveData.postValue(capability)

        coVerify(exactly = 0) { refreshCapabilitiesFromServerUseCase.execute(any()) }
        verify(exactly = 1) { getStoredCapabilitiesUseCase.execute(any()) }
    }

    @Test
    fun fetchCapabilitiesLoading() {
        initTest()

        coEvery { refreshCapabilitiesFromServerUseCase.execute(any()) } returns UseCaseResult.Success(Unit)

        ocCapabilityViewModel.refreshCapabilitiesFromNetwork()

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { refreshCapabilitiesFromServerUseCase.execute(any()) }
        //verify(exactly = 0) { getStoredCapabilitiesUseCase.execute(any()) }
    }

    @Test
    fun fetchCapabilitiesError() {
        initTest()

        coEvery { refreshCapabilitiesFromServerUseCase.execute(any()) } returns UseCaseResult.Success(Unit)

        ocCapabilityViewModel.refreshCapabilitiesFromNetwork()

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { refreshCapabilitiesFromServerUseCase.execute(any()) }
        //verify(exactly = 0) { getStoredCapabilitiesUseCase.execute(any()) }
    }

    @Test
    fun fetchCapabilitiesSuccess(){
        initTest()

        coEvery { refreshCapabilitiesFromServerUseCase.execute(any()) } returns UseCaseResult.Success(Unit)

        ocCapabilityViewModel.refreshCapabilitiesFromNetwork()

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { refreshCapabilitiesFromServerUseCase.execute(any()) }
        //verify(exactly = 0) { getStoredCapabilitiesUseCase.execute(any()) }
    }
}
