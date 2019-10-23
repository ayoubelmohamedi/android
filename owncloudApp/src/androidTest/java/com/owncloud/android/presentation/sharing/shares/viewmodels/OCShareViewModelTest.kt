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

package com.owncloud.android.presentation.sharing.shares.viewmodels

import android.accounts.Account
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.owncloud.android.domain.UseCaseResult
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.usecases.CreatePrivateShareAsyncUseCase
import com.owncloud.android.domain.sharing.shares.usecases.CreatePublicShareAsyncUseCase
import com.owncloud.android.domain.sharing.shares.usecases.DeleteShareAsyncUseCase
import com.owncloud.android.domain.sharing.shares.usecases.EditPrivateShareAsyncUseCase
import com.owncloud.android.domain.sharing.shares.usecases.EditPublicShareAsyncUseCase
import com.owncloud.android.domain.sharing.shares.usecases.GetShareAsLiveDataUseCase
import com.owncloud.android.domain.sharing.shares.usecases.GetSharesAsLiveDataUseCase
import com.owncloud.android.domain.sharing.shares.usecases.RefreshSharesFromServerAsyncUseCase
import com.owncloud.android.presentation.viewmodels.sharing.OCShareViewModel
import com.owncloud.android.utils.AppTestUtil
import com.owncloud.android.utils.AppTestUtil.DUMMY_SHARE
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
class OCShareViewModelTest {
    private lateinit var ocShareViewModel: OCShareViewModel

    private lateinit var getSharesAsLiveDataUseCase: GetSharesAsLiveDataUseCase
    private lateinit var getShareAsLiveDataUseCase: GetShareAsLiveDataUseCase
    private lateinit var refreshSharesFromServerAsyncUseCase: RefreshSharesFromServerAsyncUseCase
    private lateinit var createPrivateShareAsyncUseCase: CreatePrivateShareAsyncUseCase
    private lateinit var editPrivateShareAsyncUseCase: EditPrivateShareAsyncUseCase
    private lateinit var createPublicShareAsyncUseCase: CreatePublicShareAsyncUseCase
    private lateinit var editPublicShareAsyncUseCase: EditPublicShareAsyncUseCase
    private lateinit var deletePublicShareAsyncUseCase: DeleteShareAsyncUseCase

    private val filePath = "/Photos/image.jpg"

    private var testAccount: Account = AppTestUtil.createAccount("admin@server", "test")

    private val sharesLiveData = MutableLiveData<List<OCShare>?>()
    private val privateShareLiveData = MutableLiveData<OCShare>()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun initTest() {
        getSharesAsLiveDataUseCase = spyk(mockkClass(GetSharesAsLiveDataUseCase::class))
        getShareAsLiveDataUseCase = spyk(mockkClass(GetShareAsLiveDataUseCase::class))
        refreshSharesFromServerAsyncUseCase = spyk(mockkClass(RefreshSharesFromServerAsyncUseCase::class))
        createPrivateShareAsyncUseCase = spyk(mockkClass(CreatePrivateShareAsyncUseCase::class))
        editPrivateShareAsyncUseCase = spyk(mockkClass(EditPrivateShareAsyncUseCase::class))
        createPublicShareAsyncUseCase = spyk(mockkClass(CreatePublicShareAsyncUseCase::class))
        editPublicShareAsyncUseCase = spyk(mockkClass(EditPublicShareAsyncUseCase::class))
        deletePublicShareAsyncUseCase = spyk(mockkClass(DeleteShareAsyncUseCase::class))

        every { getSharesAsLiveDataUseCase.execute(any()) } returns sharesLiveData
        every { getShareAsLiveDataUseCase.execute(any()) } returns privateShareLiveData

        ocShareViewModel = OCShareViewModel(
            filePath,
            testAccount.name,
            getSharesAsLiveDataUseCase,
            getShareAsLiveDataUseCase,
            refreshSharesFromServerAsyncUseCase,
            createPrivateShareAsyncUseCase,
            editPrivateShareAsyncUseCase,
            createPublicShareAsyncUseCase,
            editPublicShareAsyncUseCase,
            deletePublicShareAsyncUseCase
        )
    }

    /******************************************************************************************************
     ******************************************* PRIVATE SHARES *******************************************
     ******************************************************************************************************/

