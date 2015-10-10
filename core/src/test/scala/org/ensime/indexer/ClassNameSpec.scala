package org.ensime.indexer

import org.scalatest._

class ClassNameSpec extends WordSpec with Matchers {
  "ClassName" should {
    "remove \"package\" from FQNs" in {
      ClassName(PackageName(List("org", "example")), s"package$$Class").fqnString shouldBe "org.example.Class"
      ClassName(PackageName(List("org", "example")), "package$Class$Subclass").fqnString shouldBe "org.example.Class$Subclass"
      ClassName(PackageName(List("org", "example", "package")), "Class").fqnString shouldBe "org.example.Class"
      ClassName(PackageName(List("org", "example", "package$")), "Class").fqnString shouldBe "org.example.Class"
    }

    "preserve the FQN of package objects" in {
      ClassName(PackageName(List("org.example")), "package").fqnString shouldBe "org.example.package$"
      ClassName(PackageName(List("org.example")), "package$").fqnString shouldBe "org.example.package$"
    }
  }
}
