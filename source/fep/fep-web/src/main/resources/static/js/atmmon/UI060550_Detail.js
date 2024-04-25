var formId = "form-validator";

$(document).ready(function() {
	// 按下查詢按鈕
	var btnQuery = $('#btnQuery');
	if (btnQuery.length > 0) {
		btnQuery.click(function() {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		});
	}
})