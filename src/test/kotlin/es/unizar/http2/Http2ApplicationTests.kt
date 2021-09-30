package es.unizar.http2

import org.eclipse.jetty.http.HttpFields
import org.eclipse.jetty.http.HttpURI
import org.eclipse.jetty.http.HttpVersion
import org.eclipse.jetty.http.MetaData
import org.eclipse.jetty.http2.api.Session
import org.eclipse.jetty.http2.api.Stream
import org.eclipse.jetty.http2.api.server.ServerSessionListener
import org.eclipse.jetty.http2.client.HTTP2Client
import org.eclipse.jetty.http2.frames.DataFrame
import org.eclipse.jetty.http2.frames.HeadersFrame
import org.eclipse.jetty.util.Callback
import org.eclipse.jetty.util.FuturePromise
import org.eclipse.jetty.util.Jetty
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit


@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Http2ApplicationTests {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun `check HTTP 2 support`() {
        var isHttp2 = false
        val client = HTTP2Client()
        client.start()

        // Connect to host
        val sessionPromise: FuturePromise<Session> = FuturePromise<Session>()
        client.connect(
            null,
            InetSocketAddress("localhost", port),
            ServerSessionListener.Adapter(),
            sessionPromise
        )

        // Obtain the client Session object
        val session: Session = sessionPromise.get(5, TimeUnit.SECONDS)

        // Prepare the HTTP request headers.
        val requestFields = HttpFields()
        requestFields.put("User-Agent", client.javaClass.name + "/" + Jetty.VERSION)
        // Prepare the HTTP request object.
        val request = MetaData.Request(
            "GET",
            HttpURI("https://localhost:$port/"),
            HttpVersion.HTTP_2,
            requestFields
        )
        // Create the HTTP/2 HEADERS frame representing the HTTP request.
        val headersFrame = HeadersFrame(request, null, true)

        // Prepare the listener to receive the HTTP response frames.
        val responseListener = object : Stream.Listener.Adapter() {
            override fun onHeaders(stream: Stream, frame: HeadersFrame) {
                with(frame.metaData) {
                    println("HTTP Version: $httpVersion")
                    isHttp2 = httpVersion.version == 20
                    fields.forEach { field ->
                        println("${field.header}: ${field.value}")
                    }
                }
            }
            override fun onData(stream: Stream, frame: DataFrame, callback: Callback) {
                val bytes = ByteArray(frame.data.remaining())
                frame.data.get(bytes)
                println("${bytes.size}: " + String(bytes))
                callback.succeeded()
            }
        }

        session.newStream(headersFrame, FuturePromise(), responseListener)

        Thread.sleep(TimeUnit.SECONDS.toMillis(5))

        client.stop()
        assertTrue(isHttp2)
    }
}
