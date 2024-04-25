//package com.syscom.fep.batch.task.atmp;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.syscom.fep.base.FEPBase;
//import com.syscom.fep.base.enums.FEPChannel;
//import com.syscom.fep.base.vo.LogData;
//import com.syscom.fep.batch.base.enums.BatchReturnCode;
//import com.syscom.fep.batch.base.library.BatchJobLibrary;
//import com.syscom.fep.batch.base.task.Task;
//import com.syscom.fep.common.log.LogHelperFactory;
//import com.syscom.fep.configuration.CMNConfig;
//import com.syscom.fep.frmcommon.util.CalendarUtil;
//import com.syscom.fep.frmcommon.util.FormatUtil;
//import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
//import com.syscom.fep.mybatis.mapper.BsdaysMapper;
//import com.syscom.fep.mybatis.model.Bsdays;
//import com.syscom.fep.vo.enums.ATMZone;
//
///**
// *
// */
//public class ImportBSDY extends FEPBase implements Task {
////	public class ImportBSDY  {
//
//    private boolean batchResult = false;
//    private String _batchLogPath = StringUtils.EMPTY;
//
//    private BatchJobLibrary job = null;
//    private String effDate = "";
//
//    private SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
//    private BsdaysMapper bsdaysMapper = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
//
//
//    @Override
//    public BatchReturnCode execute(String[] args) {
//        try {
//            //初始化相關批次物件及拆解傳入參數
//            this.initialBatch(args);
//            job.writeLog("---------------------------------------------------------------------------------------");
//            job.writeLog(ProgramName + "開始!");
//            //回報批次平台開始工作
//            job.startTask();
//            if (!this.checkConfig()) {
//                job.stopBatch();
//                return BatchReturnCode.Succeed;
//            }
//            job.writeLog(ProgramName + "開始2!");
//            //批次主要處理流程
//            batchResult = this.mainProcess();
//            //通知批次作業管理系統工作正常結束
//            if (batchResult) {
//                job.writeLog(ProgramName + "正常結束!!");
//                job.writeLog("------------------------------------------------------------------");
//                job.endTask();
//            } else {
//                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
//                job.writeLog("------------------------------------------------------------------");
//                job.abortTask();
//            }
//            return BatchReturnCode.Succeed;
//        } catch (Exception ex) {
//            //通知批次作業管理系統工作失敗,暫停後面流程
//            try {
//                job.abortTask();
//                job.writeLog(ProgramName + "失敗!!");
//                job.writeLog(ex.toString());
//                job.writeLog("------------------------------------------------------------------");
//            } catch (Exception e) {
//                logContext.setProgramException(e);
//                logContext.setProgramName(ProgramName);
//                sendEMS(logContext);
//            }
//            return BatchReturnCode.ProgramException;
//        } finally {
//            if (job != null) {
//                job.writeLog(ProgramName + "結束!!");
//                job.writeLog("------------------------------------------------------------------");
//                job.dispose();
//                job = null;
//            }
//            if (logContext != null) {
//                logContext = null;
//            }
//        }
//    }
//
//    /**
//     * 初始化相關批次物件及拆解傳入參數
//     *
//     * @param args
//     */
//    private void initialBatch(String[] args) {
//        logContext = new LogData();
//        logContext.setChannel(FEPChannel.BATCH);
//        logContext.setEj(0);
//        logContext.setProgramName(ProgramName);
//        //檢查Batch Log目錄參數
//        _batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
//
//        if (StringUtils.isBlank(_batchLogPath)) {
//            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
//            return;
//        }
//
//        //初始化BatchJob物件,傳入工作執行參數
//        job = new BatchJobLibrary(this, args, _batchLogPath);
//    }
//
//    /**
//     * 檢查是否為營業日
//     *
//     * @return
//     * @throws Exception
//     */
//    private boolean checkConfig() throws Exception {
//        if (!job.getArguments().containsKey("FORCERUN")) {
//            if (!job.isBsDay(ATMZone.TWN.toString())) {
//                job.writeLog("非營業日不執行批次");
//                return false;
//            }
//        }
////		else {
////			if("Y".equals(job.getArguments().get("FORCERUN"))) {
////				job.writeLog("參數FORCERUN=Y, 強制執行此批次!");
////			}
////
////		}
//
//        //'最大執行Thread數
////		_batchInputPath = CMNConfig.getInstance().getBatchInputPath();
////        _batchInputFile = CMNConfig.getInstance().getBT010020File();
//
////		if (StringUtils.isBlank(_batchInputPath) || StringUtils.isBlank(_batchInputFile)) {
////			LogHelperFactory.getGeneralLogger().error("參數BatchInputPath未設定");
////			return false;
////		}
//
//        job.writeLog("effDate:" + this.effDate);
//        return true;
//    }
//
//
//    /**
//     * 批次主要處理流程
//     * @return
//     */
//    /**
//     * @return
//     */
//    @Transactional
//    public boolean mainProcess() throws IOException {
//        boolean rtn = true;//是否正常跑完
//        BufferedReader br = null;
//        try {
////        	String bSYEAR = job.getArguments().get("BSYEAR");
//            String importPath = job.getArguments().get("ImportPath");
//            String importFileName = job.getArguments().get("ImportFileName");
//            String sFTPServer = "";
//            String sFTPUCode = "";
//            String sFTPSCode = "";
//            job.writeLog(ProgramName + "mainProcess!");
//
//////        	取得SFTP Server IP
////        	Map<String, Object> sFTPServermap = sysconfExtMapper.getAllDataByPk2(1, "SFTPServer");
////        	if (sFTPServermap == null || StringUtils.isBlank(String.valueOf(sFTPServermap.get("SYSCONF_VALUE")))) {
////        		job.writeLog("SYSCONF NOT FOUND");
////    			rtn = false;
////    		}else {
////    			sFTPServer = String.valueOf(sFTPServermap.get("SYSCONF_VALUE"));
////    		}
////
//////        	取得SFTP Server 帳號
////        	Map<String, Object> sFTPUCodemap = sysconfExtMapper.getAllDataByPk2(1, "SFTPUCode");
////        	if (sFTPUCodemap == null || StringUtils.isBlank(String.valueOf(sFTPUCodemap.get("SYSCONF_VALUE")))) {
////        		job.writeLog("SYSCONF NOT FOUND");
////    			rtn = false;
////    		}else {
////    			sFTPUCode = String.valueOf(sFTPUCodemap.get("SYSCONF_VALUE"));
////    		}
//////        	取得SFTP Server 密碼
////        	Map<String, Object> sFTPSCodemap = sysconfExtMapper.getAllDataByPk2(1, "SFTPSCode");
////        	if (sFTPSCodemap == null || StringUtils.isBlank(String.valueOf(sFTPSCodemap.get("SYSCONF_VALUE")))) {
////        		job.writeLog("SYSCONF NOT FOUND");
////    			rtn = false;
////    		}else {
////    			sFTPSCode = String.valueOf(sFTPSCodemap.get("SYSCONF_VALUE"));
////    		}
//
//            if (StringUtils.isNotBlank(importPath)) {
////                File file = new File(CleanPathUtil.cleanString(basePath), CleanPathUtil.cleanString(importFileName));
//                FileReader fr = new FileReader(importPath + importFileName);
//                br = new BufferedReader(fr);
//                String arraydate1 = "";
//                String bsdaysDate = "";
//                int rocYear = 0;
//                int adYear = 0;
//                int firstint = 6;
//                boolean datepass = false;
//                int nextnbsdyint = 0;
//                int differenceint = 0;
//
////    			boolean nextnbsdy = false;
//                while (br.ready()) {
//                    String value = br.readLine();
//                    if (StringUtils.isNotBlank(value)) {
//                        if (StringUtils.isBlank(arraydate1)) {
//                            arraydate1 = value.substring(2, 5);
//                        } else if (value.substring(2, 5).equals(arraydate1)) {
//                            datepass = true;
//                        }
//
//                    }
//                }
//
//                if (datepass) {
//                    fr = new FileReader(importPath + importFileName);
//                    br = new BufferedReader(fr);
//                    while (br.ready()) {
//                        String value = br.readLine();
//                        rocYear = Integer.valueOf(value.substring(2, 5));
//                        //西元年
//                        adYear = rocYear + 1911;
//                        //DMDATA[6:1] = 1，表示第一筆，為資料年度前 180天營業狀況
//                        if ("1".equals(value.substring(5, 6))) {
//                            for (int i = firstint; i < value.length(); i++) {
//                                DateFormat dateFormat = new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
//                                Date date = dateFormat.parse(adYear + "0101");
//
//                                Calendar calendar = Calendar.getInstance();
//                                calendar.setTime(date);
//                                calendar.add(Calendar.DATE, i - firstint);
//
//                                Bsdays bsdays = bsdaysMapper.selectByPrimaryKey("TWN", FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//
//                                if (bsdays != null) {
//                                    //營業日
//                                    char singlechar = value.charAt(i);
//                                    if (Character.compare('Y', singlechar) == 0) {
//                                        bsdays.setBsdaysWorkday((short) 1);
//                                    } else {
//                                        bsdays.setBsdaysWorkday((short) 0);
//                                    }
//
//                                    //下一個營業日
//                                    for (int x = i + 1; x < value.length(); x++) {
//                                        char nextchar = value.charAt(x);
//                                        if (Character.compare('Y', nextchar) == 0) {
//                                            nextnbsdyint = x;
//                                            break;
//                                        }
//                                    }
//                                    differenceint = nextnbsdyint - i;
//                                    calendar.add(Calendar.DATE, differenceint);
//                                    //下一個營業日
//                                    bsdays.setBsdaysNbsdy(FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                    bsdaysMapper.updateByPrimaryKey(bsdays);
//                                    //新增
//                                } else {
//                                    Bsdays bsdaysEntity = new Bsdays();
//                                    /* 依照 DMDATA[3:3] 該年度的日曆，逐筆 Insert*/
//                                    bsdaysEntity.setBsdaysZoneCode("TWN");
//                                    bsdaysEntity.setBsdaysDate(FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                    //太陽日
//                                    bsdaysEntity.setBsdaysJday(i - 5);
//
//                                    char singlechar = value.charAt(i);
//                                    if (Character.compare('Y', singlechar) == 0) {
//                                        bsdaysEntity.setBsdaysWorkday((short) 1);
//                                    } else {
//                                        bsdaysEntity.setBsdaysWorkday((short) 0);
//                                    }
//
//                                    //星期幾
//                                    int weekint = CalendarUtil.getDayOfWeekForQuartz(calendar);
//                                    if (weekint == 1) {
//                                        bsdaysEntity.setBsdaysWeekno((short) 7);
//                                    } else {
//                                        short weekno = (short) ((short) weekint - 1);
//                                        bsdaysEntity.setBsdaysWeekno(weekno);
//                                    }
//
//                                    //下一個營業日
//                                    for (int x = i + 1; x < value.length(); x++) {
//                                        char nextchar = value.charAt(x);
//                                        if (Character.compare('Y', nextchar) == 0) {
//                                            nextnbsdyint = x;
//                                            break;
//                                        }
//                                    }
//                                    differenceint = nextnbsdyint - i;
//                                    calendar.add(Calendar.DATE, differenceint);
//                                    //下一個營業日
//                                    bsdaysEntity.setBsdaysNbsdy(FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//
//                                    bsdaysEntity.setBsdaysStDateAtm(bsdaysEntity.getBsdaysNbsdy());
//                                    bsdaysEntity.setBsdaysStDateRm(bsdaysEntity.getBsdaysDate());
//                                    bsdaysEntity.setBsdaysStFlag((short) 0);
//                                    bsdaysEntity.setUpdateUserid(0);
//                                    bsdaysEntity.setUpdateTime(new Date());
//
//                                    bsdaysMapper.insert(bsdaysEntity);
//                                }
//                                //上年度倒數幾天的下一營業日需要重新修改，當年度日曆檔不含
//                                if (i == firstint) {
//                                    dateFormat = new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
//                                    date = dateFormat.parse(adYear - 1 + "1231");
//                                    boolean findlastdaystop = true;
//                                    calendar = Calendar.getInstance();
//                                    calendar.setTime(date);
//                                    //foreach day
//                                    Calendar foreachcalendar = Calendar.getInstance();
//                                    Date foreachdate = dateFormat.parse(adYear - 1 + "1231");
//                                    foreachcalendar.setTime(foreachdate);
//
//                                    bsdays = bsdaysMapper.selectByPrimaryKey("TWN", FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                    //下一個營業日
//                                    for (int x = i; x < value.length(); x++) {
//                                        char nextchar = value.charAt(x);
//                                        if (Character.compare('Y', nextchar) == 0) {
//                                            nextnbsdyint = x;
//                                            break;
//                                        }
//                                    }
//                                    differenceint = nextnbsdyint - i;
//                                    if (differenceint == 0) {
//                                        differenceint = differenceint + 1;
//                                    }
//                                    calendar.add(Calendar.DATE, differenceint + 1);
//                                    //下一個營業日
//                                    int findcnt = 0;
//                                    while (findlastdaystop) {
//                                        if (findcnt > 0) {
//                                            foreachcalendar.add(Calendar.DATE, -1);
//                                            bsdays = bsdaysMapper.selectByPrimaryKey("TWN", FormatUtil.dateTimeFormat(foreachcalendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                        }
//                                        //foreach day
//                                        if (bsdays != null) {
//                                            if (StringUtils.isNotBlank(bsdays.getBsdaysDate()) && StringUtils.isNotBlank(bsdays.getBsdaysNbsdy())) {
//                                                Date comparebsday = dateFormat.parse(bsdays.getBsdaysDate());
//                                                Date comparenbbsdy = dateFormat.parse(bsdays.getBsdaysNbsdy());
//                                                if (comparenbbsdy.before(comparebsday) || bsdays.getBsdaysDate().equals(bsdays.getBsdaysNbsdy())) {
//                                                    bsdays.setBsdaysNbsdy(FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                                    bsdays.setBsdaysStDateAtm(bsdays.getBsdaysNbsdy());
//                                                    bsdaysMapper.updateByPrimaryKey(bsdays);
//                                                } else {
//                                                    findlastdaystop = false;
//                                                }
//                                            } else {
//                                                findlastdaystop = false;
//                                            }
//                                        } else {
//                                            findlastdaystop = false;
//                                        }
//
//                                        findcnt++;
//                                    }
//                                }
//                            }
//                            //DMDATA[6:1] = 2，表示第二筆，為資料年度第 181~365天(閏年為 366) 營業狀況
//                        } else if ("2".equals(value.substring(5, 6))) {
//
//                            for (int i = firstint; i < value.length() - 1; i++) {
//                                DateFormat dateFormat = new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
//                                Date date = dateFormat.parse(adYear + "0101");
//
//                                Calendar calendar = Calendar.getInstance();
//                                calendar.setTime(date);
//                                calendar.add(Calendar.DATE, 180 + i - firstint);
//
//                                Bsdays bsdays = bsdaysMapper.selectByPrimaryKey("TWN", FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//
//                                if (bsdays != null) {
//                                    //營業日
//                                    char singlechar = value.charAt(i);
//                                    if (Character.compare('Y', singlechar) == 0) {
//                                        bsdays.setBsdaysWorkday((short) 1);
//                                    } else {
//                                        bsdays.setBsdaysWorkday((short) 0);
//                                    }
//
//                                    //下一個營業日
//                                    for (int x = i + 1; x < value.length(); x++) {
//                                        char nextchar = value.charAt(x);
//                                        if (Character.compare('Y', nextchar) == 0) {
//                                            nextnbsdyint = x;
//                                            break;
//                                        }
//                                    }
//                                    differenceint = nextnbsdyint - i;
//                                    calendar.add(Calendar.DATE, differenceint);
//                                    //下一個營業日
//                                    bsdays.setBsdaysNbsdy(FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                    bsdaysMapper.updateByPrimaryKey(bsdays);
//                                    //新增
//                                } else {
//                                    Bsdays bsdaysEntity = new Bsdays();
//                                    /* 依照 DMDATA[3:3] 該年度的日曆，逐筆 Insert*/
//                                    bsdaysEntity.setBsdaysZoneCode("TWN");
//                                    bsdaysEntity.setBsdaysDate(FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                    //太陽日
//                                    bsdaysEntity.setBsdaysJday(i - 5 + 180);
//
//                                    char singlechar = value.charAt(i);
//                                    if (Character.compare('Y', singlechar) == 0) {
//                                        bsdaysEntity.setBsdaysWorkday((short) 1);
//                                    } else {
//                                        bsdaysEntity.setBsdaysWorkday((short) 0);
//                                    }
//
//                                    //星期幾
//                                    int weekint = CalendarUtil.getDayOfWeekForQuartz(calendar);
//                                    if (weekint == 1) {
//                                        bsdaysEntity.setBsdaysWeekno((short) 7);
//                                    } else {
//                                        short weekno = (short) ((short) weekint - 1);
//                                        bsdaysEntity.setBsdaysWeekno(weekno);
//                                    }
//
//                                    //下一個營業日
//                                    for (int x = i + 1; x < value.length(); x++) {
//                                        char nextchar = value.charAt(x);
//                                        if (Character.compare('Y', nextchar) == 0) {
//                                            nextnbsdyint = x;
//                                            break;
//                                        }
//                                    }
//                                    differenceint = nextnbsdyint - i;
//                                    calendar.add(Calendar.DATE, differenceint);
//                                    //下一個營業日
//                                    bsdaysEntity.setBsdaysNbsdy(FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//
//                                    bsdaysEntity.setBsdaysStDateAtm(bsdaysEntity.getBsdaysNbsdy());
//                                    bsdaysEntity.setBsdaysStDateRm(bsdaysEntity.getBsdaysDate());
//                                    bsdaysEntity.setBsdaysStFlag((short) 0);
//                                    bsdaysEntity.setUpdateUserid(0);
//                                    bsdaysEntity.setUpdateTime(new Date());
//
//                                    bsdaysMapper.insert(bsdaysEntity);
//                                }
//
//                                //前半年最後一天的營業日
//                                if (i == firstint) {
//                                    dateFormat = new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
//                                    date = dateFormat.parse(adYear + "0101");
//
//                                    calendar = Calendar.getInstance();
//                                    calendar.setTime(date);
//                                    calendar.add(Calendar.DATE, 179);
//                                    bsdays = bsdaysMapper.selectByPrimaryKey("TWN", FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                    //下一個營業日
//                                    for (int x = i; x < value.length(); x++) {
//                                        char nextchar = value.charAt(x);
//                                        if (Character.compare('Y', nextchar) == 0) {
//                                            nextnbsdyint = x;
//                                            break;
//                                        }
//                                    }
//                                    differenceint = nextnbsdyint - i;
//                                    if (differenceint == 0) {
//                                        differenceint = differenceint + 1;
//                                    }
//                                    calendar.add(Calendar.DATE, differenceint);
//                                    if (bsdays != null) {
//                                        //下一個營業日
//                                        bsdays.setBsdaysNbsdy(FormatUtil.dateTimeFormat(calendar, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
//                                        bsdaysMapper.updateByPrimaryKey(bsdays);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    job.writeLog("日曆文字檔年度不相同");
//                    rtn = false;
//                }
//
//
//            }
//        } catch (Exception ex) {
//            job.writeLog(ex.getMessage());
//            rtn = false;
//        } finally {
//            if (br != null) {
//                safeClose(br);
//            }
//        }
//        return rtn;
//    }
//
//
//    public void safeClose(BufferedReader br) {
//        if (br != null) {
//            try {
//                br.close();
//            } catch (IOException e) {
//                job.writeLog(e.getMessage());
//            }
//        }
//    }
//
//}
