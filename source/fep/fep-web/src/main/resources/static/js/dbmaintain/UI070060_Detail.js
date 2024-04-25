var formId = "form-validator";
$(document).ready(function() {
	// 按下儲存按鈕
	$('#btnSaveBtn').click(function() {
		if (doValidateForm(formId)) {
			removeAttrDisabled(formId);
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
});