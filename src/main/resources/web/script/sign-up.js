
	var EMPTY_STRING = ""
	var OK_STRING = "OK"

    function doLogin () {

        var id = document.getElementById("username").value
        var secret = document.getElementById("password").value

        checkAuthorization(id, secret)

        function checkAuthorization (id, secret) {

            var authorization = authorization()
            if (authorization === undefined) return null

            xhr = new XMLHttpRequest()
            xhr.onload = function(e) {
                if (xhr.response === "") {
                    console.log("Login successfull")
                    renderFriendlyMessage('Opening Webclient ...')
                    redirectToWebclientUI()
                } else {
                    renderWarning(xhr.response)
                }
            }
            xhr.open("POST", "/accesscontrol/login", false)
            xhr.setRequestHeader("Authorization", authorization)
            xhr.send()

            /** Returns value for the "Authorization" header. */
            function authorization() {
                return "Basic " + btoa(id + ":" + secret)   // ### FIXME: btoa() might not work in IE
            }
        }

    }

	// ### empty passwords
    // ###
    // form.onsubmit()-Implementation
	function createAccount() {

        checkUserName()
		if (checkPassword() !== OK_STRING) return
		if (comparePasswords() !== OK_STRING) return
        if (checkMailbox() == null) return
        if (checkAgreements() !== OK_STRING) return

        var username = encodeURIComponent(document.getElementById("username").value)
        var mailbox = encodeURIComponent(document.getElementById("mailbox").value)
        var password = encodeURIComponent('-SHA256-' + SHA256(document.getElementById("pass-one").value))

        // ### set default ws-topicid in cookie in dm4_workspace_id=
        xhr = new XMLHttpRequest()
        xhr.onload = function(e) {
            var username = xhr.response
            if (username.indexOf("<html>") != 0) {
                renderFriendlyMessage('Submitting ..')
                redirectToOK()
            } else {
                renderWarning("Account creation failed.")
            }
        }
        /** xhr.onerror = function (e) {
            renderWarning("Account creation failed." + e)
        } **/
        xhr.open("GET", "/sign-up/create/" + username + "/" + password + "/" + mailbox)
        xhr.setRequestHeader("Content-Type", "text/plain")
        xhr.send()

	}

	function checkUserName() {

		this.usernameInput = document.getElementById("username") // fixme: maybe its better to acces the form element

		var userInput = this.usernameInput.value
		if (!isValidUsername(userInput)) {
			renderWarning("This username would be invalid.")
			return null
		}
		xhr = new XMLHttpRequest()
		xhr.onload = function(e) {
			var response = JSON.parse(xhr.response)
			if (!response.isAvailable) {
				renderWarning("This username is already taken.")
                disableSignupForm()
				return null
			} else {
                enableSignupForm()
				renderWarning(EMPTY_STRING)
				return OK_STRING
			}
		}
		xhr.open("GET", "/sign-up/check/" + userInput, false)
		xhr.send()

		function isValidUsername(name) {
			if (name.length <= 1) {
                disableSignupForm()
                return false
            } else {
                enableSignupForm()
                return true
            }
		}

	}

	function checkPassword () {
		var passwordField = document.getElementById("pass-one") // fixme: maybe its better to acces the form element
		if (passwordField.value.length <=7) {
			renderWarning("Your password must be at least 8 characters long.")
            disableSignupForm()
			return null
		} else {
            enableSignupForm()
            return OK_STRING
        }
	}

	function checkMailbox () { // fixme:
		var mailboxField = document.getElementById("mailbox") // fixme: maybe its better to acces the form element
		if (mailboxField.value.indexOf("@") == -1 || mailboxField.value.indexOf(".") == -1) {
			renderWarning("This E-Mail Address would be invalid.")
            disableSignupForm()
			return null
		} else {
            enableSignupForm()
			renderWarning(EMPTY_STRING)
			return OK_STRING
		}
	}

	function comparePasswords () {
		var passwordFieldTwo = document.getElementById("pass-one") // fixme: maybe its better to acces the form element
		var passwordFieldOne = document.getElementById("pass-two") // fixme: maybe its better to acces the form element
		if (passwordFieldOne.value !== passwordFieldTwo.value) {
			renderWarning("Your passwords do not match.")
            disableSignupForm()
			return null
		} else if (passwordFieldOne.value === passwordFieldTwo.value) {
			renderWarning(EMPTY_STRING)
            enableSignupForm()
			checkPassword()
			return OK_STRING
		}
	}

    function checkAgreements() {
		var tosCheck = document.getElementById("toscheck").checked
		var privateOk = document.getElementById("privateinfo").checked
        //
        console.log("Checked....." + tosCheck + " and " + privateOk)
		if (tosCheck && privateOk) {
            renderWarning(EMPTY_STRING)
            enableSignupForm()
			return OK_STRING
		} else {
            renderWarning("First, please check our terms and conditions.")
            disableSignupForm()
			return null
		}
	}

    function showLabsPrivateText() {
        var textArea = document.getElementById('private-info')
            textArea.setAttribute("style", "display: block;")
    }

    function showLabsTermsText() {
        var textArea = document.getElementById('account-info')
            textArea.setAttribute("style", "display: block;")
    }

	function renderWarning(message) {
		var textNode = document.createTextNode(message)
		var messageElement = document.getElementById('message')
		while(messageElement.hasChildNodes()) {
			// looping over lastChild thx to http://stackoverflow.com/questions/5402525/remove-all-child-nodes
			messageElement.removeChild(messageElement.lastChild);
		}
		messageElement.appendChild(textNode)
	}

    function renderFriendlyMessage(message) {
		var textNode = document.createTextNode(message)
		var messageElement = document.getElementById('message-view')
		while(messageElement.hasChildNodes()) {
			// looping over lastChild thx to http://stackoverflow.com/questions/5402525/remove-all-child-nodes
			messageElement.removeChild(messageElement.lastChild);
		}
		messageElement.appendChild(textNode)
	}

    function disableSignupForm () {
        document.getElementById("create").setAttribute("disabled", "true")
        document.getElementById("create").setAttribute("style", "background-color: #a9a9a9;")
    }

    function enableSignupForm () {
        document.getElementById("create").removeAttribute("disabled")
        document.getElementById("create").removeAttribute("style")
    }

    function redirectToWebclientUI () {
        setTimeout(function (e) {
            window.location.href = '/de.deepamehta.webclient'
        }, 1500)
    }

    function redirectToOK () {
        setTimeout(function (e) {
            window.location.href = '/ok'
        }, 1500)
    }

