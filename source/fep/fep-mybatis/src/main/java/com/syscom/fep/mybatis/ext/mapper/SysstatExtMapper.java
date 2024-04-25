package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.SysstatMapper;
import com.syscom.fep.mybatis.model.Sysstat;

@Resource
public interface SysstatExtMapper extends SysstatMapper {
	/**
	 * 2021-04-20 Richard add
	 *
	 * @return
	 */
	List<Sysstat> selectAll();

	/**
	 * Han add 2022-07-22
	 * @return
	 */
	List<Map<String, Object>> getQueryAll();

	int updateByHbkno(
			@Param("SYSSTAT_HBKNO")String SYSSTAT_HBKNO,
// 2024-03-06 Richard modified for SYSSTATE 調整
//			@Param("SYSSTAT_INTRA")Short SYSSTAT_INTRA,
//			@Param("SYSSTAT_AGENT")Short SYSSTAT_AGENT,
//			@Param("SYSSTAT_ISSUE")Short SYSSTAT_ISSUE,
//			@Param("SYSSTAT_IWD_I")Short SYSSTAT_IWD_I,
//			@Param("SYSSTAT_IWD_A")Short SYSSTAT_IWD_A,
//			@Param("SYSSTAT_IWD_F")Short SYSSTAT_IWD_F,
//			@Param("SYSSTAT_IFT_I")Short SYSSTAT_IFT_I,
//			@Param("SYSSTAT_IFT_A")Short SYSSTAT_IFT_A,
//			@Param("SYSSTAT_IFT_F")Short SYSSTAT_IFT_F,
//			@Param("SYSSTAT_IPY_I")Short SYSSTAT_IPY_I,
//			@Param("SYSSTAT_IPY_A")Short SYSSTAT_IPY_A,
//			@Param("SYSSTAT_IPY_F")Short SYSSTAT_IPY_F,
//			@Param("SYSSTAT_CDP_F")Short SYSSTAT_CDP_F,
//			@Param("SYSSTAT_ICCDP_I")Short SYSSTAT_ICCDP_I,
//			@Param("SYSSTAT_ICCDP_A")Short SYSSTAT_ICCDP_A,
//			@Param("SYSSTAT_ICCDP_F")Short SYSSTAT_ICCDP_F,
//			@Param("SYSSTAT_ETX_I")Short SYSSTAT_ETX_I,
//			@Param("SYSSTAT_ETX_A")Short SYSSTAT_ETX_A,
//			@Param("SYSSTAT_ETX_F")Short SYSSTAT_ETX_F,
//			@Param("SYSSTAT_2525_A")Short SYSSTAT_2525_A,
//			@Param("SYSSTAT_2525_F")Short SYSSTAT_2525_F,
//			@Param("SYSSTAT_CPU_A")Short SYSSTAT_CPU_A,
//			@Param("SYSSTAT_CPU_F")Short SYSSTAT_CPU_F,
//			@Param("SYSSTAT_CAU_A")Short SYSSTAT_CAU_A,
//			@Param("SYSSTAT_CAU_F")Short SYSSTAT_CAU_F,
//			@Param("SYSSTAT_CWV_A")Short SYSSTAT_CWV_A,
//			@Param("SYSSTAT_CWM_A")Short SYSSTAT_CWM_A,
//			@Param("SYSSTAT_GPCWD_F")Short SYSSTAT_GPCWD_F,
//			@Param("SYSSTAT_CA_I")Short SYSSTAT_CA_I ,
//			@Param("SYSSTAT_CAV_A")Short SYSSTAT_CAV_A,
//			@Param("SYSSTAT_CAF_A")Short SYSSTAT_CAF_A,
//			@Param("SYSSTAT_CAM_A")Short SYSSTAT_CAM_A,
//			@Param("SYSSTAT_GPCAD_F")Short SYSSTAT_GPCAD_F,
//			@Param("SYSSTAT_CAA_I")Short SYSSTAT_CAA_I,
//			@Param("SYSSTAT_FWD_I")Short SYSSTAT_FWD_I,
//			@Param("SYSSTAT_ADM_I")Short SYSSTAT_ADM_I,
//			@Param("SYSSTAT_AIG")Short SYSSTAT_AIG,
//			@Param("SYSSTAT_HK_ISSUE")Short SYSSTAT_HK_ISSUE,
//			@Param("SYSSTAT_HK_FISCMB")Short SYSSTAT_HK_FISCMB,
//			@Param("SYSSTAT_HK_PLUS")Short SYSSTAT_HK_PLUS,
//			@Param("SYSSTAT_MO_ISSUE")Short SYSSTAT_MO_ISSUE,
//			@Param("SYSSTAT_MO_FISCMB")Short SYSSTAT_MO_FISCMB,
//			@Param("SYSSTAT_MO_PLUS")Short SYSSTAT_MO_PLUS,
			@Param("SYSSTAT_T24_TWN")Short SYSSTAT_T24_TWN
// 2024-03-06 Richard modified for SYSSTATE 調整
//			@Param("SYSSTAT_T24_HKG")Short SYSSTAT_T24_HKG,
//			@Param("SYSSTAT_T24_MAC")Short SYSSTAT_T24_MAC,
//			@Param("SYSSTAT_HK_FISCMQ")Short SYSSTAT_HK_FISCMQ,
//			@Param("SYSSTAT_MO_FISCMQ")Short SYSSTAT_MO_FISCMQ,
//			@Param("SYSSTAT_FAW_A")Short SYSSTAT_FAW_A,
//			@Param("SYSSTAT_EAF_A")Short SYSSTAT_EAF_A,
//			@Param("SYSSTAT_EAV_A")Short SYSSTAT_EAV_A,
//			@Param("SYSSTAT_EAM_A")Short SYSSTAT_EAM_A,
//			@Param("SYSSTAT_EWV_A")Short SYSSTAT_EWV_A,
//			@Param("SYSSTAT_EWM_A")Short SYSSTAT_EWM_A,
//			@Param("SYSSTAT_GPEMV_F")Short SYSSTAT_GPEMV_F,
//			@Param("SYSSTAT_PURE")Short SYSSTAT_PURE,
//			@Param("SYSSTAT_IIQ_I")Short SYSSTAT_IIQ_I,
//			@Param("SYSSTAT_IIQ_F")Short SYSSTAT_IIQ_F,
//			@Param("SYSSTAT_IIQ_P")Short SYSSTAT_IIQ_P,
//			@Param("SYSSTAT_IFT_P")Short SYSSTAT_IFT_P,
//			@Param("SYSSTAT_ICCDP_P")Short SYSSTAT_ICCDP_P,
//			@Param("SYSSTAT_IPY_P")Short SYSSTAT_IPY_P,
//			@Param("SYSSTAT_CDP_A")Short SYSSTAT_CDP_A,
//			@Param("SYSSTAT_CDP_P")Short SYSSTAT_CDP_P,
//			@Param("SYSSTAT_IQV_P")Short SYSSTAT_IQV_P,
//			@Param("SYSSTAT_IQM_P")Short SYSSTAT_IQM_P,
//			@Param("SYSSTAT_IQC_P")Short SYSSTAT_IQC_P,
//			@Param("SYSSTAT_EQP_P")Short SYSSTAT_EQP_P,
//			@Param("SYSSTAT_EQC_P")Short SYSSTAT_EQC_P,
//			@Param("SYSSTAT_EQU_P")Short SYSSTAT_EQU_P,
//			@Param("SYSSTAT_ADM_A")Short SYSSTAT_ADM_A,
//			@Param("SYSSTAT_NWD_I")Short SYSSTAT_NWD_I,
//			@Param("SYSSTAT_NWD_A")Short SYSSTAT_NWD_A,
//			@Param("SYSSTAT_GPIWD_F")Short SYSSTAT_GPIWD_F,
//			@Param("SYSSTAT_GPOB_F")Short SYSSTAT_GPOB_F,
//			@Param("SYSSTAT_NFW_I")Short SYSSTAT_NFW_I,
//			@Param("SYSSTAT_VAA_F")Short SYSSTAT_VAA_F,
//			@Param("SYSSTAT_VAA_A")Short SYSSTAT_VAA_A
			);

	Sysstat selectFirstByLbsdyFisc();
}