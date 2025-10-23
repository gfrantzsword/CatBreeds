package com.example.catbreeds.data.unitTests

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.catbreeds.data.connectivity.ConnectivityCheckerImpl
import com.example.catbreeds.test_core.MainDispatcherRule
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ConnectivityCheckerTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @RelaxedMockK
    private lateinit var context: Context

    @RelaxedMockK
    private lateinit var connectivityManager: ConnectivityManager

    private lateinit var connectivityChecker: ConnectivityCheckerImpl
    private val network: Network = mockk()
    private val networkCapabilities: NetworkCapabilities = mockk()

    @Before
    fun setup() {
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        connectivityChecker = ConnectivityCheckerImpl(context)
    }

    @Test
    fun `WHEN internet is available and validated SHOULD return true`() {
        // GIVEN
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        // WHEN
        val result = connectivityChecker.isConnected()

        // THEN
        assertTrue(result)
    }

    @Test
    fun `WHEN no active network SHOULD return false`() {
        // GIVEN
        every { connectivityManager.activeNetwork } returns null

        // WHEN
        val result = connectivityChecker.isConnected()

        // THEN
        assertFalse(result)
    }

    @Test
    fun `WHEN network capabilities are null SHOULD return false`() {
        // GIVEN
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        // WHEN
        val result = connectivityChecker.isConnected()

        // THEN
        assertFalse(result)
    }

    @Test
    fun `WHEN internet capability is missing SHOULD return false`() {
        // GIVEN
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        // WHEN
        val result = connectivityChecker.isConnected()

        // THEN
        assertFalse(result)
    }

    @Test
    fun `WHEN validated capability is missing SHOULD return false`() {
        // GIVEN
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false

        // WHEN
        val result = connectivityChecker.isConnected()

        // THEN
        assertFalse(result)
    }
}