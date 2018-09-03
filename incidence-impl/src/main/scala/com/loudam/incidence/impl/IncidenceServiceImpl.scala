package com.loudam.incidence.impl

import java.io.File

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.loudam.incidence.api.{IncidenceMessage, IncidenceService, Location}
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.server.PlayServiceCall
import play.api.mvc._
import com.loudam.incidence.eventsourcing.{AddIncidence, GetIncidence, IncidenceEntity}
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.core.parsers.Multipart.{FileInfo, FilePartHandler}

import scala.concurrent.{ExecutionContext, Future}



class IncidenceServiceImpl(persistentEntityRegistry:PersistentEntityRegistry, controllerComponents: play.api.mvc.ControllerComponents)
                          (implicit exCtx: ExecutionContext) extends AbstractController(controllerComponents) with IncidenceService {

    override def getOneIncidence(incidentId : String): ServiceCall[NotUsed, IncidenceMessage] = ServiceCall { _ =>
//      ???
      val ref = persistentEntityRegistry.refFor[IncidenceEntity](incidentId)
      ref.ask(GetIncidence(incidentId))
    }

// These function get the files in request body and store it in the target folder
  private def fileHandler: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) => {

      val tempFile = {
        // create a temp file in the `target` folder
        val f = new java.io.File("./target/file-upload-data/uploads", filename).getAbsoluteFile
        // make sure the subfolders inside `target` exist.
        f.getParentFile.mkdirs()
        f
      }
      val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(tempFile.toPath)
      val acc: Accumulator[ByteString, IOResult] = Accumulator(sink)
      acc.map {
        case akka.stream.IOResult(bytesWriten, status) =>
          println(status.get)
          FilePart(partName, filename, contentType, tempFile)
      }
    }
}

  override def reportIncidence: ServiceCall[NotUsed, String] = PlayServiceCall { wrapCall =>
    // Need to use EssentialAction so that we can check the content type before we choose
    // a body parser
    EssentialAction { requestHeader =>
      val action = if (requestHeader.contentType == Option("application/json")) {
        // Just delegate straight to Lagom's logic
        wrapCall(ServiceCall { request =>
          // Do something
          val ref = persistentEntityRegistry.refFor[IncidenceEntity]("report")
          //  ref.ask(AddIncidence(title, description, Location(longitude.toDouble,latitude.toDouble), tags, fileSet))
             ref.ask(AddIncidence("","", "Fire on bridge", Location(12.76,17.986), Option(List("fire")), Option(List("Fire.jpg"))))
        })
      } else {
        // Otherwise, use the multipart/form-data logic
        Action.async(parse.multipartFormData(fileHandler)) { request =>
          val files = request.body.files.toList
                val data = request.body.dataParts

             val   fileSet = Option(for (d <- files) yield d.filename)
                val tags = Option(data.get("tags").toList.flatten)
                val description = data.get("description").get.mkString
                val longitude = data.get("longitude").get.mkString
                val latitude = data.get("latitude").get.mkString
                val title = data.get("title").get.mkString

          val ref = persistentEntityRegistry.refFor[IncidenceEntity](title)
          ref.ask(AddIncidence(incidentId="1",title, description, Location(longitude.toDouble,latitude.toDouble), tags, fileSet))
          Future.successful(Ok)
        }
      }
      action(requestHeader)
    }
  }
}
