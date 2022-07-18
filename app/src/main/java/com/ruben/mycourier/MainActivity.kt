package com.ruben.mycourier

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gojek.courier.QoS
import com.gojek.courier.logging.ILogger
import com.gojek.mqtt.auth.Authenticator
import com.gojek.mqtt.client.MqttClient
import com.gojek.mqtt.client.config.PersistenceOptions
import com.gojek.mqtt.client.config.v3.MqttV3Configuration
import com.gojek.mqtt.client.factory.MqttClientFactory
import com.gojek.mqtt.client.listener.MessageListener
import com.gojek.mqtt.client.model.MqttMessage
import com.gojek.mqtt.event.EventHandler
import com.gojek.mqtt.event.MqttEvent
import com.gojek.mqtt.exception.handler.v3.AuthFailureHandler
import com.gojek.mqtt.model.KeepAlive
import com.gojek.mqtt.model.MqttConnectOptions
import com.gojek.mqtt.model.ServerUri
import com.gojek.workmanager.pingsender.WorkManagerPingSenderConfig
import com.gojek.workmanager.pingsender.WorkPingSenderFactory
import com.ruben.mycourier.ui.theme.MyCourierTheme

class MainActivity : ComponentActivity() {

    private var mqttClient: MqttClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCourierTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), lines = 1)
                }
            }
        }

        setup()
    }

    private fun setup() {
        val connectOptions = MqttConnectOptions(
            serverUris = listOf(ServerUri("broker.hivemq.com", 1883, "tcp")),
            clientId = "ruben_test",
            username = "",
            keepAlive = KeepAlive(
                timeSeconds = 30
            ),
            isCleanSession = false,
            password = ""
        )

        mqttClient = MqttClientFactory.create(
            context = this,
            mqttConfiguration = MqttV3Configuration(
                authenticator = object : Authenticator {
                    override fun authenticate(
                        connectOptions: MqttConnectOptions,
                        forceRefresh: Boolean
                    ): MqttConnectOptions {
                        return connectOptions
                    }
                },
                pingSender = WorkPingSenderFactory.createMqttPingSender(applicationContext, WorkManagerPingSenderConfig()),
                logger = object : ILogger {
                    override fun d(tag: String, msg: String) {
                        Log.d("MyCourier", "tag: $tag, message: $msg")
                    }

                    override fun d(tag: String, msg: String, tr: Throwable) {
                        Log.d("MyCourier", "tag: $tag, message: $msg, error: ${tr.message}")
                    }

                    override fun e(tag: String, msg: String) {
                        Log.d("MyCourier", "tag: $tag, message: $msg")
                    }

                    override fun e(tag: String, msg: String, tr: Throwable) {
                        Log.d("MyCourier", "tag: $tag, message: $msg, error: ${tr.message}")
                    }

                    override fun i(tag: String, msg: String) {
                        Log.d("MyCourier", "tag: $tag, message: $msg")
                    }

                    override fun i(tag: String, msg: String, tr: Throwable) {
                        Log.d("MyCourier", "tag: $tag, message: $msg, error: ${tr.message}")
                    }

                    override fun v(tag: String, msg: String) {
                        Log.d("MyCourier", "tag: $tag, message: $msg")
                    }

                    override fun v(tag: String, msg: String, tr: Throwable) {
                        Log.d("MyCourier", "tag: $tag, message: $msg, error: ${tr.message}")
                    }

                    override fun w(tag: String, msg: String) {
                        Log.d("MyCourier", "tag: $tag, message: $msg")
                    }

                    override fun w(tag: String, msg: String, tr: Throwable) {
                        Log.d("MyCourier", "tag: $tag, message: $msg, error: ${tr.message}")
                    }

                    override fun w(tag: String, tr: Throwable) {
                        Log.d("MyCourier", "tag: $tag, error: ${tr.message}")
                    }
                },
                eventHandler = object : EventHandler {
                    override fun onEvent(mqttEvent: MqttEvent) {
                        super.onEvent(mqttEvent)

                        if (mqttEvent is MqttEvent.MqttConnectSuccessEvent) {
                            subscribeMqtt()
                        }
                    }
                },
                persistenceOptions = PersistenceOptions.PahoPersistenceOptions(bufferCapacity = 0),
                authFailureHandler = object : AuthFailureHandler {
                    override fun handleAuthFailure() {
                        //do nothing
                    }
                }
            )
        )

        mqttClient?.connect(connectOptions)

        mqttClient?.addGlobalMessageListener(listener = object : MessageListener {
            override fun onMessageReceived(mqttMessage: MqttMessage) {
                Log.d("MyCourier", "message ${mqttMessage.topic}")
            }

        })
    }

    private fun subscribeMqtt() {
        mqttClient?.subscribe("#" to QoS.ONE)
    }
}

@Composable
fun Greeting(modifier: Modifier, lines: Int? = null) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = {
        TextView(context).apply {
            lines?.let {
                maxLines = it
                ellipsize = TextUtils.TruncateAt.END
            }
        }
    },
        update = {
            it.text = "ThisisaverylongtextandIamanidiotfordoingthisbutamioraminotwhoknowsman"
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyCourierTheme {
        Greeting(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp))
    }
}