    @Test
    fun insertPrivateShareSuccess() {
        initTest()

        coEvery { createPrivateShareAsyncUseCase.execute(any()) } returns UseCaseResult.Success(Unit)

        ocShareViewModel.insertPrivateShare(
            filePath = DUMMY_SHARE.path,
            shareType = DUMMY_SHARE.shareType,
            shareeName = DUMMY_SHARE.accountOwner,
            permissions = DUMMY_SHARE.permissions,
            accountName = DUMMY_SHARE.accountOwner
        )

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { createPrivateShareAsyncUseCase.execute(any()) }
        coVerify(exactly = 0) { createPublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun insertPrivateShareFails() {
        initTest()

        coEvery { createPrivateShareAsyncUseCase.execute(any()) } returns UseCaseResult.Error(Throwable())

        ocShareViewModel.insertPrivateShare(
            filePath = DUMMY_SHARE.path,
            shareType = DUMMY_SHARE.shareType,
            shareeName = DUMMY_SHARE.accountOwner,
            permissions = DUMMY_SHARE.permissions,
            accountName = DUMMY_SHARE.accountOwner
        )

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { createPrivateShareAsyncUseCase.execute(any()) }
        coVerify(exactly = 0) { createPublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun updatePrivateShareSuccess() {
        initTest()

        coEvery { editPrivateShareAsyncUseCase.execute(any()) } returns UseCaseResult.Success(Unit)

        ocShareViewModel.updatePrivateShare(
            remoteId = DUMMY_SHARE.remoteId,
            permissions = DUMMY_SHARE.permissions,
            accountName = DUMMY_SHARE.accountOwner
        )

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { editPrivateShareAsyncUseCase.execute(any()) }
        coVerify(exactly = 0) { editPublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun updatePrivateShareFails() {
        initTest()

        coEvery { editPrivateShareAsyncUseCase.execute(any()) } returns UseCaseResult.Error(Throwable())

        ocShareViewModel.updatePrivateShare(
            remoteId = DUMMY_SHARE.remoteId,
            permissions = DUMMY_SHARE.permissions,
            accountName = DUMMY_SHARE.accountOwner
        )

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { editPrivateShareAsyncUseCase.execute(any()) }
        coVerify(exactly = 0) { editPublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun refreshPrivateShareNull() {
        initTest()

        privateShareLiveData.value = null

        ocShareViewModel.refreshPrivateShare(DUMMY_SHARE.remoteId)

        verify(exactly = 1) { getShareAsLiveDataUseCase.execute(GetShareAsLiveDataUseCase.Params(DUMMY_SHARE.remoteId)) }
        //verify(exactly = 0) { getSharesAsLiveDataUseCase.execute(any()) }
    }

    @Test
    fun refreshPrivateShareWithData() {
        initTest()

        privateShareLiveData.value = DUMMY_SHARE.copy(id = 123, name = "PhotoLink")

        ocShareViewModel.refreshPrivateShare(DUMMY_SHARE.remoteId)

        verify(exactly = 1) { getShareAsLiveDataUseCase.execute(any()) }
        //verify(exactly = 0) { getSharesAsLiveDataUseCase.execute(any()) }
    }

    /******************************************************************************************************
     ******************************************* PUBLIC SHARES ********************************************
     ******************************************************************************************************/

    @Test
    fun insertPublicShareSuccess() {
        initTest()

        coEvery { createPublicShareAsyncUseCase.execute(any()) } returns UseCaseResult.Success(Unit)

        ocShareViewModel.insertPublicShare(
            filePath = DUMMY_SHARE.path,
            name = "Photos 2 link",
            password = "1234",
            expirationTimeInMillis = -1,
            publicUpload = false,
            permissions = DUMMY_SHARE.permissions,
            accountName = DUMMY_SHARE.accountOwner
        )

        coVerify(exactly = 0) { createPrivateShareAsyncUseCase.execute(any()) }
        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { createPublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun insertPublicShareFails() {
        initTest()

        coEvery { createPublicShareAsyncUseCase.execute(any()) } returns UseCaseResult.Error(Throwable())

        ocShareViewModel.insertPublicShare(
            filePath = DUMMY_SHARE.path,
            name = "Photos 2 link",
            password = "1234",
            expirationTimeInMillis = -1,
            publicUpload = false,
            permissions = DUMMY_SHARE.permissions,
            accountName = DUMMY_SHARE.accountOwner
        )

        coVerify(exactly = 0) { createPrivateShareAsyncUseCase.execute(any()) }
        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { createPublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun updatePublicShareSuccess() {
        initTest()

        ocShareViewModel.updatePublicShare(
            remoteId = 1,
            name = "Photos 2 link",
            password = "1234",
            expirationDateInMillis = -1,
            publicUpload = false,
            permissions = -2,
            accountName = "Juan"
        )

        coVerify(exactly = 0) { editPrivateShareAsyncUseCase.execute(any()) }
        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { editPublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun updatePublicShareFails() {
        initTest()

        ocShareViewModel.updatePublicShare(
            remoteId = 1,
            name = "Photos 2 link",
            password = "1234",
            expirationDateInMillis = -1,
            publicUpload = false,
            permissions = -1,
            accountName = "Carlos"
        )

        coVerify(exactly = 0) { editPrivateShareAsyncUseCase.execute(any()) }
        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { editPublicShareAsyncUseCase.execute(any()) }
    }

    /******************************************************************************************************
     *********************************************** COMMON ***********************************************
     ******************************************************************************************************/

    @Test
    fun deletePublicShareSuccess() {
        initTest()

        coEvery { deletePublicShareAsyncUseCase.execute(any()) } returns UseCaseResult.Success(Unit)

        ocShareViewModel.deleteShare(
            remoteId = DUMMY_SHARE.remoteId
        )

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { deletePublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun deletePublicShareFails() {
        initTest()

        coEvery { deletePublicShareAsyncUseCase.execute(any()) } returns UseCaseResult.Error(Throwable())

        ocShareViewModel.deleteShare(
            remoteId = DUMMY_SHARE.remoteId
        )

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { deletePublicShareAsyncUseCase.execute(any()) }
    }

    @Test
    fun getSharesAsLiveDataEmpty() {
        initTest()

        sharesLiveData.value = listOf()

        ocShareViewModel.refreshSharesFromNetwork()

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { refreshSharesFromServerAsyncUseCase.execute(any()) }
    }

    @Test
    fun getSharesAsLiveDataNull() {
        initTest()

        sharesLiveData.value = null

        ocShareViewModel.refreshSharesFromNetwork()

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { refreshSharesFromServerAsyncUseCase.execute(any()) }
    }

    @Test
    fun getSharesAsLiveDataWithData() {
        initTest()

        sharesLiveData.value = listOf(DUMMY_SHARE, DUMMY_SHARE.copy(id = 123, name = "PhotoLink"))

        ocShareViewModel.refreshSharesFromNetwork()

        coVerify(exactly = 1, timeout = TEST_TIMEOUT_IN_MS) { refreshSharesFromServerAsyncUseCase.execute(any()) }
    }
}
