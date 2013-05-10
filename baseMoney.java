create or replace function baseMoney( tAgentCode in varchar2, tWageNo in varchar2,tWageCode in varchar2) return number is
   --电销基本工资 计算函数
  RetrunAward number(12,4):=0;              /*基本工资最终计算结果*/
  BaseAward number(12,4):=0;                /*基本工资职级标准*/

  --mWorkDays number(12,4):=0;                /*本月实际工作天数*/

  
  mMonEndDate     Date;                      /*薪资月月末日期*/
  mOutWorkFlag    varchar2(2);               /*是否是当月离职标志 0:离职月；1：非离职月*/
  mCalSql lmcalmode.calsql%type;
  --项目存储位置
  mTableName laassessindex.itablename%type:='';
  mColName laassessindex.icolname%type:='';
  mIndexType laassessindex.indextype%type:='';
  mPValue number(12,4):=0;                  /*本人对应的P值*/
  mOrgCode latree.dxsaleorg%type;
  mDXBranchtype2 latree.dxbranchtype2%type;
  mAgentGrade latree.agentgrade%type;

  tHasRecord Integer:=0; --是否有记录

begin
  --发放时必须在职
    --查询薪资月月末
    Select EndDate into mMonEndDate from LAStatSegment
      where YearMonth=tWageNo
        and StatType='01';
    --查询是否是离司月 离司月允许计算
    Select Count('0') into mOutWorkFlag from LAAgent a
      where AgentCode=tAgentCode
        and EmployDate<=mMonEndDate
       -- and (OutWorkDate is null or OutWorkDate>mMonEndDate);
  and (OutWorkDate is null or to_char(OutWorkDate,'YYYYMM')>=tWageNo);
    --如果不满足条件：
    if mOutWorkFlag = '0' then
      return 0;
    end if;

   --本月任职天数对应工资比例
  --  select nvl(sum(k4),0) into mK4 from LAKPIImportBaseData
 --  where agentcode=tAgentCode and startdate=tWageNo;


  --  select iTableName,iColName,indextype into mTableName,mColName,mIndexType  from laassessindex where INDEXCODE = 'DXB028' and branchtype=5;
  --  mCalSql:='select nvl(sum('||mColName||'),0) from '||mTableName||' where indextype='''||mIndexType||''' and indexcalno=''' ||tWageNo||''' and agentcode=''' ||tAgentCode||'''';
  --  execute immediate mCalSql into mK1;
    --因为操作员错误输入，基本工资为负值 容错。
  --  if (mK4-mK1)<0 then
  --     return 0;
  --  end if;
  --  BaseAwardRate:=(mK4-mK1)/21.7500;
  --  if BaseAwardRate=0 then
  --     return 0;
  --  end if;

    --基本工资
    select agentgrade,DXBranchtype2,dxsaleorg into mAgentGrade,mDXBranchtype2,mOrgCode from latree where agentcode=tAgentcode and branchtype=5;
    select count(*) into tHasRecord from LAWageRadixToGrade where indexcode=tWageCode and agentgrade=mAgentGrade
         /*and orgcode=mOrgCode */and tWageNo>=startdate and tWageNo<=enddate;
    if tHasRecord>0 then
      select radixvalue into BaseAward from LAWageRadixToGrade where indexcode=tWageCode and agentgrade=mAgentGrade
      /*and orgcode=mOrgCode*/ and tWageNo>=startdate and tWageNo<=enddate;
    else
         BaseAward:=0;
    end if;
      --找到本人的P值
      select iTableName,iColName,indextype into mTableName,mColName,mIndexType  from laassessindex where INDEXCODE = 'DXB024' and branchtype=5;
      mCalSql:='select '||mColName||' from '||mTableName||' where indextype='''||mIndexType||''' and indexcalno=''' ||tWageNo||''' and agentcode=''' ||tAgentCode||'''';
      execute immediate mCalSql into mPValue;
      --基本工资算法 =本职业级别对应的基本工资标准*本人的P值*本月工作天数的工资比例
    --  RetrunAward:=BaseAward*mPValue*BaseAwardRate;
     RetrunAward:=BaseAward*mPValue;
    return(RetrunAward);
end baseMoney;
