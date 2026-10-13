package com.example

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.DemoStatusProvider
import com.example.model.StatusItem
import com.example.ui.AppTab
import com.example.ui.StatusViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Status Saver", appName)
    }

    @Test
    fun `demo statuses provider produces items`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val statuses = DemoStatusProvider.getDemoStatuses(context)
        assertTrue(statuses.isNotEmpty())
        assertTrue(statuses.any { it.isVideo })
        assertTrue(statuses.any { !it.isVideo })
    }

    @Test
    fun `view model handles tab selection and multi selection`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StatusViewModel(app)

        vm.selectTab(AppTab.SAVED)
        assertEquals(AppTab.SAVED, vm.uiState.value.currentTab)

        val sampleStatuses = listOf(
            StatusItem(id = "item_1", uri = Uri.parse("content://media/1"), name = "sample_1.jpg", isVideo = false),
            StatusItem(id = "item_2", uri = Uri.parse("content://media/2"), name = "sample_2.mp4", isVideo = true)
        )
        vm.selectAll(sampleStatuses)
        assertTrue(vm.uiState.value.isMultiSelectMode)
        assertEquals(2, vm.uiState.value.selectedIds.size)

        vm.clearSelection()
        assertFalse(vm.uiState.value.isMultiSelectMode)
        assertTrue(vm.uiState.value.selectedIds.isEmpty())
    }

    @Test
    fun `demo statuses toggle persists state`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StatusViewModel(app)
        val initial = vm.uiState.value.includeDemo
        vm.toggleDemoStatuses()
        assertEquals(!initial, vm.uiState.value.includeDemo)
    }
}
