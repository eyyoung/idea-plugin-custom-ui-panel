package com.nd.sdp.common.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Created by Young on 2017/7/12.
 */
@State(name = "CustomUIConfig", storages = arrayOf(Storage("CustomUIConfig.xml")))
class CustomUIConfig : PersistentStateComponent<CustomUIConfig> {

    var xmlUrl: String? = DEFAULT_CONFIG
        set(xmlUrl) {
            var xmlUrl = xmlUrl
            if (xmlUrl == null || xmlUrl.isEmpty()) {
                xmlUrl = DEFAULT_CONFIG
            }
            field = xmlUrl
        }

    override fun getState(): CustomUIConfig? {
        return this
    }

    override fun loadState(customUIConfig: CustomUIConfig) {
        XmlSerializerUtil.copyBean(customUIConfig, this)
    }

    companion object {

        private val DEFAULT_CONFIG = "http://git.sdp.nd/common-ui/ui-panel-config/raw/master/config.xml"

        val instance: CustomUIConfig
            get() = ServiceManager.getService(CustomUIConfig::class.java)
    }
}
