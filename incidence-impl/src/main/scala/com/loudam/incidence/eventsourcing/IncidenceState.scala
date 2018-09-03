package com.loudam.incidence.eventsourcing

import com.loudam.incidence.api.Location
import play.api.libs.json.{Format, Json}
import  scala.collection.mutable.MutableList

case class Incidence(incidentId:String,title:String, description:String, location:Location, tags: Option[List[String]], files:Option[List[String]])

object Incidence{
  implicit val format:Format[Incidence] = Json.format[Incidence]
}
