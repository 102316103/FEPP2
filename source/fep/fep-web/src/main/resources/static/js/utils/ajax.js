/**
 * 執行Ajax
 *
 * @param jsonData
 * @param url
 * @param isAsync
 * @param showProcessing
 * @param successFunc
 * @param errorFunc
 */
function doAjax(jsonData, url, isAsync, showProcessing, successFunc, errorFunc) {
    if (showProcessing) {
        showLoading(true);
        if ((typeof showProcessingMessage !== 'undefined')) {
            showProcessingMessage(true);
        }
        setTimeout(function () {
            $.ajax(getAjaxData(jsonData, url, isAsync, showProcessing, successFunc, errorFunc));
        }, 100)
    } else {
        $.ajax(getAjaxData(jsonData, url, isAsync, showProcessing, successFunc, errorFunc));
    }
}

/**
 * 執行表單ajax
 *
 * @param formId
 * @param url
 * @param isAsync
 * @param showProcessing
 * @param successFunc
 * @param errorFunc
 */
function doAjaxFormById(formId, url, isAsync, showProcessing, successFunc, errorFunc) {
    doAjaxForm($('#' + formId), url, isAsync, showProcessing, successFunc, errorFunc);
}

/**
 * 執行表單ajax
 *
 * @param form
 * @param url
 * @param isAsync
 * @param showProcessing
 * @param successFunc
 * @param errorFunc
 */
function doAjaxForm(form, url, isAsync, showProcessing, successFunc, errorFunc) {
    if (showProcessing) {
        showLoading(true);
        if ((typeof showProcessingMessage !== 'undefined')) {
            showProcessingMessage(true);
        }
        setTimeout(function () {
            $.ajax(getAjaxFormData(form, url, isAsync, showProcessing, successFunc, errorFunc));
        }, 100);
    } else {
        $.ajax(getAjaxFormData(form, url, isAsync, showProcessing, successFunc, errorFunc));
    }
}

/**
 * 獲取執行Ajax的json資料
 *
 * @param jsonData
 * @param url
 * @param isAsync
 * @param showProcessing
 * @param successFunc
 * @param errorFunc
 * @return
 */
function getAjaxData(jsonData, url, isAsync, showProcessing, successFunc, errorFunc) {
    var ajaxData =
        {
            async: isAsync,
            cache: false,
            type: "POST",
            dataType: 'json',
            url: ctx + url,
            contentType: 'application/json;charset=UTF-8',
            data: JSON.stringify(jsonData),
            success: function (result) {
                ajaxSuccess(showProcessing, result, successFunc);
            },
            error: function (xhr, statusText, thrownError) {
                ajaxError(showProcessing, errorFunc, xhr, statusText, thrownError);
            }
        };
    return ajaxData;
}

/**
 * 獲取ajax表單
 *
 * @param formId
 * @param url
 * @param isAsync
 * @param showProcessing
 * @param successFunc
 * @param errorFunc
 * @returns
 */
function getAjaxFormDataById(formId, url, isAsync, showProcessing, successFunc, errorFunc) {
    return getAjaxFormData($('#' + formId), url, isAsync, showProcessing, successFunc, errorFunc);
}

/**
 * 獲取ajax表單
 *
 * @param form
 * @param url
 * @param isAsync
 * @param showProcessing
 * @param successFunc
 * @param errorFunc
 * @returns
 */
function getAjaxFormData(form, url, isAsync, showProcessing, successFunc, errorFunc) {
    var ajaxData =
        {
            async: isAsync,
            cache: false,
            type: "POST",
            url: ctx + url,
            data: form.serialize(),
            success: function (result) {
                ajaxSuccess(showProcessing, result, successFunc);
            },
            error: function (xhr, statusText, thrownError) {
                ajaxError(showProcessing, errorFunc, xhr, statusText, thrownError);
            }
        };
    return ajaxData;
}

function ajaxSuccess(showProcessing, result, successFunc) {
    if (showProcessing) {
        if ((typeof showProcessingMessage !== 'undefined')) {
            showProcessingMessage(false);
        }
        showLoading(false);
    }
    if ('undefined' !== typeof result) {
        if (result.ajaxErr) {
            $("#error500AlertContent").html(result.message)
            showDangerAlert('error500Alert', '');
        } else if (result.isRedirect) {
            location.href = ctx + result.data;
        } else {
            successFunc(result);
        }
    } else {
        $("#error500AlertContent").html('result is undefined!!!');
        showDangerAlert('error500Alert', '');
    }
}

function ajaxError(showProcessing, errorFunc, xhr, statusText, thrownError) {
    if (showProcessing) {
        if ((typeof showProcessingMessage !== 'undefined')) {
            showProcessingMessage(false);
        }
        showLoading(false);
    }
    if (errorFunc == null) {
        location.href = ctx + '/error/500';
    } else {
        errorFunc(xhr, statusText, thrownError);
    }
}

/**
 * ajax檔案下載並彈出另存為dialog
 *
 * @param jsonData
 * @param url
 */
function doAjaxDownload(jsonData, url, errorFunc) {
    showLoading(true);
    if ((typeof showProcessingMessage !== 'undefined')) {
        showProcessingMessage(true);
    }
    setTimeout(function () {
        $.ajax({
            async: true,
            cache: false,
            type: "POST",
            url: ctx + url,
            data: JSON.stringify(jsonData),
            xhrFields: {
                responseType: 'blob'
            },
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Cache-Control', 'no-cache');
                xhr.setRequestHeader("Content-type", "application/json");
                xhr.setRequestHeader("Response-type", "blob");
                xhr.setRequestHeader('anti-csrf-token', token);
            },
            success: function (result, status, xhr) {
                var contentDisposition = xhr.getResponseHeader('Content-Disposition');
                var contentType = xhr.getResponseHeader('Content-Type');
                var blob = new Blob([result], {type: contentType});
                if (stringIsBlank(contentDisposition)) {
                    var reader = new FileReader();
                    reader.readAsText(blob, 'utf-8');
                    reader.onload = function (e) {
                        var data = jsonStringToObj(reader.result);
                        if (data.ajaxErr) {
                            $("#error500AlertContent").html(data.message)
                            showDangerAlert('error500Alert', '');
                        } else if (data.isRedirect) {
                            location.href = ctx + result.data;
                        } else {
                            showMessage(data.messageType, data.message);
                        }
                    }
                } else {
                    var fileName = decodeURIComponent(getFileNameFromContentDisposition(contentDisposition));
                    saveAs(blob, fileName);
                }
                showProcessingMessage(false);
                showLoading(false);
            },
            error: function (xhr, statusText, thrownError) {
                if ((typeof showProcessingMessage !== 'undefined')) {
                    showProcessingMessage(false);
                }
                showLoading(false);
                if (errorFunc == null) {
                    location.href = ctx + '/error/500';
                } else {
                    errorFunc(xhr, evt);
                }
            }
        });
    }, 100);
}