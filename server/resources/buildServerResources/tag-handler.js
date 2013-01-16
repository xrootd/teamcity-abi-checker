var TagHandler = {
    requestTags: function () {
        var buildType = jQuery("#ui-abi-checker-build-type option:selected").val();

        BS.ajaxRequest("/requestTags.html", {
            parameters: 'buildTypeId=' + buildType,
            onComplete: function (transport) {
                if (transport.responseXML) {
                    BS.XMLResponse.processErrors(transport.responseXML, {
                        onAbiCheckerProblemError: function (elem) {
                            alert(elem.firstChild.nodeValue);
                        }
                    });
                    TagHandler.fillTags(transport.responseText);
                }
            }
        });
        return false;
    },

    fillTags: function (tags) {
        var xmlDoc = jQuery.parseXML(tags);
        var xml = jQuery(xmlDoc);

        jQuery("#ui-abi-checker-reference-tag").empty();

        xml.find("tag").each(function () {
            tag = jQuery(this).text()
            option = "<option value='" + tag + "'>" + tag + "</option>";
            jQuery("#ui-abi-checker-reference-tag").append(option);
        });

        jQuery("#ui-abi-checker-reference-tag option:first-child").attr("selected", true);
    }
};

TagHandler.requestTags();