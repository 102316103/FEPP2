var formId = "form-validator";
$(document).ready(function () {
    initDateTimePicker1('transactDate', 'YYYY-MM-DD');
    initDateTimePicker1('feptxnTxTimeBegin', 'HH:mm:ss');
    initDateTimePicker1('feptxnTxTimeEnd', 'HH:mm:ss');
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (confirm('確定要以現有條件搜尋FEPLOG?')) {
            if (doValidateForm(formId)) {
                showLoading(true);
                showProcessingMessage(true);
                $('#' + formId).submit();
            }
        } else {
            alert('取消搜尋');
        }
    });
    // Grid中交易別超連結
    $('.ej').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/atmmon/UI_060610/bindGridDetail', form);
    });
    // 下載Log
    $('#btnDownload').click(function () {
        var formData = getFormData(formId);
        doAjaxDownload(formData, '/atmmon/UI_060610/download');
    });
	$('#server').change(function() {
		var form = {
            server:$('#server').val()
		}
		doAjax(form, "/atmmon/UI_060610/getSelectLog", false, true, function(resp) {
			$('#logType option').remove();
			$.each(resp, function(index, value) {
				$('#logType').append($('<option></option>').text(value).val(value));
			});
		});
	});

});

/**
 * 初始化日期時間選擇元件
 *
 * @param id
 * @param format
 * @param defaultDate
 */
function initDateTimePicker1(id, format, defaultDate) {
    $('#' + id).datetimepicker({
        format: format,
        locale: 'zh-tw',
        allowInputToggle: true,
        useStrict: true,
        toolbarPlacement: 'bottom',
        defaultDate: defaultDate,
        buttons: {
            showToday: true,
            showClear: true,
            showClose: true
        },
        icons: {
            today: 'fa fa-calendar-check'
        },
        tooltips: {
            today: '跳轉到當前日期/時刻',
            clear: '清除',
            close: '關閉面板',
            selectMonth: '選擇月份',
            prevMonth: '上一月',
            nextMonth: '下一月',
            selectYear: '選擇年',
            prevYear: '上一年',
            nextYear: '下一年',
            selectDecade: '選擇十年內',
            prevDecade: '上一個十年',
            nextDecade: '下一個十年',
            prevCentury: '上一個世紀',
            nextCentury: '下一個世紀',
            incrementHour: '增加時',
            pickHour: '選擇時',
            decrementHour: '減少時',
            incrementMinute: '增加分',
            pickMinute: '選擇分',
            decrementMinute: '減少分',
            incrementSecond: '增加秒',
            pickSecond: '選擇秒',
            decrementSecond: '減少秒'
        }
    });
}