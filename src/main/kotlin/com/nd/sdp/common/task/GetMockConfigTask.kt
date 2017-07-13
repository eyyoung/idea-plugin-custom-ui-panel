package com.nd.sdp.common.task

import com.google.gson.Gson
import com.nd.sdp.common.model.Config
import com.nd.sdp.common.model.Widget

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

/**
 * 获取配置任务
 * Created by Young on 2017/7/3.
 */
class GetMockConfigTask(private val callback: Callback<Config, Void>) : Runnable {

    override fun run() {
        try {
            val file = File("D:", "styles.xml")
            val jaxbContext = JAXBContext.newInstance(Config::class.java)
            val unmarshaller = jaxbContext.createUnmarshaller()
            val config = unmarshaller.unmarshal(file) as Config
            callback.call(config)
        } catch (e: JAXBException) {
            e.printStackTrace()
        }

    }
}
