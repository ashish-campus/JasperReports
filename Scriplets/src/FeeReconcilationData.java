import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeeReconcilationData {
	private Long userId;
	private String registrationNumber;
	private String enrollmentNumber;
	private String name;
	private String course;
	private Integer year;
	private Integer semester;
	private Date dateOfJoining;
	private Date dateOfReleive;
	private Long cash = new Long(0);
	private Long cheque = new Long(0);
	private Long lateFee = new Long(0);
	private Long discount = new Long(0);
	private Long deposit = new Long(0);
	private Long[] feeMemoIds;
	private List<Long> groupDueAmounts;
	private Map<String, Long> groupNameDueAmountMap = new LinkedHashMap<>();
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getRegistrationNumber() {
		return registrationNumber;
	}
	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}
	public String getEnrollmentNumber() {
		return enrollmentNumber;
	}
	public void setEnrollmentNumber(String enrollmentNumber) {
		this.enrollmentNumber = enrollmentNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCourse() {
		return course;
	}
	public void setCourse(String course) {
		this.course = course;
	}
	
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getSemester() {
		return semester;
	}
	public void setSemester(Integer semester) {
		this.semester = semester;
	}
	public Date getDateOfJoining() {
		return dateOfJoining;
	}
	public void setDateOfJoining(Date dateOfJoining) {
		this.dateOfJoining = dateOfJoining;
	}
	public Date getDateOfReleive() {
		return dateOfReleive;
	}
	public void setDateOfReleive(Date dateOfReleive) {
		this.dateOfReleive = dateOfReleive;
	}
	public Long getCash() {
		return cash;
	}
	public void setCash(Long cash) {
		this.cash = cash;
	}
	public Long getCheque() {
		return cheque;
	}
	public void setCheque(Long cheque) {
		this.cheque = cheque;
	}
	public Long getLateFee() {
		return lateFee;
	}
	public void setLateFee(Long lateFee) {
		this.lateFee = lateFee;
	}
	public Long getDiscount() {
		return discount;
	}
	public void setDiscount(Long discount) {
		this.discount = discount;
	}
	public Long getDeposit() {
		return deposit;
	}
	public void setDeposit(Long deposit) {
		this.deposit = deposit;
	}
	public Long[] getFeeMemoIds() {
		return feeMemoIds;
	}
	public void setFeeMemoIds(Long[] feeMemoIds) {
		this.feeMemoIds = feeMemoIds;
	}
	public List<Long> getGroupDueAmounts() {
		return groupDueAmounts;
	}
	public void setGroupDueAmounts(List<Long> groupDueAmounts) {
		this.groupDueAmounts = groupDueAmounts;
	}
	public Map<String, Long> getGroupNameDueAmountMap() {
		return groupNameDueAmountMap;
	}
	public void setGroupNameDueAmountMap(Map<String, Long> groupNameDueAmountMap) {
		this.groupNameDueAmountMap = groupNameDueAmountMap;
	}
	public void initializeroupNameDueAmountMap(List<String> groupNames) {
		for(String groupName : groupNames) {
			this.groupNameDueAmountMap.put(groupName, new Long(0));
		}
	}
	@Override
	public String toString() {
		return "FeeReconcilationData [userId=" + userId + ", registrationNumber=" + registrationNumber
				+ ", enrollmentNumber=" + enrollmentNumber + ", name=" + name + ", course=" + course + ", year=" + year
				+ ", semester=" + semester + ", dateOfJoining=" + dateOfJoining + ", dateOfReleive=" + dateOfReleive
				+ ", cash=" + cash + ", cheque=" + cheque + ", lateFee=" + lateFee + ", discount=" + discount
				+ ", deposit=" + deposit + ", feeMemoIds=" + feeMemoIds + "]";
	}
	
	
}
