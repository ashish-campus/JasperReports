import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

public class FeeReconcilationScriplet extends JRDefaultScriptlet {

	public String hello() throws JRScriptletException {
		return "Hello! I'm the report's scriptlet object.";
	}

	public List<FeeReconcilationData> getFeeData(Long feeFundId, Long academicYearId, String clientIdentifiers,
			Long organizationId, String academicYearKey) throws JRScriptletException {
		long startTime = System.nanoTime();
		System.out.println(startTime);
		
		System.out.println(feeFundId + " " + academicYearId  + " " + clientIdentifiers  + " " + organizationId
				 + " " + academicYearId);
		ConnectionUtil util = new ConnectionUtil();
		Connection fee_prod_conn = util.getFeeProductionConnection();
		Connection meta_conn = util.getMetcampusProductionConnection();
		try {
			Map<String, FeeReconcilationData> dataMap = util.getFeeStudentData(fee_prod_conn,
					Query.USER_DATA_FEE, feeFundId, academicYearId, clientIdentifiers, organizationId);
			dataMap = util.getMetaCampusStudentData(dataMap, meta_conn, Query.USER_DATA_METACAMPUS, academicYearKey);
			dataMap = util.getCashChequeLateDiscountAmountInMemory(dataMap, fee_prod_conn, feeFundId);
			dataMap = util.getGroupDueAmountInMemory(dataMap, fee_prod_conn, Query.GROUP_DUE_AMOUNT_WITH_REG_NUM, feeFundId,
					organizationId);
			
			long endTime = System.nanoTime();
			System.out.println(endTime);
			long timeElapsed = endTime - startTime;
			System.out.println(timeElapsed / 1000000 + " millisecond");
			return new ArrayList<>(dataMap.values());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new ArrayList<FeeReconcilationData>();
	}

	public List<String> getGroupNames(Long feeFundId) throws JRScriptletException {
		ConnectionUtil util = new ConnectionUtil();
		Connection fee_prod_conn = util.getFeeProductionConnection();
		List<String> groupNames = new ArrayList<>();
		try {
			groupNames = util.getGroupNames(fee_prod_conn, Query.GROUP_NAMES_FEE, feeFundId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return groupNames;
	}
}
