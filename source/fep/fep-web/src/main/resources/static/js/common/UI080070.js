var formId = "form-validator";

function initTimePicker(id, defaultDate) {
	initDateTimePicker(id, 'HH:mm', defaultDate);
}

$(document).ready(function() {


	initDatePicker('logTimeBegin', new Date());
	initDatePicker('logTimeEnd', new Date());
	initTimePicker('logTimeBeginTime', new Date());
	initTimePicker('logTimeEndTime', new Date());
	
	// Grid中第一列查詢按鈕
	$('.a-inquiry').click(function() {
		if($(this).attr("data-audit") != '') {
			var value = $(this).attr("value");
			var form = jsonStringToObj(value);
			doFormSubmit('/common/UI_080070/inquiryDetail', form);
		}else {
			alert('無欄位輸入資料');
		}
	});

    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });

    $('#btnApply').click(function() {
        let form = {
            btnType: 'insert'
        }
        doFormSubmit('/common/UI_080070/showDetail', form);
    });

    $('.btn-inquiry').click(function() {
        let value = $(this).attr("value");
        let form = jsonStringToObj(value);
        doFormSubmit('/common/UI_080070/showDetail', form);
    });

        // 建立表單驗證
        var validatorOption = getValidFormOptinal({
            rules: {
                logTimeBegin: {
                    required: true
                },
                logTimeEnd: {
                    required: true
                }
            },
            messages: {
                logTimeBegin: {
                    required: "請輸入年月"
                },
                logTimeEnd: {
                    required: "請輸入年月"
                }
            }
        });
        validatorOption = addAndGetDateLessEqualValidator(formId, 'logTimeBegin', 'logTimeEnd', '起日不能大於訖日', validatorOption);
        validatorOption = addAndGetDateLessEqualValidator(formId, 'logTimeBeginTime', 'logTimeEndTime', '起始時間不能大於結束時間', validatorOption);
        $('#' + formId).validate(validatorOption);

})