package com.nd.sdp.common.utils

import javax.swing.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.dnd.DnDConstants
import java.awt.event.InputEvent

/**
 * 拖拽传输器
 */
class ValueExportTransferHandler(private val value: String?) : TransferHandler() {

    // Returns the Drag & Drop actions supported by the component provided when acting as drag source.
    override fun getSourceActions(c: JComponent): Int {
        return DnDConstants.ACTION_COPY_OR_MOVE
    }

    //Creates a Transferable that can be used to export data from the specified component.
    override fun createTransferable(c: JComponent): Transferable? {
        return StringSelection(value)
    }

    override fun exportAsDrag(comp: JComponent, e: InputEvent, action: Int) {
        super.exportAsDrag(comp, e, action)
    }

    // Called by the Drag & Drop system to complete the export of transferable data from the drag source component.
    override fun exportDone(source: JComponent?, data: Transferable?, action: Int) {
        super.exportDone(source, data, action)
    }

    companion object {
        val SUPPORTED_DATE_FLAVOR = DataFlavor.stringFlavor
    }
}
