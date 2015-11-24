package org.ensime.intg

import akka.event.slf4j.SLF4JLogging
import org.ensime.api._
import org.ensime.fixture._
import org.scalatest.{ Matchers, WordSpec }
import org.ensime.util.file._

// a pure java project, checking that how things behave without scala
class JavaWorkflow extends WordSpec with Matchers
    with IsolatedEnsimeConfigFixture
    with IsolatedTestKitFixture
    with IsolatedProjectFixture
    with SLF4JLogging {

  val original = EnsimeConfigFixture.JavaTestProject

  "ensime-server" should {
    "open the pure Java test project" in {
      withEnsimeConfig { implicit config =>
        withTestKit { implicit testkit =>
          withProject { (project, asyncHelper) =>
            import testkit._

            val sourceRoot = javaMain(config)
            val fooFile = sourceRoot / "pure/NoScalaHere.java"
            val fooFilePath = fooFile.getAbsolutePath

            project ! TypecheckFilesReq(List(fooFile))
            expectMsg(VoidResponse)

            project ! TypeAtPointReq(fooFile, OffsetRange(30))
            expectMsg(Some(BasicTypeInfo("pure.NoScalaHere", -1, DeclaredAs.Class, "pure.NoScalaHere", Nil, Nil, Some(EmptySourcePosition()), None)))
          }
        }
      }
    }
  }

}
