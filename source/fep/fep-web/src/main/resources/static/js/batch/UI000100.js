var formId = "form-validator";

$(document).ready(function() {

    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    });
    // Grid中第一列查詢按鈕
    $('.a-inquiry').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/batch/UI_000100/queryDetails', form);
    });

    $('#btnInsert').click(function() {
        var form = {
            "":"",
        }
        doFormSubmit('/batch/UI_000100/queryDetails', form);
    });

    $('#btnDelete').click(function() {
        showLoading(true);
        showProcessingMessage(true);
        $('#idForm').submit();
    });
})