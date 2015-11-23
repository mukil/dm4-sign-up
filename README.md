
# DeepaMehta 4 Sign-up

This plugin introduces a configurable registration process for DeepaMehta 4 _User Accounts_.

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
*    Optionally: Configure a sign-up process with an Email based confirmation workflow
*    Optionally: Sends confirmation mail with token to the users registering Email address
*    Optionally: Sends notification to admin after a new user account was sucessfully created
*    Optionally: If `new_accounts_are_enabled=true`, an account activation notice is sent

Note: If `Email Confirmation Required` is set to _true_ the confirmation tokens the system sends out are **not persisted** and get lost after a bundle/system restart. Once a token was send out the link containing it is valid for sixty minutes.

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
*    the flag `Email Confirmation Required` is there to decide whether to involve Emails at all

This configuration topic is associated with the "Plugin" topic representing this plugin.

## Licensed under the GPL License 3.0

GPL v3 - https://www.gnu.org/licenses/gpl.html

### TODOs

- Assign newly registered user account to a specific workspace by default (and configuration)
- Switch plugin configuration to the new dm4-config facility

## Version history

Note: These three are still open questions, it is yet unclear how (easy) we can solve this in detail.

1.1, Nov 23, 2015
- "Email Confirmation Required" is now a new configuration option:<br/>
  If `.. Required`, confirmation mails are send out including a token<br/>
  (valid for 60mins) and a link to proceed with the sign-up process<br/>
  Note: This option requires a 'postfix' -> 'Internet Site' like web server setup
- Further, if an "Admin Mailbox" is set, notifications on each account creation are sent to admin
- Updated sources to be compatible with DeepaMehta 4.7
- Updated dependency to bundle dm47-webactivator-0.4.6
- Included a few webpages which inform the user about the sign-up process
- If `new_accounts_are_enabled` is set to false, a notification is sent to the user when her
  account is `Enabled` by an administrator

Note: This plugin is not compatible with previous installations of the dm4-sign-up module.

1.0.0, Dec 25, 2014

- configurable by end-users
- compatible with 4.4
- feature complete

-------------------------------
Author: Malte Reißig, 2013-2015

