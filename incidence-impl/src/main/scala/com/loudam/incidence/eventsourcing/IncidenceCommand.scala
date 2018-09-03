package com.loudam.incidence.eventsourcing


import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.loudam.incidence.api._
import play.api.libs.json.{Format, Json}


//commands
sealed trait IncidenceCommand[R] extends ReplyType[R]

case class AddIncidence(incidentId:String,title:String, description:String, location:Location, tags: Option[List[String]], files:Option[List[String]]) extends IncidenceCommand[String]

object AddIncidence{
  implicit val format:Format[AddIncidence] = Json.format
}

case class GetIncidence(title:String) extends IncidenceCommand[IncidenceMessage]

object  GetIncidence{
  implicit val format:Format[GetIncidence] = Json.format
}

