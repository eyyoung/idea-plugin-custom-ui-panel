package com.nd.sdp.common.model

/**
 * 控件Bean
 * Created by Young on 2017/7/3.
 */
class Widget {

    var name: String? = null
    var repository: String? = null
    var readme: String? = null
    var more: String? = null
    var image: String? = null
    var category: String? = null
    var xml: String? = null
    var java: String? = null
    var defaultType: String? = null
    var dependency: Dependency? = null
    var wiki: String? = null

    override fun toString(): String {
        return if (name == null) "" else name!!
    }

    fun getSearchInfo(): String {
        return name + readme + more + category
    }
}
