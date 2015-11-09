
# DeepaMehta 4 Sign-up

This plugin provides a simple and configurable `login` and `registration` dialog for DeepaMehta 4. Furthermore account creation is built around a simple Email based confirmation workflow.

This plugin adds:
*    A `Sign-up`-link next to the `Login`-button in the DeepaMehta 4 Webclient
*    A `Sign-up Configuration` topic associated to the `DeepaMehta 4 Sign up` Plugin
     (part of the "System" workspace and thus editable by `admin`)

The special features of the `registration` dialog is comprised of:
*    Username existence check
*    Email existence check
*    Simple GUI-Notification mechanism
*    Minimal CSS Definition

The special features of the `login` dialog is comprised of:
*    Simple GUI-Notification mechanism
*    Automatic redirect
*    Minimal CSS Definition

The special logic of this plugin is comprised of:
*    Sends confirmation mail with token to the users registering Email addresss
*    Sends notification to admin after a new user account was sucessfully created 
*    Confirmation tokens (which are not persisted & get lost after a bundle/system restart) are valid for 60mins

## Requirements

DeepaMehta 4 is a platform for collaboration and knowledge management.
https://github.com/jri/deepamehta

To be able to install this module you first and additionally have to install the following DeepaMehta 4 Plugins.

*    `dm47-webactivator-0.4.6`-Bundle - Build from [sources](https://github.com/jri/dm4-webactivator)

For the plugins mailbox validation process to run you must install this plugin with deepamehta4 on a web server with a 
`postfix` -> `Internet Site` like mail send functionality.

## Download & Installation

You can find the latest stable version of this plugin bundled for download at [http://download.deepamehta.de/nightly/](http://download.deepamehta.de/nightly/).

As mentioned above, you currently need to download and install the aditonally required `dm47-webactivator-0.4
.6`-Bundle, too.

After downloading the two bundle-files, place them in the `bundles` folder of your DeepaMehta installation and restart 
DeepaMehta 4.

## Usage & Configuration

The central topic for configuring the sign-up dialog for your DeepaMehta 4 installation is of type `Sign-up 
Configuration`. Editing this topic via your dm4-webclient allows you to interactively control/adapt the following 
parameters:

*    all text-messages, hyperlinks, titles, logo, css, read more url of the login page
*    the terms of service and privacy policy (including checkbox labels) used in the sign-up form
*    the page footer (HTML) of all pages
*    the path to a custom image file as logo
*    the path to a custom CSS file replacing the default style
*    the mailbox emails are sent from
*    the administrators mailbox where the system sends notifications to

This configuration topic is associated with the "Plugin" topic representing this plugin.

## Licensed under the GPL License 3.0

GPL v3 - https://www.gnu.org/licenses/gpl.html

# Version History

1.1, UPCOMING
- Email confirmation is now mandatory, confirmation mails are send out
  (this requires a 'postfix' -> 'Internet Site' like web server setup)
- Adapted createUserAccounts to new ACL-Service (remove join Workspace) - no Workspace cookie
- Maintain configuration topic editble (migrate from postInstall to Migration)
- Added Email notification to admin on account creation
- Updated module to be compatible with DeepaMehta 4.7
- Incorporate some dm4-mail low-level functionality
- Updated dependency to bundle dm47-webactivator-0.4.6
- This plugin is not compatible with previous installations of the dm4-sign-up module

1.0.0, Dec 25, 2014

- configurable by end-users
- compatible with 4.4
- feature complete

-------------------------------
Author: Malte Reißig, 2013-2015

