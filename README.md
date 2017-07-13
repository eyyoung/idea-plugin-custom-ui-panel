# Custom UI Widget Panel

## Description

This plugin means to manage the Common UI preview and dependency<br/>
We use this plugin to introduce the style and Common Widget we now suppose to use in the Android development<br/>
Such as a button in specification style we want to use in all company,this plugin may lead to simplify the introduction work and also when our widget or style update,the dependency may also update,this plugin will auto manage the Gradle dependency<br/>

A config is used in this plugin<br/>
[Config.xml](https://github.com/eyyoung/idea-plugin-custom-ui-panel/blob/master/config.xml)<br/>
Config adress is configurable

## Preview

![Plugin Preview](https://raw.githubusercontent.com/eyyoung/idea-plugin-custom-ui-panel/master/preview/preview.gif)

## Config Sample

```xml
<config>
    <commonDep>
            <groupId>com.nd.sdp.android.common</groupId>
            <artifactId>res</artifactId>
            <version>0.2.6-release</version>
    </commonDep>
    <widgets>
        <widget>
            <name>Button</name>
            <dependency>
            </dependency>
            <repository>http://git.sdp.nd/component-android/sdp-common-res</repository>
            <category>Button</category>
            <readme>默认按钮</readme>
            <defaultType>xml</defaultType>
            <more></more>
            <image>http://git.sdp.nd/949177/common-ui-intellij-plugin/raw/master/images/button_1.png</image>
            <icon></icon>
            <xml>
                <![CDATA[<Button
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="${replace}"/>]]>
            </xml>
            <java></java>
        </widget>
    </widgets>
</config>
```

### commonDep

An general Dependency node,When the widget not config dependency,we suppose to add gradle dependency by this node

### widget

- name

The name suppose to display in the plugin

- dependency

The Gradle dependency of this widget to use,when node is null,use commonDep node

- repository

The widget maintain by which repo

- category

The widget suppose to display in which category

- readme

The simple introduce

- image

The image which will show on the plugin when widge is selected

- xml

The xml which can be added into the xml file
- java

Such as xml,but in java file
