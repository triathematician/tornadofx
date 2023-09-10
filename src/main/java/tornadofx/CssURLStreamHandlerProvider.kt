package tornadofx

import sun.net.www.protocol.css.Handler
import java.net.spi.URLStreamHandlerProvider

/** CSS protocol provider implementation. */
class CssURLStreamHandlerProvider : URLStreamHandlerProvider() {
    override fun createURLStreamHandler(protocol: String?) =
        if ("css" == protocol) Handler() else null
}