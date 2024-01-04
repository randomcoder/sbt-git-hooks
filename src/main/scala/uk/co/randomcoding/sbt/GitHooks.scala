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
import sbt.internal.util.ManagedLogger

import scala.sys.process.Process
import scala.util.Properties

object GitHooks extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {

    val writeHooks = taskKey[Unit]("Write Git Hooks")
    val hooksSourceDir = settingKey[Option[File]]("Directory containing pre written git hooks")
    val gitHooksDir = settingKey[Option[File]]("Directory to write the git hooks to")
  }

  import autoImport._

  lazy val writeHooksTask = Def.task  {
    val hooksSource = hooksSourceDir.value.getOrElse(file("git-hooks"))
    val targetDirectory = gitHooksDir.value.getOrElse(file(Process("git rev-parse --git-path hooks").!!))

    WriteGitHooks(hooksSource, targetDirectory, streams.value.log)
  }

  lazy val gitHooksSettings: Seq[Def.Setting[_]] = Seq(
    hooksSourceDir := None,
    gitHooksDir := None,
    writeHooks := writeHooksTask.value
  )

  override val globalSettings: Seq[Setting[_]] = gitHooksSettings
}

object WriteGitHooks {

  def apply(hooksSourceDir: File, hooksTargetDir: File, log: ManagedLogger): Unit = {
    if (hooksTargetDir.exists()) {
      log.info(s"Copying hooks from ${hooksSourceDir.getAbsolutePath} into ${hooksTargetDir.getAbsolutePath}")
      Option(hooksSourceDir.listFiles).map(_.toList).getOrElse(Nil).foreach { hook =>
        val hookTarget = hooksTargetDir.toPath.resolve(hook.getName)
        log.info(s"Copying ${hook.getName} to $hookTarget")
        Files.copy(hook.toPath, hookTarget, StandardCopyOption.REPLACE_EXISTING)
        if (!Properties.isWin) Files.setPosixFilePermissions(hookTarget, PosixFilePermissions.fromString("rwxr-xr-x"))
      }
    } else log.info(s"${hooksTargetDir.getPath} does not exist (possibly within a submodule). Not writing any hooks.")
  }
}
