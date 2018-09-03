 package com.loudam.incidence.eventsourcing

 import com.loudam.incidence.api._
 import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
 import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}

 import scala.concurrent.ExecutionContext

 class IncidenceProcessor(session: CassandraSession,incidenceRepository: IncidenceRepository, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
 extends ReadSideProcessor[IncidenceEvent] {

   override def buildHandler()={//: ReadSideProcessor.ReadSideHandler[IncidenceEvent] = {
       readSide.builder[IncidenceEvent]("IncidenceOffset")
       .setGlobalPrepare(incidenceRepository.createTable)
       .setPrepare(_ => incidenceRepository.createPreparedStatements)
       // .setEventHandler[IncidenceAdded](e ⇒ incidenceRepository.storeIncidence(e.event.IncidenceMessage))
       .build()
       }

   override def aggregateTags: Set[AggregateEventTag[IncidenceEvent]] = IncidenceEvent.Tag.allTags

 }
