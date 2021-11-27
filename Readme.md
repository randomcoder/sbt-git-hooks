# SBT Git Hooks

[![Build Status](https://travis-ci.org/randomcoder/sbt-git-hooks.svg?branch=master)](https://travis-ci.org/randomcoder/sbt-git-hooks)

SBT auto plugin to manage git hooks from within the project. This plugin will copy any files from the
`git-hooks` directory into `.git/hooks` and ensure they are executable.

See the git hooks [documentation](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks) for details on
the hooks that are available.

### How to use

`sbt-git-hooks` is an auto plugin for SBT 1.1.x

Add the plugin to your build with the following in `project/plugins.sbt`:

```
addSbtPlugin("uk.co.randomcoding" % "sbt-git-hooks" % "0.2.0")
```

Then run the task `writeHooks` to copy the hooks into `.git/hooks`.

**Note**: Everytime there are modifications to custom git hooks, you are expected to run this `writeHooks` sbt task. This requirement however could be automated by invoking this task on every sbt load. To do so add following code to `build.sbt`:
```scala
lazy val startupTransition: State => State = { s: State =>
  "writeHooks" :: s
}

onLoad in Global := {
  val old = (onLoad in Global).value
  startupTransition compose old
}
```

Now if you run `sbt` in your repository directory, `writeHooks` task will automically be called and your updated custom git hooks will be copied to `.git/hooks` out-of-the-box.

### Licence

This plugin is licenced under the [AGPL v3](https://www.gnu.org/licenses/agpl-3.0.en.html)

### Contributions

Contributions are welcome. Please create a fork and submit pull requests.
