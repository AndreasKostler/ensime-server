// Copyright (C) 2016 ENSIME Authors
// License: GPL 3.0
package org.ensime.util

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import org.scalatest._
import org.scalatest.time._
import org.scalatest.concurrent.Eventually
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import scala.concurrent.duration._

/**
 * Boilerplate remover and preferred testing style in ENSIME.
 */
trait EnsimeSpec extends FlatSpec with Matchers with Inside with Retries with Eventually {
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()
  val log = LoggerFactory.getLogger(this.getClass)

  private val akkaTimeout: Duration = ConfigFactory.load().getDuration("akka.test.default-timeout", TimeUnit.MILLISECONDS).milliseconds
  override val spanScaleFactor: Double = ConfigFactory.load().getDouble("akka.test.timefactor")
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(akkaTimeout),
    interval = scaled(Span(5, Millis))
  )

  // taggedAs(org.scalatest.tagobject.Retryable)
  // will be retried (don't abuse it)
  override def withFixture(test: NoArgTest) = {
    if (isRetryable(test)) withRetry { super.withFixture(test) }
    else super.withFixture(test)
  }

}
