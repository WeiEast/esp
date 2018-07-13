package com.apass.esp.domain.vo;

import com.apass.esp.domain.entity.ProActivityCfg;
import com.apass.esp.domain.entity.ProCouponRel;
import com.apass.esp.domain.enums.ActivityCfgCoupon;

import java.util.List;

public class ActivityCfgForEditVo extends ProActivityCfg {
    private List<ProCouponRel> proCouponRels;
    private Long fydCouponId;

    public Long getFydCouponId() {
        return fydCouponId;
    }

    public void setFydCouponId(Long fydCouponId) {
        this.fydCouponId = fydCouponId;
    }

    public List<ProCouponRel> getProCouponRels() {
        return proCouponRels;
    }

    public void setProCouponRels(List<ProCouponRel> proCouponRels) {
        this.proCouponRels = proCouponRels;
    }
}
