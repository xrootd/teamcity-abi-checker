jQuery(document).ready(function () {
    TagRequester.requestTags();
});

var TagRequester = {
    requestTags: function () {
        var buildType = jQuery("#${abiCheckerBean.buildTypeKey} option:selected").val();
        var buildTypeId = buildType.match(/\{id=(bt.*)\}/)[1];

        BS.ajaxRequest("/requestTags.html", {
            parameters: 'buildTypeId=' + buildTypeId,
            onComplete: function (transport) {
                if (transport.responseXML) {
                    BS.XMLResponse.processErrors(transport.responseXML, {
                        onAbiCheckerProblemError: function (elem) {
                            alert(elem);
                        }
                    });
                    TagRequester.fillTags(transport.responseText);
                }
            }
        });
        return false;
    },

    fillTags: function (tags) {
        var xmlDoc = jQuery.parseXML(tags);
        var xml = jQuery(xmlDoc);

        jQuery("#${abiCheckerBean.referenceTagKey}").empty();

        xml.find("tag").each(function () {
            tag = jQuery(this).text()
            option = "<option value='" + tag + "'>" + tag + "</option>";
            jQuery("#${abiCheckerBean.referenceTagKey}").append(option);
        });

        jQuery("#${abiCheckerBean.referenceTagKey} option:first-child").attr("selected", true);
    }
};