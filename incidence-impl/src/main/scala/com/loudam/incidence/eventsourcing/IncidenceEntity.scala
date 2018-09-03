package com.loudam.incidence.eventsourcing

import com.lightbend.lagom.scaladsl.persistence.{PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.loudam.incidence.api.{IncidenceMessage, Location}
import scala.collection.immutable.Seq

class IncidenceEntity extends PersistentEntity{
  override type Command = IncidenceCommand[_]
  override type Event = IncidenceEvent
  override type State = Incidence

  override def initialState: Incidence =  Incidence(incidentId="", title="", description="", Location(0,0), tags= Option(List.empty), files= None)

  override def behavior: Behavior = {

    case Incidence(incidentId,title, description, Location(longitude,latitude), tags, files) => Actions()
    
    .onCommand[AddIncidence, String] {

      case (AddIncidence(incidentId,title, description, Location(longitude,latitude), tags, files), ctx, state) =>
        ctx.thenPersist(
          IncidenceAdded(incidentId,title, description, Location(longitude,latitude), tags, files)
        ) { _ =>   ctx.reply("Incidence has been Submitted")     }
     }
    .onReadOnlyCommand[GetIncidence, IncidenceMessage] {
      case (GetIncidence(title), ctx, state) =>
        ctx.reply(IncidenceMessage(incidentId,title, description, Location(longitude,latitude), tags, files))}

    .onEvent {
      case (IncidenceAdded(incidentId,title, description, Location(longitude,latitude), tags, files), state) =>
        // update the current state
        Incidence(incidentId,title, description, Location(longitude,latitude), tags, files)
    }
  }

}




object IncidenceSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[AddIncidence],
    JsonSerializer[GetIncidence],
    JsonSerializer[IncidenceAdded],
    JsonSerializer[Incidence]
  )
}