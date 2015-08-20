
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

        // prevent submission of form
        if (!isValidUsername()) return
        if (checkPassword() !== OK_STRING) return
        if (comparePasswords() !== OK_STRING) return
        // if (checkMailbox() === null) return
        if (checkAgreements() !== OK_STRING) return

        var usernameVal = encodeURIComponent(document.getElementById("username").value)
        // var mailbox = encodeURIComponent(document.getElementById("mailbox").value)
        var passwordVal = encodeURIComponent('-SHA256-' + SHA256(document.getElementById("pass-one").value))
        // var password = encodeURIComponent(document.getElementById("pass-one").value)

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
        xhr.open("GET", "/sign-up/create/" + usernameVal + "/" + passwordVal + "?no_workspace_assignment=true")
        xhr.setRequestHeader("Content-Type", "text/plain")
        xhr.send()

    }

    function isValidUsername () {
        var usernameInput = document.getElementById("username") // fixme: maybe its better to acces the form element
        var userInput = usernameInput.value
        if (userInput.length <= 1) {
            renderWarning("This username would be invalid.")
            disableSignupForm()
            return false
        }
        enableSignupForm()
        return true
    }

    function checkUserNameAvailability() {

        var usernameInput = document.getElementById("username") // fixme: maybe its better to acces the form element
        var userInput = usernameInput.value
        
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

    }
    
    function checkMailboxAvailability() {
        var mailboxField = document.getElementById("mailbox") // fixme: maybe its better to acces the form element
        var mailBox = mailboxField.value
        xhr = new XMLHttpRequest()
        xhr.onload = function(e) {
            var response = JSON.parse(xhr.response)
            if (!response.isAvailable) {
                console.log("This mailbox is already registered")
                renderWarning("This E-Mail address is already registered.")
                disableSignupForm()
                return null
            } else {
                enableSignupForm()
                renderWarning(EMPTY_STRING)
                return OK_STRING
            }
        }
        xhr.open("GET", "/sign-up/check/mailbox/" + mailBox, false)
        xhr.send()

    }

    function checkPassword () {
        var passwordField = document.getElementById("pass-one") // fixme: maybe its better to acces the form element
        if (passwordField.value.length <=7) {
            renderWarning("Your password must be at least 8 characters long.")
            disableSignupForm()
            return null
        }
        enableSignupForm()
        return OK_STRING
    }

    function checkMailbox () {
        var mailboxField = document.getElementById("mailbox") // fixme: maybe its better to acces the form element
        if (mailboxField.value.indexOf("@") === -1 || mailboxField.value.indexOf(".") === -1) {
            renderWarning("This E-Mail Address would be invalid.")
            disableSignupForm()
            return null
        }
        enableSignupForm()
        renderWarning(EMPTY_STRING)
        return OK_STRING
    }

    function comparePasswords () {
        var passwordFieldTwo = document.getElementById("pass-one") // fixme: maybe its better to acces the form element
        var passwordFieldOne = document.getElementById("pass-two") // fixme: maybe its better to acces the form element
        if (passwordFieldOne.value !== passwordFieldTwo.value) {
            renderWarning("Your passwords do not match.")
            disableSignupForm()
            return null
        }
        renderWarning(EMPTY_STRING)
        enableSignupForm()
        checkPassword()
        return OK_STRING
    }

    function checkAgreements() {
        var tosCheck = document.getElementById("toscheck").checked
        var privateOk = document.getElementById("privateinfo").checked
        //
        if (tosCheck && privateOk) {
            renderWarning(EMPTY_STRING)
            enableSignupForm()
            return OK_STRING
        }
        renderWarning("First, please check our terms and conditions.")
        disableSignupForm()
        return null
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
