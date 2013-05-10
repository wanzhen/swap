create or replace function DX_WageC_Attendance( tBranchType in varchar2,tAgentCode in varchar2, tWageNo in varchar2) return number is
  
  mAttendenceMoney   number(20, 6) := 0;  
  tWageCode varchar2 :='DXC002';
  mK4 number(12,4):=0;                      /*����ȱ������*/
  mPinCai     number(20, 6) := 0;   -- Ƹ�Ž���
  
  BaseAward number(12,4):=0;                /*��������ְ����׼*/
    --��Ŀ�洢λ��
  mTableName laassessindex.itablename%type:='';
  mColName laassessindex.icolname%type:='';
  mIndexType laassessindex.indextype%type:='';
  mPValue number(12,4):=0;                  /*���˶�Ӧ��Pֵ*/
  mOrgCode latree.dxsaleorg%type;
  mDXBranchtype2 latree.dxbranchtype2%type;
  mAgentGrade latree.agentgrade%type;
begin
    
     Select EndDate into mMonEndDate from LAStatSegment
      where YearMonth=tWageNo
        and StatType='01';
    --��ѯ�Ƿ�����˾�� ��˾���������
    Select Count('0') into mOutWorkFlag from LAAgent a
      where AgentCode=tAgentCode
        and EmployDate<=mMonEndDate
       -- and (OutWorkDate is null or OutWorkDate>mMonEndDate);
	and (OutWorkDate is null or to_char(OutWorkDate,'YYYYMM')>=tWageNo);
    --���������������
    if mOutWorkFlag = '0' then
      return 0;
    end if;
  
    select agentgrade,DXBranchtype2,dxsaleorg into mAgentGrade,mDXBranchtype2,mOrgCode from latree where agentcode=tAgentcode and branchtype=5;
    select count(*) into tHasRecord from LAWageRadixToGrade where indexcode=tWageCode and agentgrade=mAgentGrade
         /*and orgcode=mOrgCode */and tWageNo>=startdate and tWageNo<=enddate;
    if tHasRecord>0 then
      select radixvalue into BaseAward from LAWageRadixToGrade where indexcode=tWageCode and agentgrade=mAgentGrade
      /*and orgcode=mOrgCode*/ and tWageNo>=startdate and tWageNo<=enddate;
    else
         BaseAward:=0;
    end if;
    --Ƹ�Ž���
     Select nvl(Sum(Money),0)
          into mPinCai from LARewardPunish
          where AgentCode=tAgentCode and indexcalno=tWageNo and Awardtitle='01' and BranchType=tBranchType;
     --ȱ������
      select nvl(sum(k4),0) into mK4 from LAKPIImportBaseData
   where agentcode=tAgentCode and startdate=tWageNo;     
   
     --�ҵ����˵�Pֵ
      select iTableName,iColName,indextype into mTableName,mColName,mIndexType  from laassessindex where INDEXCODE = 'DXB024' and branchtype=5;
      mCalSql:='select '||mColName||' from '||mTableName||' where indextype='''||mIndexType||''' and indexcalno=''' ||tWageNo||''' and agentcode=''' ||tAgentCode||'''';
      execute immediate mCalSql into mPValue;
      
       
    Result := (BaseAward*mPValue + mPinCai)*mK4/21.75;
   
  return(Result);
end DX_WageC_Attendance;
/
