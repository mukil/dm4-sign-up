
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
                    setTimeout(function (e) {
                        window.location.href = '/de.deepamehta.webclient'
                    }, 1500)
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

	// fixme: empty passwords, disabled OK button if anything fails

	function createAccount() {

		if (checkPassword() !== OK_STRING) return
		if (comparePasswords() !== OK_STRING) return
        if (checkMailbox() == null) return

        var username = encodeURIComponent(document.getElementById("username").value)
        var mailbox = encodeURIComponent(document.getElementById("mailbox").value)
        var password = encodeURIComponent('-SHA256-' + SHA256(document.getElementById("pass-one").value))

		/** if (checkUserName(false) !== OK_STRING) return false
		if (checkPassword() !== OK_STRING) return false
		if (comparePasswords() !== OK_STRING) return false
		if (checkMailbox() !== OK_STRING) return false **/

        xhr = new XMLHttpRequest()
        xhr.onload = function(e) {
            // ### redirect to log-in form
            console.log(xhr.response)
        }
        xhr.open("GET", "/sign-up/create/" + username + "/" + password + "/" + mailbox)
        xhr.send()

	}

	function checkUserName(async) {

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
		xhr.open("GET", "/sign-up/check/" + userInput, async)
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
    }

    function enableSignupForm () {
        document.getElementById("create").removeAttribute("disabled")
    }
