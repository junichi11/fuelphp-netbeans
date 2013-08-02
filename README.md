#About

This NetBeans plugin provides support for FuelPHP Framework.

##Features

- Badge Icon
- create a new FuelPHP Project from New Project Option
- create a file for auto completion
- Action : go to view , go to action
- Smart Go To and some go to actions
- Support for custom fuel directory name
- Support for view path code completion
- MVC Node
- Support for running PHPUnit Test
- Hyperlink navigation
- Run Command Action
- Generate Action
- Default config option
- Save config as default action

## Install

- manually build and install NBM file
- download NBM(http://plugins.netbeans.org/plugin/44665/php-fuelphp-framework) file and manually install

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

## Smart Go To / Go To Actions

### Smart Go To

We can go to specific files relevant to a current file.

**[Ctrl + Shift + G] [S]** or **[Ctrl + J] [S]**

(from : to)

- controller : view, view model, test
- model : test
- view: controller
- view model : controller, test
- test : tested class

### Go To Actions

It is available the following actions:

- [Ctrl + Shift + G] [C] Controller 
- [Ctrl + Shift + G] [V] View
- [Ctrl + Shift + G] [W] View Model
- [Ctrl + Shift + G] [T] Test
- [Ctrl + Shift + G] [Ctrl + C] All Controllers
- [Ctrl + Shift + G] [Ctrl + M] All Models
- [Ctrl + Shift + G] [Ctrl + W] All View Models
- [Ctrl + Shift + G] [Ctrl + T] All Tests
- [Ctrl + Shift + G] [Ctrl + K] All Tasks
- [Ctrl + Shift + G] [Ctrl + I] All Configurations

### Keymap

If you have already used these shortcuts, please try to set shortcuts to keymap (Tools > Options).

## Custom fuel directory name

If you use "myfuel" name instead of "fuel" directory name, please set as the following.

1. Project properties > Framework > FuelPHP
2. fuel name : your fuel directoy name (e.g. myfuel)

### Notice
You can change only the fuel directory name. Many features don't work if other directory names are changed.

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

## PHPUnit Test Init Action
Project right-click > FuelPHP > PHPUnit Test Init

This action will create the necessary files to run PHPUnit in NetBeans.
And set them.

## Create Test Action
You can create Test Case files from context menu on file nodes or editor.

## Hyperlink Navigation
This feature is similar to Go To action.

1. Hold-down Ctrl key
2. wait until text color is changed to blue
3. click
4. go to file

e.g.
```php
// move cursor on 'bootstrap.css'
// 1. ... 4.
Asset::css('bootstrap.css');
```

This is valid for the following methods.

### Asset Class
- img
- css
- js

### View, ViewModel Class
- forge

### Option for creating file automatically
You can also use option for creating file automatically.
Please check `project properties > Framework > FuelPHP > create a file ...`

You can create a empty file when target files don't exisit if this option is valid.

## Run Command Action
You can run oil commands with IDE.

Right-click project > FuelPHP > Run Command or Shortcut key `Alt + Shift + X`

## Generate Action
You can run `oil generate` command with GUI.

Right-click project > FuelPHP > Generate

## Default Config Option

Tools > Options > PHP > FuelPHP  
We can set default config(app/conifg/config.php) to the Options.  
This is used when new project is created.

## Save Config As Default Action

This is able to set current project config to the Options.  
Right-click project > Fuel > save config as default

## Important!
Don't clone to SD Card port from new project option!(Hang up...)

##License

CDDL-GPLv2 (see license file)