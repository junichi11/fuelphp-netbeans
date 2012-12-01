#About

This NetBeans plugin provides support for FuelPHP Framework.

##Feature

- Badge Icon (Done)
- create a new FuelPHP Project from New Project Option(Done)
- create a file for auto completion(Done)
- Action : go to view , go to action (Done)
- Support for custom fuel directory name

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

### Custom fuel directory name

If you use "myfuel" name instead of "fuel" directory name, please set as the following.

1. Project properties > Framework > FuelPHP
2. fuel name : your fuel directoy name (e.g. myfuel)

## Important!
Don't clone to SD Card port from new project option!(Hang up...)

##License

CDDL-GPLv2 (see license file)