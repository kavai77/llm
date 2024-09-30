import {initializeApp} from "https://www.gstatic.com/firebasejs/10.13.1/firebase-app.js";
import {
    FacebookAuthProvider,
    getAuth,
    GoogleAuthProvider,
    signInWithPopup
} from "https://www.gstatic.com/firebasejs/10.13.1/firebase-auth.js";

const firebaseConfig = {
    apiKey: "AIzaSyArvJCZKBBFL9AVP1U32fRc5IdXoQrtlsE",
    authDomain: "sarcastic-llm.firebaseapp.com",
    projectId: "sarcastic-llm",
    storageBucket: "sarcastic-llm.appspot.com",
    messagingSenderId: "447406791921",
    appId: "1:447406791921:web:df58ecde16fc2346fedd50",
    measurementId: "G-B4TQ6JS8PH"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

export function initAuth(authRequired, githubNotSupported) {
    const authModal = authRequired
        ? new bootstrap.Modal('#authModal', {
            keyboard: false,
            backdrop: 'static',
            focus: true})
        : null;
    auth.onAuthStateChanged(function (user) {
        authStateObserver(user, authModal);
    });
    $("#logout").click(function () {
        auth.signOut();
    });
    if (authModal != null) {
        $("#googleLogin").click(function () {
            const provider = new GoogleAuthProvider();
            signInWithPopup(auth, provider);
        });
        $("#facebookLogin").click(function () {
            const provider = new FacebookAuthProvider();
            signInWithPopup(auth, provider);
        });
        $("#githubLogin").click(function () {
            alert(githubNotSupported);
            // const provider = new GithubAuthProvider();
            // signInWithPopup(auth, provider);
        });
    }
}

function authStateObserver(user, authModal) {
    if (user) { // User is signed in!
        user.getIdToken().then(function (accessToken) {
            if (authModal != null) {
                authModal.hide();
            }
            $("#loggedInUserContainer").show();
            $("#avatar").prop('src', user.photoURL)
                .prop('title', user.displayName)
                .tooltip('_fixTitle');
            setCookie("FirebaseAuth", accessToken, 1);
            $.ajax({
                url: '/llm/login',
                type: 'GET'
            });
        });
    } else { // User is signed out!
        $("#loggedInUserContainer").hide();
        if (authModal != null) {
            authModal.show();
        }
        eraseCookie("FirebaseAuth");
    }
}

function setCookie(name, value, hours) {
    var expires = "";
    if (hours) {
        var date = new Date();
        date.setTime(date.getTime() + (hours * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "") + expires + "; path=/;SameSite=Strict";
}

function eraseCookie(name) {
    document.cookie = name + '=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;SameSite=Strict';
}