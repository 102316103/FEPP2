var formId = "form-validator";

$(document).ready(function() {
	initDatePicker('feptxnTbsdyFisc');
	initDatePicker('feptxnTxDate');
	initTimePicker('feptxnTxTimeBegin');
	initTimePicker('feptxnTxTimeEnd');
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});	
	// Grid中第一列查詢按鈕
	$('.a-inquiry').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060550/inquiryDetail', form);
	});
	// 建立表單驗證
	var validatorOption = getValidFormOptinal({
		rules: {
			feptxnTbsdyFisc: {
				required: true,
				dateISO: true,
			},
			feptxnTxDate: {
				dateISO: true,
			},
			feptxnEjfno: {
				digits: true
			},
			feptxnTraceEjfno: {
				digits: true
			},
			feptxnTxAmt: {
				digits: true
			},
		}
	});
	validatorOption = addAndGetDateLessEqualValidator(formId, 'feptxnTxTimeBegin', 'feptxnTxTimeEnd', '交易時間起不可大於交易時間訖', validatorOption);
	$('#' + formId).validate(validatorOption);
})