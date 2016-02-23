
    var EMPTY_STRING = ""
    var OK_STRING = "OK"



    // --- Plain DeepaMehta 4 login method used by "/sign-up/login" page. --- //

    function doLogout() {
        xhr = new XMLHttpRequest()
        xhr.onload = function(e) {
            if (xhr.response === "") {
                renderFriendlyMessage('Sie werden abgemeldet...') // ## Todo: Configurable Message
                window.document.location.reload()
            } else {
                renderWarning(xhr.response)
            }
        }
        xhr.open("POST", "/accesscontrol/logout", false)
        xhr.send()
    }

    function doLogin() {

        var id = document.getElementById("username").value
        var secret = document.getElementById("password").value

        checkAuthorization(id, secret)

        function checkAuthorization(id, secret) {

            var authorization = authorization()
            if (authorization === undefined) return null

            xhr = new XMLHttpRequest()
            xhr.onload = function(e) {
                if (xhr.response === "") {
                    renderFriendlyMessage('Lade Anwendung...') // ## Todo: Configurable Message
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


    // --- Plain JavaScript form

    function checkAPIAggrement() {
        console.log("Todo: Implement check if API Terms of use were accepted.")
        console.log("Todo: Immediately update Membership to the Reporting WS accordingly")
    }

    function saveAccountEdits() {
        console.log("Todo: Save edits to account information")
    }

    // --- Plain JavaScript form validation used by "/sign-up/" page. --- //

    // ### empty passwords and crazy mailbox-domains.
    // This is the form.onsubmit() implementation.
    function createAccount() {

        // any of these should prevent submission of form
        if (!isValidUsername()) return
        if (checkPassword() !== OK_STRING) return
        if (comparePasswords() !== OK_STRING) return
        if (checkMailbox() === null) return
        if (checkAgreements() !== OK_STRING) return

        var usernameVal = encodeURIComponent(document.getElementById("username").value)
        var mailbox = encodeURIComponent(document.getElementById("mailbox").value)
        var passwordVal = encodeURIComponent('-SHA256-' + SHA256(document.getElementById("pass-one").value))
        // employing the w3school way to go to a new resource
        window.document.location.assign("//" +  window.location.host + "/sign-up/handle/" + usernameVal + "/"
            + passwordVal +"/" + mailbox + "?no_workspace_assignment=true")

    }

    function isValidUsername() {
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

    function checkPassword() {
        var passwordField = document.getElementById("pass-one") // fixme: maybe its better to acces the form element
        if (passwordField.value.length <=7) {
            renderWarning("Your password must be at least 8 characters long.")
            disableSignupForm()
            return null
        }
        enableSignupForm()
        return OK_STRING
    }

    function checkMailbox() {
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

    function comparePasswords() {
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

    function disableSignupForm() {
        document.getElementById("create").setAttribute("disabled", "true")
        document.getElementById("create").setAttribute("style", "background-color: #a9a9a9;")
    }

    function enableSignupForm() {
        document.getElementById("create").removeAttribute("disabled")
        document.getElementById("create").removeAttribute("style")
    }

    function redirectToWebclientUI() {
        setTimeout(function (e) {
            window.location.href = '/kiezatlas/' // ### Todo: Make configurable
        }, 1500)
    }

