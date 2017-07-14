package com.nd.sdp.common.config

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import org.jetbrains.annotations.Nls

import javax.swing.*

/**
 * Created by Young on 2017/7/12.
 */
class CustomUIConfigurable : SearchableConfigurable {

    private val customUIConfig: CustomUIConfig
    private var customUIConfigGUI: CustomUIConfigGUI? = null

    init {
        customUIConfig = CustomUIConfig.instance
    }

    override fun getId(): String {
        return "com.nd.sdp.common.CustomUIToolWindow"
    }

    @Nls
    override fun getDisplayName(): String {
        return "Custom UI Widget Panel"
    }

    override fun getHelpTopic(): String? {
        return "com.nd.sdp.common.CustomUIToolWindow"
    }

    override fun createComponent(): JComponent? {
        customUIConfigGUI = CustomUIConfigGUI()
        customUIConfigGUI!!.setConfig(customUIConfig)
        return customUIConfigGUI!!.rootPanel
    }

    override fun disposeUIResources() {
        customUIConfigGUI = null
    }

    override fun enableSearch(option: String?): Runnable? {
        return null
    }

    override fun isModified(): Boolean {
        return customUIConfigGUI!!.isModified
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        customUIConfigGUI!!.apply()
    }

    override fun reset() {
        customUIConfigGUI!!.reset()
    }
}
