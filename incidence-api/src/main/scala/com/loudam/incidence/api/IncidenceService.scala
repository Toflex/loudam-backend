package com.loudam.incidence.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall, ServiceAcl}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import play.api.libs.json.{Format, Json}
import  scala.collection.mutable.MutableList


trait IncidenceService extends Service{

// curl -X POST -F "file=@README.md" -F "file=@build.sbt"  -F "title=Fire on Otedola bridge" -F "description=Tanker bust out it flame" -F "tags=Fire" -F "tags=Tanker" -F "longitude=12.34" -F "latitude=90.232"  -v  http://localhost:54647/api/incidence/report
  def reportIncidence(): ServiceCall[NotUsed, String]

// curl http://localhost:9000/api/incidence/Tanker
  def getOneIncidence(incidentId : String): ServiceCall[NotUsed, IncidenceMessage]

// curl http://localhost:9000/api/incidence
  // def getAllIncidence : ServiceCall[NotUsed,IncidenceMessage]


  override final def descriptor: Descriptor = {
    import Service._
    named(name="incidence")
      .withCalls(
        restCall(Method.GET, pathPattern = "/api/incidents/:incidentId ", getOneIncidence _),
        // restCall(Method.GET, pathPattern = "/api/incidents", getAllIncidence),
        restCall(Method.POST, pathPattern = "/api/incidents", reportIncidence _)
      )
  }
}

case class Location(longitude:Double, latitude:Double)
object Location{
  implicit val format:Format[Location] = Json.format[Location]
}

case class IncidenceMessage(id:String,title:String, description:String, location:Location, tags: Option[List[String]], files:Option[List[String]])
object IncidenceMessage{
  implicit val format:Format[IncidenceMessage] = Json.format[IncidenceMessage]
}