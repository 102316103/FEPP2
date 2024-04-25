package com.syscom.safeaa.enums;

import org.apache.commons.lang3.StringUtils;

public enum SAFEMessageId {
    None(0),

    /**
     * 登入帳號尚在鎖定期間
     */
    AccountLockoutDuration(1),

    /**
     * 已超過登入帳號登入失敗次數，該帳戶已被鎖定
     */
    AccountLockoutThreshold(2),

    /**
     * 登入帳號長度超過限制
     */
    MaxAccountLength(3),

    /**
     * 登入帳號長度不足
     */
    MinAccountLength(4),

    /**
     * 登入帳號必須包含文字字元
     */
    AccountRequireAlphaNumeric(5),

    /**
     * 上次登入失敗，目前暫停登入
     */
    ResetAccountLockoutCounterAfter(6),

    /**
     * 密碼不得包含登入帳號
     */
    AlllowLogOnId(7),

    /**
     * 密碼不允許重複字元
     */
    AllowRepeatedCharacters(8),

    /**
     * 系統自動產生密碼失敗
     */
    AutoCreatePassword(9),

    /**
     * 新密碼不得與前幾次密碼相同
     */
    EnforcePasswordHistory(10),

    /**
     * 密碼使用期限已到，請變更密碼
     */
    MaxPasswordAge(11),

    /**
     * 密碼長度超過限制
     */
    MaxPasswordLength(12),

    /**
     * 密碼使用期限太短，不得變更
     */
    MinPasswordAge(13),

    /**
     * 密碼長度不足
     */
    MinPasswordLength(14),

    /**
     * 密碼必須符合複雜性需求
     */
    PasswordComplexity(15),

    /**
     * 密碼加密方式不得空白
     */
    PasswordEncryption(16),

    /**
     * 密碼必須包含文字字元
     */
    PasswordRequireAlphaNumeric(17),

    /**
     * 密碼必須包含特殊字元
     */
    RequireSpecialCharacters(18),

    /**
     * 首次登入或密碼重置後登入需變更密碼
     */
    FirstLogOnChangePassword(19),

    /**
     * 新密碼啟動期限已過，請重新申請密碼
     */
    NewPasswordValidPeriod(20),

    /**
     * 密碼不允許包含身分證號
     */
    AllowIDNO(21),

    /**
     * 密碼不允許包含出生日期
     */
    AllowBirthday(22),

    /**
     * 密碼不允許連續字元
     */
    AllowContinueCharacters(23),

    /**
     * 同一登入帳號不允許重複登入
     */
    AllowMultiLogOn(24),

    /**
     * 已超過密碼提示答案錯誤次數，該帳戶已被鎖定
     */
    PasswordAnswerThreshold(25),

    /**
     * 密碼生效日期錯誤
     */
    PasswordEffectDateInvalid(26),

    /**
     * 上次變更密碼日期錯誤
     */
    LastPasswordChangeTimeInvalid(27),

    /**
     * 該登入帳號尚未啟用
     */
    LogOnIdNotEffect(31),

    /**
     * 該登入帳號已停用
     */
    LogOnIdExpired(32),

    /**
     * 登入失敗
     */
    LogOnFail(33),

    /**
     * 無使用者狀態資料
     */
    WithoutUserStatusData(34),

    /**
     * 重複登入
     */
    UserAlreadyLogOn(35),

    /**
     * 語系不得空白
     */
    EmptyCulture(36),

    /**
     * 舊密碼錯誤
     */
    WrongOldPassword(37),

    /**
     * 新密碼不符合密碼管理原則
     */
    NewPasswordAgainstPolicy(38),

    /**
     * 傳入參數錯
     */
    NoParameterValue(39),

    /**
     * 新舊密碼不得相同
     */
    TheSamePassword(40),

    /**
     * 新密碼不得空白
     */
    NewPasswordEmpty(41),

    /**
     * 登入帳號或密碼錯誤
     */
    WrongLogOnIdPassword(42),

    /**
     * 密碼提示答案錯誤
     */
    WrongPasswordHintAnswer(43),

    /**
     * 使用者序號未傳入
     */
    LostUserId(51),

    /**
     * 登入帳號不得空白
     */
    EmptyLogOnId(52),

    /**
     * 使用者名稱不得空白
     */
    EmptyUserName(53),

    /**
     * 無此使用者資料
     */
    WithoutUserData(54),

    /**
     * 密碼不得空白
     */
    EmptyPassword(55),

    /**
     * 角色序號未傳入
     */
    LostRoleId(56),

    /**
     * 角色代碼不得空白
     */
    EmptyRoleNo(57),

    /**
     * 角色名稱不得空白
     */
    EmptyRoleName(58),

    /**
     * 無此角色資料
     */
    WithoutRoleData(59),

