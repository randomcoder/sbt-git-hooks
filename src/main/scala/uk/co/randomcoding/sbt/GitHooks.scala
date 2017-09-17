/*
 * Copyright (C) 2017. RandomCoder <randomcoder@randomcoding.co.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.randomcoding.sbt

import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Files, StandardCopyOption}

import sbt.Keys._
import sbt._

import scala.util.Properties

object GitHooks extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {

    val writeHooks = taskKey[Unit]("Write Git Hooks")
    val hooksSourceDir = settingKey[Option[File]]("Directory containing pre written git hooks")
    val gitHooksDir = settingKey[Option[File]]("Directory to write the git hooks to")

    lazy val gitHooksSettings: Seq[Def.Setting[_]] = Seq(
      hooksSourceDir in writeHooks := None,
      writeHooks := {
        val hooksSource = (hooksSourceDir in writeHooks).value.getOrElse(baseDirectory.value / "hooks")
        val targetDirectory = (gitHooksDir in writeHooks).value.getOrElse(baseDirectory.value / ".git" / "hooks")

        streams.value.log.info(s"Writing hooks from $hooksSource to $targetDirectory")
        WriteGitHooks(hooksSource, targetDirectory)
        streams.value.log.info(s"Successfully written hooks from $hooksSource to $targetDirectory")
      }
    )
  }

  import autoImport._
  override val projectSettings: Seq[Setting[_]] = inConfig(Compile)(gitHooksSettings)
}

object WriteGitHooks {

  def apply(hooksSourceDir: File, hooksTargetDir: File): Unit = {
    hooksSourceDir.listFiles.foreach { hook =>
      val hookTarget = hooksTargetDir.toPath.resolve(hook.getName)
      Files.copy(hook.toPath, hookTarget, StandardCopyOption.REPLACE_EXISTING)
      if (!Properties.isWin) Files.setPosixFilePermissions(hookTarget, PosixFilePermissions.fromString("rwxr-xr-x"))
    }
  }
}
