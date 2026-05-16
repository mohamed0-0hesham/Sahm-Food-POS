package com.coditria.footpos.core.navigation

import com.coditria.footpos.domain.model.OrderId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigatorTest {

    @Test
    fun `push then pop returns to previous`() {
        val nav = Navigator()
        nav.push(Destination.Stacked.About)
        assertEquals(1, nav.state.value.stacks[TabKey.Sell]?.size)
        assertTrue(nav.pop())
        assertEquals(0, nav.state.value.stacks[TabKey.Sell]?.size)
    }

    @Test
    fun `pop returns false when stack is empty`() {
        val nav = Navigator()
        assertFalse(nav.pop())
    }

    @Test
    fun `handleBack dismisses modal before popping the stack`() {
        val nav = Navigator()
        nav.push(Destination.Stacked.OrderDetail(OrderId("o1")))
        nav.showModal(Destination.Modal.SyncStatus)
        assertTrue(nav.handleBack())
        assertNull(nav.state.value.modal)
        // The stack entry is still there
        assertEquals(1, nav.state.value.stacks[TabKey.Sell]?.size)
    }

    @Test
    fun `each tab has its own stack`() {
        val nav = Navigator()
        nav.push(Destination.Stacked.About)
        nav.selectTab(TabKey.Orders)
        assertEquals(0, nav.state.value.stacks[TabKey.Orders]?.size)
        nav.push(Destination.Stacked.OrderDetail(OrderId("o1")))
        assertEquals(1, nav.state.value.stacks[TabKey.Orders]?.size)
        assertEquals(1, nav.state.value.stacks[TabKey.Sell]?.size)
    }
}
