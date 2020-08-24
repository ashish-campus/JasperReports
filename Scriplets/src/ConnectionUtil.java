import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ConnectionUtil {
	public static final String METACAMPUS = "jdbc:mysql://localhost:3306/metacampus_college";
	public static final String METACAMPUS_USERNAME = "root";
	public static final String METACAMPUS_PASSWORD = "root";
	
	public static final String METACAMPUS_PROD = "jdbc:mysql://35.189.141.65:3306/metacampus_college";
	public static final String METACAMPUS_PROD_USERNAME = "support";
	public static final String METACAMPUS_PROD_PASSWORD = "ciitdc#123";
	
	public static final String FEE_PRODUCTION = "jdbc:postgresql://fee-production.cagbuud0sqow.us-east-1.rds.amazonaws.com:5432/fee_production";
	public static final String FEE_PRODUCTION_USERNAME = "jasper";
	public static final String FEE_PRODUCTION_PASSWORD = "1234";
	public static final String LATE_FREQUENCY = "late_fee";
	
	public Set<Long> feeMemoIdsSet = new HashSet<>();
//	public Map<String, Long[]> feeMemoIdsMap = new HashMap<>();

	public Connection getFeeProductionConnection() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return getConnection(ConnectionUtil.FEE_PRODUCTION, 
				ConnectionUtil.FEE_PRODUCTION_USERNAME, ConnectionUtil.FEE_PRODUCTION_PASSWORD);
	}
	
	public Connection getMetcampusConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return getConnection(ConnectionUtil.METACAMPUS, ConnectionUtil.METACAMPUS_USERNAME, 
				  ConnectionUtil.METACAMPUS_PASSWORD);
	}
	
	public Connection getMetcampusProductionConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return getConnection(ConnectionUtil.METACAMPUS_PROD, ConnectionUtil.METACAMPUS_PROD_USERNAME, 
				  ConnectionUtil.METACAMPUS_PROD_PASSWORD);
	}
	
	private Connection getConnection(String url, String userName, String password) {
		Connection connection = null;
		try {
			Properties props = new Properties();
			props.setProperty("user", userName);
			props.setProperty("password", password);
			connection = DriverManager.getConnection(url, props);
			System.out.println("Got it!");

		} catch (SQLException e) {
			throw new Error("Problem", e);
		} 
		return connection;
	}

	public Map<String, FeeReconcilationData> getFeeStudentData(Connection con, String query, 
			Long feeFundId, Long academicYearId, String clientIdentifiers, Long organizationId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(query);
			stmt.setLong(1, feeFundId);
			stmt.setLong(2, feeFundId);
			String[] identifiers = clientIdentifiers.split(",");
			List<String> identifiersCon = new ArrayList<>();
			for(String identifier : identifiers) {
				identifiersCon.add(identifier.substring(1, identifier.length() - 1));
			}
			Array clientIdentifiersArray = con.createArrayOf("VARCHAR", identifiersCon.toArray());
			stmt.setArray(3, clientIdentifiersArray);
			stmt.setLong(4, academicYearId);
			stmt.setLong(5, academicYearId);
			stmt.setLong(6, organizationId);
			
			ResultSet rs = stmt.executeQuery();
			Map<String, FeeReconcilationData> dataMap = new LinkedHashMap<>();
			int index = 1;
			List<String> groupNames = getGroupNames(con, Query.GROUP_NAMES_FEE, feeFundId);
			while (rs.next()) {
				FeeReconcilationData data = new FeeReconcilationData();
				data.initializeroupNameDueAmountMap(groupNames);
				data.setUserId(rs.getLong("user_id"));
				data.setRegistrationNumber(rs.getString("registration_number"));
				data.setEnrollmentNumber(rs.getString("enrollment_number"));
				data.setName(rs.getString("name"));
				data.setCourse(rs.getString("course"));
				data.setDeposit(rs.getLong("deposit_amount"));
				Array array = rs.getArray("fee_memo_ids");
				
				if(array != null) {
					Long[] feeMemoIds = (Long[]) array.getArray();
					data.setFeeMemoIds(feeMemoIds);
					feeMemoIdsSet.addAll(Arrays.asList(feeMemoIds));
				}
				System.out.println(index++ + " name: " + data.getName() + " array : " + array);
				dataMap.put(data.getRegistrationNumber(), data);
			}
//			System.out.println(feeMemoIdsSet);
			return dataMap;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return null;
	}

	public Map<String, FeeReconcilationData> getMetaCampusStudentData(Map<String, FeeReconcilationData> dataMap, Connection con, String query, String academicYearKey) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(query);
			stmt.setLong(1, Long.parseLong(academicYearKey));
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String registrationNumber = rs.getString("registrationnumber");
				FeeReconcilationData data = dataMap.get(registrationNumber);
				if(data == null) 
					continue;
				data.setDateOfJoining(rs.getDate("admissiondate"));
				
				String status = rs.getString("status");
				Date statusEffectiveDate = rs.getDate("statuseffectivedate");
				
				if("FINISHED".equalsIgnoreCase(status))
					data.setDateOfReleive(statusEffectiveDate);
				data.setSemester(rs.getInt("semester"));
				data.setYear(rs.getInt("year"));
				System.out.println("registrationNumber : " + registrationNumber
						+ " admissionDate : " + data.getDateOfJoining()
						+ " dateRelieve : " + data.getDateOfReleive()
						+ " semester : " + data.getSemester()
						+ " year : " + data.getYear());
			}
			return dataMap;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return null;
	}
	
	public Map<String, FeeReconcilationData> getGroupDueAmount(Map<String, FeeReconcilationData> dataMap, Connection con, String query, 
			Long feeFundId, Long organizationId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			int index = 1;
			for(FeeReconcilationData data : dataMap.values()) {
				if(data.getFeeMemoIds() == null)
					continue;
				stmt = con.prepareStatement(query);
				stmt.setLong(1, organizationId);
				
				stmt.setArray(2, con.createArrayOf("BIGINT", data.getFeeMemoIds()));
				stmt.setLong(3, feeFundId);
				ResultSet rs = stmt.executeQuery();
				List<Long> groupDueAmounts = new ArrayList<>();
				while (rs.next()) {
					groupDueAmounts.add(rs.getLong("due_amount"));
				}
				data.setGroupDueAmounts(groupDueAmounts);
				System.out.println(index++ + " " + groupDueAmounts);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return null;
	}
	
	public Map<String, FeeReconcilationData> getGroupDueAmountInMemory(Map<String, FeeReconcilationData> dataMap, Connection con, String query, 
			Long feeFundId, Long organizationId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(query);
			stmt.setLong(1, organizationId);
			stmt.setArray(2, con.createArrayOf("BIGINT", feeMemoIdsSet.toArray()));
			stmt.setLong(3, feeFundId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				String regNum = rs.getString("registration_number");
				String groupName = rs.getString("fee_head_group_name");
				Long dueAmount = rs.getLong("due_amount");
				FeeReconcilationData data = dataMap.get(regNum);
				Map<String, Long> groupNameDueAmountMap = data.getGroupNameDueAmountMap();
				if(groupNameDueAmountMap.get(groupName) == null) {
					groupNameDueAmountMap.put(groupName, new Long(0));
				}
				Long totalDueAmount = groupNameDueAmountMap.get(groupName) + dueAmount;
				groupNameDueAmountMap.put(groupName, totalDueAmount);
//				System.out.println(index++ + " registrationNumber : " + regNum + "groupName : " + groupName 
//						+ "amount" + dueAmount);
				
			}
			
			for(FeeReconcilationData data : dataMap.values()) { 
				System.out.println("registrationNumber : " + data.getRegistrationNumber() + " " 
			+ data.getGroupNameDueAmountMap().values());
			}
			System.out.print("end here");
			return dataMap;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return null;
	}
	
	public Map<String, FeeReconcilationData> getCashChequeLateDiscountAmount(Map<String, FeeReconcilationData> dataMap, Connection con, String query, Long feeFundId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			int index = 1;
			for(FeeReconcilationData data : dataMap.values()) {
				if(data.getFeeMemoIds() == null)
					continue;
				
				Array feeMemoIdsArray = con.createArrayOf("BIGINT", data.getFeeMemoIds());
				stmt = con.prepareStatement(query);
				stmt.setArray(1, feeMemoIdsArray);
				stmt.setLong(2, feeFundId);
				stmt.setArray(3, feeMemoIdsArray);
				stmt.setArray(4, feeMemoIdsArray);
				stmt.setArray(5, feeMemoIdsArray);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					data.setCash(rs.getLong("cash_paid_amount"));
					data.setCheque(rs.getLong("cheque_paid_amount"));
					data.setLateFee(rs.getLong("late_fee_amount"));
					data.setDiscount(rs.getLong("discount"));
				}
				System.out.println(index++ + " cash : " + data.getCash() + " cheque : " + data.getCheque()
				+ " late fee : " + data.getLateFee() + " discount : " + data.getDiscount());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return null;
	}
	
	public Map<String, FeeReconcilationData> getCashChequeLateDiscountAmountInMemory(Map<String, FeeReconcilationData> dataMap, Connection con, Long feeFundId) throws SQLException {
		getFeeTransactionMemoDetailsInMemory(dataMap, con, feeFundId);
		getFeePaymentDistributionsInMemory(dataMap, con);
		for(FeeReconcilationData data : dataMap.values()) { 
			System.out.println("registrationNumber : " + data.getRegistrationNumber() + " " 
		+ " cash : " + data.getCash() + " cheque : " + data.getCheque() +
		" discount : " + data.getDiscount() + " late_fee : " + data.getLateFee());
		}
		System.out.print("end here");
		return dataMap;
	}
	
	private Map<String, FeeReconcilationData> getFeeTransactionMemoDetailsInMemory(Map<String, FeeReconcilationData> dataMap, Connection con, Long feeFundId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(Query.FEE_TRANSACTION_MEMO_DETAILS);
			stmt.setArray(1, con.createArrayOf("BIGINT", feeMemoIdsSet.toArray()));
			ResultSet rs = stmt.executeQuery();
			Long lateFeeHeadId = getFeeHead(con, feeFundId, LATE_FREQUENCY);
			while(rs.next()) {
				String regNum = rs.getString("registration_number");
				Long feeHeadId = rs.getLong("fee_head_id");
				Long paidAmount = rs.getLong("paid_amount");
				Long discount = rs.getLong("discount");
				FeeReconcilationData data = dataMap.get(regNum);
				data.setDiscount(data.getDiscount() + discount);
				if(feeHeadId.equals(lateFeeHeadId)) {
					data.setLateFee(data.getLateFee() + paidAmount);
				}
			}
			return dataMap;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return null;
	}
	
	private Map<String, FeeReconcilationData> getFeePaymentDistributionsInMemory(Map<String, FeeReconcilationData> dataMap, Connection con) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(Query.FEE_PAYMENT_DISTRIBUTIONS_DETAILS);
			stmt.setArray(1, con.createArrayOf("BIGINT", feeMemoIdsSet.toArray()));
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				String regNum = rs.getString("registration_number");
				Long paidAmount = rs.getLong("paid_amount");
				String mode = rs.getString("mode");
				FeeReconcilationData data = dataMap.get(regNum);
				if("cash".equalsIgnoreCase(mode))
					data.setCash(data.getCash() + paidAmount);
				
				if("cheque".equalsIgnoreCase(mode))
					data.setCheque(data.getCheque() + paidAmount);
			}
			return dataMap;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return null;
	}
	
	public Long getFeeHead(Connection con, Long feeFundId, String frequency) throws SQLException {
		Long feeHeadId = null;
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(Query.FEE_HEAD);
			stmt.setLong(1, feeFundId);
			stmt.setString(2, frequency);			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				feeHeadId = rs.getLong("fee_head_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return feeHeadId;
	}
	
	public List<String> getGroupNames(Connection con, String query, 
			Long feeFundId) throws SQLException {
		List<String> groupNames = new ArrayList<>();
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(query);
			stmt.setLong(1, feeFundId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				groupNames.add(rs.getString("group_name"));
			}
			return groupNames;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return null;
	}
}
