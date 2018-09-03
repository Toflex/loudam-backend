package com.loudam.incidence.impl

import com.loudam.incidence.api.IncidenceService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer}
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._
import com.loudam.incidence.eventsourcing._

class IncidenceLoader extends LagomApplicationLoader{

  override def load(context: LagomApplicationContext): LagomApplication =
  new IncidenceApplication(context) {
    override def serviceLocator: ServiceLocator = NoServiceLocator
  }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new IncidenceApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[IncidenceService])
}

abstract class IncidenceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with CassandraPersistenceComponents
  with LagomKafkaComponents
  with AhcWSComponents {
  override lazy val lagomServer = serverFor[IncidenceService](wire[IncidenceServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = IncidenceSerializerRegistry

  // Register the loudam persistent entity
  persistentEntityRegistry.register(wire[IncidenceEntity])


  // lazy val repository: IncidenceRepository = wire[IncidenceRepository]

  // // Register the lagom persistent read side processor persistent entity
  // readSide.register(wire[IncidenceProcessor])


}
