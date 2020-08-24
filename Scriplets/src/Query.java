public class Query {
	public static final String USER_DATA_FEE = "select  " + 
			"u.id as user_id,  " + 
			"u.user_access_key as registration_number, " + 
			" u.enroll_id as enrollment_number, u.name, " + 
			" (select json_data.* ->> 'value' as value from json_array_elements(u.misc_data) json_data where  " + 
			"json_data->>'label' = 'programName') as course, " + 
			" (select json_data.* ->> 'value' as value from json_array_elements(u.misc_data) json_data where  " + 
			"json_data->>'label' = 'semesterName') as semester, " + 
			"(select json_data.* ->> 'value' as value from json_array_elements(u.misc_data) json_data where  " + 
			"json_data->>'label' = 'admissionNumber') as admission_number, " + 
			"(select sum(d.amount) from deposits as d " + 
			"where d.fee_fund_id = ? " + 
			"and d.user_id = u.id) as deposit_amount, " + 
			"(select distinct ARRAY_AGG(fm.id) as fee_memo_ids from fee_memos as fm left join course_mappings as cm " + 
			"on fm.user_id = cm.user_id and fm.user_id = u.id  and cm.deleted_at is null and fm.deleted_at is null where fm.fee_fund_id = ? and fm.client_identifier = any(?) and cm.academic_year_id = ?) as fee_memo_ids " + 
			"from users as u " + 
			"where u.id in (select distinct fm.user_id from fee_memos as fm " + 
			"inner join course_mappings as cm " + 
			"on fm.user_id = cm.user_id " + 
			"where cm.academic_year_id = ? " + 
			") " + 
			"and u.organization_id = ? " + 
			"order by name";
	
	public static final String USER_DATA_METACAMPUS = "select " + 
			"distinct s.registrationnumber, s.admissiondate,  " + 
			"rel.academicprogramunitseqno as semester, rel.academicprogramyear as year,  " + 
			"sa.status, sa.statuseffectivedate " + 
			"from student s " + 
			"inner join studentadmissions sa " + 
			"on sa.studentKey = s.`key` " + 
			"inner join studentregistrations sr  " + 
			"on sr.studentAdmissionKey = sa.`key` and sr.active = true and sr.isDeleted = false " + 
			"inner join registrationentitylevels rel " + 
			"on rel.`key` = sr.registrationEntityLevelKey " + 
			"inner join registrationentities re " + 
			"on rel.registrationEntityKey = re.`key` and re.academicyearkey = ? " + 
			"WHERE s.active = TRUE and s.isdeleted = false " + 
			"and (rel.academicprogramunitseqno = 1 or rel.academicprogramunitseqno = 3 " + 
			"or rel.academicprogramunitseqno = 5 or rel.academicprogramunitseqno = 7)";
	
	public static final String GROUP_DUE_AMOUNT_FEE = 
			"select distinct fh.group_name, result_data.due_amount from fee_heads as fh " + 
			"left outer join (SELECT  " + 
			"LOWER(fh.group_name) as fee_head_group_name, SUM(fmd.due_amount)as due_amount   " + 
			"FROM fee_memo_details as fmd INNER JOIN \"fee_heads\" as fh ON fh.id = fmd.fee_head_id  " + 
			"AND fh.organization_id = ?  " + 
			"INNER JOIN fee_memos as fm  " + 
			"ON fm.id = fmd.fee_memo_id AND fm.deleted_at IS NULL  " + 
			"WHERE fmd.deleted_at IS null AND (fmd.due_amount > 0.0 ) " + 
			"AND fm.id = any(?)  " + 
			" GROUP BY 1 " + 
			") as result_data " + 
			"on LOWER(result_data.fee_head_group_name) = LOWER(fh.group_name) " + 
			"where " + 
			"fh.fee_fund_id = ? and fh.frequency <> 'late_fee' " + 
			"and (fh.fee_type is null or fh.fee_type not in ('dishonoured_cheque')) " + 
			"and (fh.additional_charge_type is null or fh.additional_charge_type not in ('other_fee', 'cheque_return_fee')) order by fh.group_name";
	
	public static final String GROUP_DUE_AMOUNT_WITH_REG_NUM = 
			"select distinct LOWER(fh.group_name) as fee_head_group_name, fmd.due_amount as due_amount, u.user_access_key as registration_number   " + 
			"FROM fee_memo_details as fmd INNER JOIN fee_heads as fh ON fh.id = fmd.fee_head_id  " + 
			"AND fh.organization_id = ?  " + 
			"inner join fee_memos as fm  " + 
			"ON fm.id = fmd.fee_memo_id AND fm.deleted_at IS NULL  " + 
			"inner join users u on u.id = fm.user_id " +
			"WHERE fmd.deleted_at IS null AND (fmd.due_amount > 0.0 ) " + 
			"AND fm.id = any(?)  " + 
			"and fh.fee_fund_id = ? and fh.frequency <> 'late_fee' " + 
			"and (fh.fee_type is null or fh.fee_type not in ('dishonoured_cheque')) " + 
			"and (fh.additional_charge_type is null or fh.additional_charge_type not in ('other_fee', 'cheque_return_fee'))";

	public static final String GROUP_NAMES_FEE = 
			"select distinct LOWER(fh.group_name) as group_name from fee_heads as fh where " + 
			"fh.fee_fund_id = ? and fh.frequency <> 'late_fee' " + 
			"and (fh.fee_type is null or fh.fee_type not in ('dishonoured_cheque')) " + 
			"and (fh.additional_charge_type is null or fh.additional_charge_type not in ('other_fee', 'cheque_return_fee')) " + 
			"order by group_name";
	
	public static final String CASH_CHEQUE_LATE_DISCOUNT_AMOUNT_FEE = 
			"select  " + 
			"(select sum(discount) from fee_transaction_memo_details as ftmd " + 
			"inner join fee_transaction_memos as ftm on ftm.id = ftmd.fee_transaction_memo_id where ftm.fee_memo_id = any(?)) as discount, " + 
			"(select sum(ftmd.paid_amount) as late_fee_amount from fee_transaction_memo_details as ftmd " + 
			"inner join fee_transaction_memos as ftm on ftmd.fee_transaction_memo_id = ftm.id " + 
			"where ftmd.fee_head_id = (select fh.id from fee_heads as fh where " + 
			"fh.fee_fund_id = ? and fh.frequency = 'late_fee') and ftm.fee_memo_id = any(?)) as late_fee_amount, " + 
			"(select sum(paid_amount) from fee_payment_distributions as fpd inner join fee_transaction_payments as ftp " + 
			"on ftp.id = fpd.fee_transaction_payment_id where fpd.fee_memo_id = any(?) and ftp.mode = 'cash') as cash_paid_amount, " + 
			"(select sum(paid_amount) from fee_payment_distributions as fpd inner join fee_transaction_payments as ftp on ftp.id = fpd.fee_transaction_payment_id " + 
			"where fpd.fee_memo_id = any(?) and ftp.mode = 'cheque') as cheque_paid_amount";
	
	public static final String FEE_TRANSACTION_MEMO_DETAILS = 
			"select u.user_access_key as registration_number, ftmd.fee_head_id, ftmd.amount, ftmd.paid_amount, " + 
			"ftmd.discount from fee_transaction_memo_details as ftmd " + 
			"inner join fee_transaction_memos as ftm on ftm.id = ftmd.fee_transaction_memo_id " + 
			"inner join fee_memos fm on fm.id = ftm.fee_memo_id " + 
			"inner join users u on u.id = fm.user_id " + 
			"where ftm.fee_memo_id = any(?)";
	
	public static final String FEE_PAYMENT_DISTRIBUTIONS_DETAILS = 
			"select u.user_access_key as registration_number, fpd.paid_amount, ftp.mode  " + 
			"from fee_payment_distributions as fpd  " + 
			"inner join fee_transaction_payments as ftp " + 
			"on ftp.id = fpd.fee_transaction_payment_id  " + 
			"inner join fee_memos fm on fm.id = fpd.fee_memo_id " + 
			"inner join users u on u.id = fm.user_id " + 
			"where fpd.fee_memo_id = any(?) " + 
			"and (ftp.mode = 'cash' or ftp.mode = 'cheque')";
	
	public static final String FEE_HEAD = 
			"select fh.id as fee_head_id from fee_heads as fh where " + 
			"fh.fee_fund_id = ? and fh.frequency = ?";
}