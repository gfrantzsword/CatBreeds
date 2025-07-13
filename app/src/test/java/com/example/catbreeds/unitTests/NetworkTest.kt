package com.example.catbreeds.unitTests

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.catbreeds.data.connectivity.ConnectivityCheckerImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ConnectivityCheckerTest {

    private lateinit var connectivityChecker: ConnectivityCheckerImpl
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var networkCapabilities: NetworkCapabilities

    @Before
    fun setup() {
        context = mockk()
        connectivityManager = mockk()
        network = mockk()
        networkCapabilities = mockk()

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        connectivityChecker = ConnectivityCheckerImpl(context)
    }

    @Test
    fun `isConnected returns true when internet is available and validated`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        val result = connectivityChecker.isConnected()

        assertTrue(result)
    }

    @Test
    fun `isConnected returns false when no active network`() {
        every { connectivityManager.activeNetwork } returns null

        val result = connectivityChecker.isConnected()

        assertFalse(result)
    }

    @Test
    fun `isConnected returns false when network capabilities are null`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        val result = connectivityChecker.isConnected()

        assertFalse(result)
    }

    @Test
    fun `isConnected returns false when internet capability is missing`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        val result = connectivityChecker.isConnected()

        assertFalse(result)
    }

    @Test
    fun `isConnected returns false when validated capability is missing`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false

        val result = connectivityChecker.isConnected()

        assertFalse(result)
    }
}