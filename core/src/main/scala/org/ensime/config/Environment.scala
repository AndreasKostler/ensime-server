// Copyright: 2010 - 2016 https://github.com/ensime/ensime-server/graphs
// Licence: http://www.gnu.org/licenses/gpl-3.0.en.html
package org.ensime.config

import java.net.{ JarURLConnection, URL }

object Environment {
  def info: String = """
    |Environment:
    |  OS : %s
    |  Java : %s
    |  Scala : %s
    |  Ensime : %s
  """.trim.stripMargin.format(osVersion, javaVersion, scalaVersion, ensimeVersion)

  private def osVersion: String =
    System.getProperty("os.name")

  private def javaVersion: String = {
    val vmInfo = System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version")
    val rtInfo = System.getProperty("java.runtime.name") + " " + System.getProperty("java.runtime.version")
    vmInfo + ", " + rtInfo
  }

  private def scalaVersion: String =
    scala.util.Properties.versionString

  private def ensimeVersion: String =
    try {
      val pathToEnsimeJar = getClass.getProtectionDomain.getCodeSource.getLocation
      val ensimeJar = new URL("jar:" + pathToEnsimeJar.toString + "!/").openConnection().asInstanceOf[JarURLConnection].getJarFile
      ensimeJar.getManifest.getMainAttributes.getValue("Implementation-Version")
    } catch {
      case _: Exception => "unknown"
    }

  def shutdownOnDisconnectFlag: Boolean = {
    Option(System.getProperty("ensime.explode.on.disconnect")).isDefined
  }
}
