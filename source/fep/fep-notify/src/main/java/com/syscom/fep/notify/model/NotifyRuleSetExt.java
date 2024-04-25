package com.syscom.fep.notify.model;

import com.syscom.fep.mybatis.model.Notifyruleset;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

public class NotifyRuleSetExt  implements Serializable, Cloneable {
    private static final long serialVersionUID = -7420049018526259961L;
    private Long ruleSetId;
    private String ruleName;
    private String ruleExpression;
    private String customerComponent;
    private Integer updateUserId;
    private Date updateTime;

    private String expression;

    public Long getRuleSetId() {
		return ruleSetId;
	}

	public void setRuleSetId(Long ruleSetId) {
		this.ruleSetId = ruleSetId;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleExpression() {
		return ruleExpression;
	}

	public void setRuleExpression(String ruleExpression) {
		this.ruleExpression = ruleExpression;
	}

	public String getCustomerComponent() {
		return customerComponent;
	}

	public void setCustomerComponent(String customerComponent) {
		this.customerComponent = customerComponent;
	}

	public Integer getUpdateUserId() {
		return updateUserId;
	}

	public void setUpdateUserId(Integer updateUserId) {
		this.updateUserId = updateUserId;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public NotifyRuleSetExt(Notifyruleset notifyruleset) {
        this.ruleSetId = notifyruleset.getRulesetId();
        this.ruleName =notifyruleset.getRuleName();
        this.ruleExpression = notifyruleset.getRuleExpression();
        this.customerComponent = notifyruleset.getCustomerComponent();
        this.updateUserId = notifyruleset.getUpdateUserId();
        this.updateTime = notifyruleset.getUpdateTime();
    }

    @Override
    public NotifyRuleSetExt clone() {
        NotifyRuleSetExt object = null;
        try {
            object = (NotifyRuleSetExt) super.clone();
            return object;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return object;
    }
}
