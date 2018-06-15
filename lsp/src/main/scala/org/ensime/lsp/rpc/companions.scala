// Copyright: 2010 - 2017 https://github.com/ensime/ensime-server/graphs
// License: http://www.gnu.org/licenses/gpl-3.0.en.html
package org.ensime.lsp.rpc.companions

import org.ensime.lsp.rpc.messages._
import spray.json._

sealed trait RpcCompanionError {
  val describe: String
}

case object UnknownMethod extends RpcCompanionError {
  override val describe = "unknown method"
}
case object NoParams extends RpcCompanionError {
  override val describe = "parameters must be given"
}
case object NoNamedParams extends RpcCompanionError {
  override val describe = "named parameters must be given"
}
final case class OtherError(err: String) extends RpcCompanionError {
  override val describe = err
}

object RpcCompanionError {
  def apply(err: String): OtherError = OtherError(err)
}

final case class RpcCommand[A](method: String)(implicit val R: JsReader[A],
                                               val W: JsWriter[A])

trait CommandCompanion[A] {

  protected[this] val commands: Seq[RpcCommand[_ <: A]]

  def read(
    jsonRpcRequestMessage: JsonRpcRequestMessage
  ): Either[RpcCompanionError, _ <: A] = {

    def readObj(command: RpcCommand[_ <: A],
                obj: JsObject): Either[RpcCompanionError, _ <: A] =
      command.R.read(obj) match {
        case Left(invalid) => Left(RpcCompanionError(invalid.msg))
        case Right(valid)  => Right(valid)
      }

    commands.find(_.method == jsonRpcRequestMessage.method) match {
      case None => Left(UnknownMethod)
      case Some(command) =>
        jsonRpcRequestMessage.params match {
          case Some(ArrayParams(_))    => Left(NoNamedParams)
          case Some(ObjectParams(obj)) => readObj(command, obj)
          case None                    => readObj(command, JsObject.empty)
        }
    }
  }

  def write[B <: A](obj: B, id: CorrelationId)(
    implicit command: RpcCommand[B]
  ): JsonRpcRequestMessage = {
    val jsObj = command.W.write(obj) match {
      case o: JsObject => o
      case _ =>
        sys.error(s"Wrong format for command $obj. Should be a json object.")
    }

    JsonRpcRequestMessage(
      command.method,
      Params(jsObj),
      id
    )
  }
}

object RpcResponse {

  def read[A](
    jsonRpcResponseSuccessMessage: JsonRpcResponseSuccessMessage
  )(implicit format: JsReader[A]): Either[RpcCompanionError, A] =
    format.read(jsonRpcResponseSuccessMessage.result) match {
      case Left(invalid) => Left(RpcCompanionError(invalid.msg))
      case Right(valid)  => Right(valid)
    }

  def write[A](obj: A, id: CorrelationId)(
    implicit format: JsWriter[A]
  ): JsonRpcResponseSuccessMessage =
    JsonRpcResponseSuccessMessage(
      format.write(obj),
      id
    )
}

final case class RpcNotification[A](method: String)(
  implicit val R: JsReader[A],
  val W: JsWriter[A]
)

trait NotificationCompanion[A] {

  protected[this] val notifications: Seq[RpcNotification[_ <: A]]

  def read(
    jsonRpcNotificationMessage: JsonRpcNotificationMessage
  ): Either[RpcCompanionError, _ <: A] = {

    def readObj(command: RpcNotification[_ <: A],
                obj: JsObject): Either[RpcCompanionError, _ <: A] =
      command.R.read(obj) match {
        case Left(invalid) => Left(RpcCompanionError(invalid.msg))
        case Right(valid)  => Right(valid)
      }

    notifications.find(_.method == jsonRpcNotificationMessage.method) match {
      case None => Left(UnknownMethod)
      case Some(command) =>
        jsonRpcNotificationMessage.params match {
          case None if command.method == "exit" =>
            readObj(command, JsObject.empty)
          case None                    => Left(NoParams)
          case Some(ArrayParams(_))    => Left(NoNamedParams)
          case Some(ObjectParams(obj)) => readObj(command, obj)
        }
    }
  }

  def write[B <: A](
    obj: B
  )(implicit notification: RpcNotification[B]): JsonRpcNotificationMessage = {
    val jsObj = notification.W.write(obj) match {
      case o: JsObject => o
      case _ =>
        sys.error(s"Wrong format for command $obj. Should be a json object.")
    }

    JsonRpcNotificationMessage(
      notification.method,
      Params(jsObj)
    )
  }
}
