var formId = "form-validator";

//上一筆資料
$("#btnPrev").bind('click', function() {
    var form = {
        "": "",
    };
    doFormSubmit("/atmmon/UI_060620_B/inquiryPrev",form)
})
//下一筆資料
$("#btnNext").bind('click', function() {
    var form = {
        "": "",
    };
    doFormSubmit("/atmmon/UI_060620_B/inquiryNext",form)
})

//更新對照檔
$(document).ready(function () {
    // 更新對照檔
    $('#btnUpdate').click(function() {
        var chbNotify = 0;
        if($('#chbNotify').prop('checked')){
            chbNotify = 1;
        }
        var jsonData = {
            chbNotify: chbNotify,
            description: $("#description").val(),
            remark: $("#remark").val(),
            responsible: $("#responsible").val(),
            notifyMail: $("#notifyMail").val(),
            action: $("#action").val(),
        };
        doAjax(jsonData, "/atmmon/UI_060620_B/updateDetail", false, true, function(resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);
            }
        });
    });
})


