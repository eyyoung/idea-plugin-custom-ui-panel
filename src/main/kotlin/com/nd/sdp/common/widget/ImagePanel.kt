package com.nd.sdp.common.widget

import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.JPanel

class ImagePanel(url: String) : JPanel() {

    private var image: BufferedImage? = null

    init {
        try {
            val u = URL(url)
            image = ImageIO.read(u)
        } catch (ex: IOException) {
            // handle exception...
        }

    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(image, 0, 0, this) // see javadoc for more info on the parameters
    }

} 