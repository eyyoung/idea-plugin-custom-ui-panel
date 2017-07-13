package com.nd.sdp.common.task

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.Messages
import com.nd.sdp.common.config.CustomUIConfig
import com.nd.sdp.common.model.Config
import java.net.URL
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException

/**
 * 获取配置任务
 * Created by Young on 2017/7/3.
 */
class GetRealConfigTask(private val callback: Callback<Config, Void>) : Runnable {

    override fun run() {
        try {
            val jaxbContext = JAXBContext.newInstance(Config::class.java)
            val unmarshaller = jaxbContext.createUnmarshaller()
            val service = ServiceManager.getService(CustomUIConfig::class.java)
            val config = unmarshaller.unmarshal(URL(service.xmlUrl)) as Config
            callback.call(config)
        } catch (e: JAXBException) {
            e.printStackTrace()
        } finally {
        }

    }
}
