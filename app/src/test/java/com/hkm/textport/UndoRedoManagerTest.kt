package com.hkm.textport

import com.hkm.textport.ui.editor.UndoRedoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoRedoManagerTest {

    @Test
    fun testUndoAndRedo() {
        val manager = UndoRedoManager()
        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)

        manager.recordChange("Hello", "Hello World")
        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)

        val undid = manager.undo("Hello World")
        assertEquals("Hello", undid)
        assertFalse(manager.canUndo)
        assertTrue(manager.canRedo)

        val redid = manager.redo("Hello")
        assertEquals("Hello World", redid)
        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)
    }
}
