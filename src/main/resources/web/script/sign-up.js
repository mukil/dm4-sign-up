
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
		if (checkUsername() == null) return false
		if (checkPassword() == null) return false
		if (comparePasswords() == null) return false
		if (checkMailbox() == null) return false

        // todo: send GET request to '/sign-up/create/username/password/mailbox'

		return true
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
				return null
			} else {
				renderWarning(EMPTY_STRING)
				return OK_STRING
			}
		}
		xhr.open("GET", "/signup/check/" + userInput, true)
		xhr.send()

		function isValidUsername(name) {
			if (name.length <= 1) return false
			return true
		}

	}

	function checkPassword () {
		var passwordField = document.getElementById("pass-one") // fixme: maybe its better to acces the form element
		if (passwordField.value.length <= 11) {
			renderWarning("Your password must be at least 9 characters long.")
			return null
		}
	}

	function checkMailbox () { // fixme:
		var mailboxField = document.getElementById("mailbox") // fixme: maybe its better to acces the form element
		if (mailboxField.value.indexOf("@") == -1 || mailboxField.value.indexOf(".") == -1) {
			renderWarning("This E-Mail Address would be invalid.")
			return null
		} else {
			renderWarning(EMPTY_STRING)
			return OK_STRING
		}
	}

	function comparePasswords () {
		var passwordFieldTwo = document.getElementById("pass-one") // fixme: maybe its better to acces the form element
		var passwordFieldOne = document.getElementById("pass-two") // fixme: maybe its better to acces the form element
		if (passwordFieldOne.value !== passwordFieldTwo.value) {
			renderWarning("Your passwords do not match.")
			return null
		} else if (passwordFieldOne.value === passwordFieldTwo.value) {
			renderWarning(EMPTY_STRING)
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
