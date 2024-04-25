var formId = "form-validator";
$(document).ready(function() {
    //明細頁儲存功能
    $('#btnChangeSave').click(function() {
        if (doValidateForm(formId)) {
	        $("#" + formId).find('input:disabled,select:disabled').removeAttr('disabled');
            showLoading(true);
            showProcessingMessage(true);
            //disabledField.prop('disabled', 'true');
            $('#' + formId).submit();
        }
    });
});