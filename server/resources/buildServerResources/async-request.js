var TagRequester = {
    requestTags: function (buildType) {
        alert('called');
        BS.ajaxRequest($('abiCheckerForm').action, {
            parameters: 'buildType=' + buildType,
            onComplete: function (transport) {

                if (transport.responseXML) {
                    alert(transport.responseXML);
                    BS.XMLResponse.processErrors(transport.responseXML, {
                        onAbiCheckerProblemError: function (elem) {
                            alert(elem.firstChild.nodeValue);
                        }
                    });
                }
                alert("complete");
                $('abiCheckerComponent').refresh();
            }
        });
        return false;
    }
};