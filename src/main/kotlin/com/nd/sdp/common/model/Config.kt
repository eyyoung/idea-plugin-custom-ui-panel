package com.nd.sdp.common.model

import javax.xml.bind.annotation.XmlRootElement

/**
 * Java Bean
 * Created by Young on 2017/7/3.
 */
@XmlRootElement
class Config {

    var commonDep: Dependency? = null
    var widgets: Widgets? = null

}
