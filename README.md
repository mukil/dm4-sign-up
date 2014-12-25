
# DeepaMehta 4 Sign-up

This plugin provides service provides a simple, AJAX and HTML-Template based, user-configurable `login` and `registration` dialog.

This plugin adds:
*    An `E-Mail Address` to each `User Account`   
     (Aggregation Definition, with a _one_ to _one_ relation, if not already present)
*    A `Sign-up`-link next to the `Login`-button in the DeepaMehta 4 Webclient
*    A `Sign-up Configuration` topic associated to the `DeepaMehta 4 Sign up` Plugin

The special features of the `registration` dialog is comprised of:
*    Username existence check
*    E-Mail Address existence and (simple) validation
*    Password length validation
*    Simple GUI-Notification mechanism
*    Minimal CSS Definition

The special features of the `login` dialog is comprised of:
*    Simple GUI-Notification mechanism
*    Automatic redirect
*    Minimal CSS Definition

## Requirements

DeepaMehta 4 is a platform for collaboration and knowledge management.
https://github.com/jri/deepamehta

To be able to install this module you first and additionally have to install the following DeepaMehta 4 Plugins.

*    `dm44-webactivator-0.4.4`-Bundle - [Source Code](https://github.com/jri/dm4-webactivator), [Nightly Build](http://download.deepamehta.de/nightly/)

## Download & Installation

You can find the latest stable version of this plugin bundled for download as `dm44-sign-up-X.Y.Z.jar-` at [http://download.deepamehta.de/nightly/](http://download.deepamehta.de/nightly/).

Additionally you need to download the required `dm44-webactivator-0.4.4`-Bundle - [Source Code](https://github.com/jri/dm4-webactivator), [Nightly Build](http://download.deepamehta.de/nightly/).

After downloading the bundle-files, place them in the `bundles` folder of your DeepaMehta installation and restart DeepaMehta.

## Usage & Configuration

The central topic for configuring the sign-up dialog for your DeepaMehta 4 installation is of type `Sign-up Configuration`. Editing and associating this topic allows you to interactively control/adapt:

*    all text-messages, hyperlinks, titles, logo, css, read more url of the login page
*    and additionally the terms of service and checkbox labels on the registration page
*    the page footer (HTML) of the login and registration page
*    the default workspace-membership assignment for new registrants 
     (simply `associate` your `Sign-up configuration` topic to one topic of type `Workspace` of your choice)
*    the path to a custom image file as logo
*    the path to a custom CSS file replacing the default style


## Licensed under the GPL License 3.0

GPL v3 - https://www.gnu.org/licenses/gpl.html

# Version History

1.0.0, Dec 25, 2014

- configurable by end-users
- compatible with 4.4
- feature complete

-------------------------------
Author: Malte Reißig, 2013-2014

