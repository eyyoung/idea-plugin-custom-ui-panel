package com.nd.sdp.common.config;

import javax.swing.*;

/**
 * Created by Young on 2017/7/12.
 */
public class CustomUIConfigGUI {
    private JTextField tfConfigUrl;
    private JPanel rootPanel;
    private CustomUIConfig customUIConfig;

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JTextField getTfConfigUrl() {
        return tfConfigUrl;
    }

    public void setConfig(CustomUIConfig customUIConfig) {
        this.customUIConfig = customUIConfig;
        tfConfigUrl.setText(customUIConfig.getXmlUrl());
    }

    public boolean isModified() {
        boolean modified = false;
        modified |= !tfConfigUrl.getText().equals(customUIConfig.getXmlUrl());
        return modified;
    }

    public void apply() {
        customUIConfig.setXmlUrl(tfConfigUrl.getText());
    }

    public void reset() {
        tfConfigUrl.setText(customUIConfig.getXmlUrl());
    }
}
