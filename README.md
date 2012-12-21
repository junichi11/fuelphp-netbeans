#About

This NetBeans plugin provides support for FuelPHP Framework.

##Feature

- Badge Icon (Done)
- create a new FuelPHP Project from New Project Option(Done)
- create a file for auto completion(Done)
- Action : go to view , go to action (Done)
- Support for custom fuel directory name
- Support for view path code completion
- MVC Node

## Install

- manually build and install NBM file
- download NBM(https://github.com/junichi11/fuelphp-netbeans/downloads) file and manually install

## Auto Code Completion

Create a new file for auto completion from Project Menu > FuelPHP > create auto completion file
It's added to nbproject directory.

c.f.
- https://gist.github.com/2364280 (kenjis)
- https://gist.github.com/4094832 (wate)

If create a project from new project, add it automatically.

## Go to view / action Action

### Go to view

- Right-click at the action method in the controller file.
- Navigate
- Go to view

### Go to action

- Right-click in the view or view model file.
- Navigate
- Go to action

## Custom fuel directory name

If you use "myfuel" name instead of "fuel" directory name, please set as the following.

1. Project properties > Framework > FuelPHP
2. fuel name : your fuel directoy name (e.g. myfuel)

## View path code completion
Plugin provides support for view path code completion.
e.g.

```php
View::forge('[Ctrl+Space]');
View::forge('welcome/[Ctrl+Space]');
ViewModel::forge('[Ctrl+Space]');
// popup directory and file names
```
If you push enter key at the directory name, please, run [Ctrl+Space] again. And directory childrenis displayed on popup list.

## MVC and modules Nodes
Create MVC(model, views, controller) and modules nodes on the project pane.
```
myproject
├─source files
├─test files
├─important files
├─include path
├─controller (fuel/app/classes/controller)
├─model (fuel/app/classes/model)
├─views (fuel/app/views)
├─modules (fuel/app/modules)

```

If you would like to hide the MVC directories in the source files,
please move to `Project properties > Framework > FuelPHP` and check "ignore..."

If you check "ignore...",
active position isn't moved to the source files node when you create a new file at the MVC nodes.
Otherwise go to there.

**Please notice that if you use remote upload on save, you can't ignore these.**

## Important!
Don't clone to SD Card port from new project option!(Hang up...)

##License

CDDL-GPLv2 (see license file)