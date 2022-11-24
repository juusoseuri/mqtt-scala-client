package client

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import io.circe._, io.circe.parser._, io.circe.generic.auto._
import java.nio.charset.StandardCharsets.UTF_8
import o1.*

object Client extends App {
  
  case class Data(light: Array[Int],temperature: Double, acceleration: Array[Double])

  def main(): Unit =
    //set variables
    val host = ???
    val username = ???
    val password = ???

    //establish the client
    val client: Mqtt5BlockingClient = MqttClient.builder.useMqttVersion5.serverHost(host).serverPort(8883).sslWithDefaultConfig.buildBlocking

    //connect to HiveMQ Cloud with TLS and username/pw
    client.connectWith.simpleAuth.username(username).password(UTF_8.encode(password)).applySimpleAuth.send

    println("Connected successfully")

    //subscribe to the topic "button/"
    client.subscribeWith.topicFilter("button/").send

    //set a callback that is called when a message is received (using the async API style)
    client.toAsync.publishes(ALL, (publish: Mqtt5Publish) => {
      //print results to console
      def parse(publish: Mqtt5Publish): Option[Data] =
        val result: String = UTF_8.decode(publish.getPayload.get()).toString
        val data = decode[Data](result)

        data match
          case Left(error) =>
            println(error.toString)
            None
          case Right(d) => Some(d)
      end parse

      val parsedResult = parse(publish)
      HandleData.handle(parsedResult)
    })

    def exit =
      println("Exiting and disconnecting client")
      client.disconnect()
      println("Disconnected")
    end exit

    sys.addShutdownHook(exit)
  end main

  main()

}