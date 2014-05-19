
	var EMPTY_STRING = ""
	var OK_STRING = "OK"

    function doLogin () {

        var id = document.getElementById("username")
        var secret = document.getElementById("password")

    }

	// fixme: empty passwords, disabled OK button if anything fails

	function createAccount() {
		if (checkUsername() == null) return false
		if (checkPassword() == null) return false
		if (comparePasswords() == null) return false
		if (checkMailbox() == null) return false

        // fixme: encrypt password before submission
        // todo: send GET request to '/sign-up/create/username/password/mailbox'

		return true
	}

	function checkUserName() {

		this.usernameInput = document.getElementById("username") // fixme: maybe its better to acces the form element

		var userInput = this.usernameInput.value
		if (!isValidUsername(userInput)) {
			renderNotification("This username would be invalid.")
			return null
		}

		xhr = new XMLHttpRequest()
		xhr.onload = function(e) {
			var response = JSON.parse(xhr.response)
			if (!response.isAvailable) {
				renderNotification("This username is already taken.")
				return null
			} else {
				renderNotification(EMPTY_STRING)
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
			renderNotification("Your password must be at least 9 characters long.")
			return null
		}
	}

	function checkMailbox () { // fixme:
		var mailboxField = document.getElementById("mailbox") // fixme: maybe its better to acces the form element
		if (mailboxField.value.indexOf("@") == -1 || mailboxField.value.indexOf(".") == -1) {
			renderNotification("This E-Mail Address would be invalid.")
			return null
		} else {
			renderNotification(EMPTY_STRING)
			return OK_STRING
		}
	}

	function comparePasswords () {
		var passwordFieldTwo = document.getElementById("pass-one") // fixme: maybe its better to acces the form element
		var passwordFieldOne = document.getElementById("pass-two") // fixme: maybe its better to acces the form element
		if (passwordFieldOne.value !== passwordFieldTwo.value) {
			renderNotification("Your passwords do not match.")
			return null
		} else if (passwordFieldOne.value === passwordFieldTwo.value) {
			renderNotification(EMPTY_STRING)
			checkPassword()
			return OK_STRING
		}
	}

	function renderNotification(message) {

		var textNode = document.createTextNode(message)
		var messageElement = document.getElementById('message')

		while(messageElement.hasChildNodes()) {
			// looping over lastChild thx to http://stackoverflow.com/questions/5402525/remove-all-child-nodes
			messageElement.removeChild(messageElement.lastChild);
		}

		messageElement.appendChild(textNode)

	}
