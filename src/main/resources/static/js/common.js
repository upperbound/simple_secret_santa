function clickConfirm() {
    return clickConfirm('Continue?');
}

function clickConfirm(message) {
    return confirm(message);
}

function changeLanguage(lang) {
    let searchParams = new URLSearchParams(window.location.search);
    let currentLang = searchParams.get("lang");
    if (currentLang == null || currentLang === "") {
        currentLang = document.documentElement.lang;
    }
    if (lang == null || lang === "" || currentLang === lang)
        return;
    let url = window.location.protocol + '//' + window.location.host + window.location.pathname;
    searchParams.set("lang", lang);
    window.location.replace(url + '?' + searchParams.toString());
}

function inviteLinkToClipboard(groupPage, groupUuid) {
    let url = window.location.protocol + '//' + window.location.host + '/' + groupPage;
    let searchParams = new URLSearchParams();
    searchParams.set("groupUuid", groupUuid);
    let inviteUrl = url + '?' + searchParams.toString();
    navigator.clipboard.writeText(inviteUrl);
    window.alert(inviteUrl);
    return false;
}

function passwordsValidation(validationMessage) {
    $('input[equal-to-id]').bind('input', function() {
        let password_repeat = $(this);
        let password = $('#' + $(this).attr("equal-to-id"));
        if(password_repeat.val() === password.val())
            this.setCustomValidity('');
        else
            this.setCustomValidity(validationMessage);
    });
}

$(document).ready(
    function () {
        let langDropdown = document.getElementsByName("lang-radio");
        if (langDropdown != null) {
            let searchParams = new URLSearchParams(window.location.search);
            let currentLang = searchParams.get("lang");
            if (currentLang == null || currentLang === "") {
                currentLang = document.documentElement.lang;
            }
            let checked = false;
            for (let i = 0; i < langDropdown.length; i++) {
                let lang = langDropdown[i].getAttribute("value");
                if (currentLang === lang) {
                    langDropdown[i].checked = true;
                    checked = true;
                } else {
                    langDropdown[i].checked = false;
                }
            }
            if (!checked) {
                langDropdown[0].checked = true;
            }
            $('input[name="lang-radio"]').click(function() {
                changeLanguage($(this).attr("value"));
            });
        }
    }
);