    /**
     * 資源序號未傳入
     */
    LostResourceId(61),

    /**
     * 資源代碼不得空白
     */
    EmptyResourceNo(62),

    /**
     * 資源內容不得空白
     */
    EmptyResourceName(63),

    /**
     * 無此資源資料
     */
    WithoutResourceData(64),

    /**
     * 功能群組序號未傳入
     */
    LostGroupId(66),

    /**
     * 功能群組代碼不得空白
     */
    EmptyGroupNo(67),

    /**
     * 功能群組名稱不得空白
     */
    EmptyGroupName(68),

    /**
     * 無此功能群組資料
     */
    WithoutGroupData(69),

    /**
     * 範本序號未傳入
     */
    LostTemplateId(71),

    /**
     * 範本代碼不得空白
     */
    EmptyTemplateNo(72),

    /**
     * 無此範本資料
     */
    WithoutTemplateData(73),

    /**
     * 管理原則範本名稱不得空白
     */
    EmptyTemplateName(74),

    /**
     * 管理原則序號未傳入
     */
    LostPolicyId(76),

    /**
     * 管理原則代碼不得空白
     */
    EmptyPolicyNo(77),

    /**
     * 管理原則名稱不得空白
     */
    EmptyPolicyName(78),

    /**
     * 無此管理原則資料
     */
    WithoutPolicyData(79),

    /**
     * 訊息代碼不得空白
     */
    EmptyBaseMessageNo(81),

    /**
     * 無此訊息代碼
     */
    WithoutBaseMessageNo(82),

    /**
     * 下層序號未傳入
     */
    LostChildId(83),

    /**
     * 成員類別不得空白
     */
    EmptyChildType(84),

    /**
     * 成員類別錯誤
     */
    ErrorChildType(85),

    /**
     * 成員代碼不得空白
     */
    EmptyChildNo(86),

    /**
     * 控制項不得空白
     */
    EmptyControl(87),

    /**
     * 代理人序號未傳入
     */
    LostDeputyUserId(88),

    /**
     * 代理角色序號未傳入
     */
    LostDeputyRoleId(89),

    /**
     * 代理人登入帳號不得空白
     */
    EmptyDeputyLogOnId(90),

    /**
     * 無此使用者代理人資料
     */
    WithoutDeputyUserData(91),

    /**
     * 使用者Mail不得空白
     */
    EmptyUserMail(92),

    /**
     * 應用系統訊息代號不得空白
     */
    EmptyMessageId(95),

    /**
     * 無此應用系統訊息代號
     */
    WithoutMessageId(96),

    /**
     * 對應通道不得空白
     */
    EmptyMapChannel(97),

    /**
     * 無此對應通道
     */
    WithoutMapChannel(98),

    /**
     * 無此訊息代碼
     */
    NoSuchMessageId(99),

    /**
     * 使用者必須屬於任一角色
     */
    UserMustBelongToRole(101),

    /**
     * 密碼將要到期
     */
    PasswordWillExpire(102),

    /**
     * 未新增資料
     */
    InsertNoRecord(5110),

    /**
     * 新增主檔失敗
     */
    InsertMasterFail(5111),

    /**
     * 新增語系資料失敗
     */
    InsertCultureFail(5112),

    /**
     * 新增成員資料失敗
     */
    InsertMemberFail(5113),

    /**
     * 新增稽核資料失敗
     */
    InsertAuditLogFail(5114),

    /**
     * 資料不存在無法修改
     */
    UpdateNoRecord(5120),

    /**
     * 修改主檔失敗
     */
    UpdateMasterFail(5121),

    /**
     * 修改語系資料失敗
     */
    UpdateCultureFail(5122),

    /**
     * 修改成員資料失敗
     */
    UpdateMemberFail(5123),

    /**
     * 資料不存在無法刪除
     */
    DeleteNoRecord(5130),

    /**
     * 刪除主檔失敗
     */
    DeletMasterFail(5131),

    /**
     * 刪除語系資料失敗
     */
    DeleteCultureFail(5132),

    /**
     * 刪除成員資料失敗
     */
    DeleteMemberFail(5133),

    /**
     * 查無資料
     */
    QueryNoRecord(5140),
    
    /**
     * 查詢失敗
     */
    QueryFail(5141),
    
    /**
     * 刪除失敗
     */
    DeleteFail(5141),

    /**
     * 新增失敗
     */
    InsertFail(5143),
    

    // For Batch Use
    /**
     * 批次服務接收Queue失敗
     */
    BatchReceiveQueueFail(201);


    private int value;

    private SAFEMessageId(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SAFEMessageId fromValue(int value) {
        for (SAFEMessageId e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static SAFEMessageId parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (SAFEMessageId e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }

}
