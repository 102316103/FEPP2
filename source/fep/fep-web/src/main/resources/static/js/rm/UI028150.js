var formId = "form-validator";

$(document).ready(function () {
    // 按下儲存按鈕
    $('#btnReserve').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            time: {
                required: true,
                number: true,
            },
        },
        messages: {
            time: {
                required: "畫面更新間隔時間不能為空",
                number: "請輸入數字",
            }
        }
    }));
})

// Ajax實現定時刷新頁面
function myrefresh() {
    if (doValidateForm(formId)) {
        showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    }
}

//指定refreshTime秒刷新一次
// 2024-04-25 Richard modified start for 【Client DoS By Sleep】
var refreshTime = document.getElementById("time").value;
if (refreshTime < 0 && refreshTime > 60) {
    refreshTime = 30;
}
// 2024-04-25 Richard modified end for 【Client DoS By Sleep】
setInterval(myrefresh, refreshTime * 1000);