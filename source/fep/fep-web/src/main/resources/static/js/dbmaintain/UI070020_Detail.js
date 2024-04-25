var formId = "form-validator";

$(document).ready(function() {
	
    initDatePicker('txtBSDAYS_DATE');
    initDatePicker('txtBSDAYS_NBSDY');
    initDatePicker('txtBSDAYS_ST_DATE_ATM');
    initDatePicker('txtBSDAYS_ST_DATE_RM');
    

	$('#btnInsert').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			removeAttrDisabled(formId);
			$('#' + formId).submit();
		}
	});
	
	// 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            BSDAYS_ZONE_CODEDdl: {
                required: true
            },
            txtBSDAYS_DATE: {
                required: true
            },
            lblBSDAYS_JDAY: {
                required: true
            },
            BSDAYS_WORKDAYDdl: {
				required: true
			},
			lblBSDAYS_WEEKNO: {
				required: true
			},
			txtBSDAYS_NBSDY: {
				required: true
			},
            txtBSDAYS_ST_DATE_ATM: {
				required: true
			},
			txtBSDAYS_ST_DATE_RM: {
				required: true
			}
            
        },
        messages: {
            BSDAYS_ZONE_CODEDdl: {
                required: "必須選擇"
            },
            txtBSDAYS_DATE: {
                required: "必需有資料"
            },
            lblBSDAYS_JDAY: {
                required: "必需有資料"
            },
            BSDAYS_WORKDAYDdl: {
				required: "必須選擇"
			},
			lblBSDAYS_WEEKNO: {
				required: "必需有資料"
			},
			txtBSDAYS_NBSDY: {
				required: "必需輸入"
			},
			txtBSDAYS_ST_DATE_ATM: {
				required: "必需輸入"
			},
			txtBSDAYS_ST_DATE_RM: {
				required: "必需輸入"
			}
        }
    }));
	

})