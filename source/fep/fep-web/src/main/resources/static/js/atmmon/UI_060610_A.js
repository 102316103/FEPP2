var formId = "form-validator";

$(document).ready(function() {
	// Grid中第一列查詢按鈕
	$('.a-inquiry').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060610_A/inquiryFeplogDetail', form);
	});
})