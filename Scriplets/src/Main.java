import java.util.List;

import net.sf.jasperreports.engine.JRScriptletException;

public class Main {
	public static void main(String ars[]) {
		FeeReconcilationScriplet scriplets = new FeeReconcilationScriplet();
		try {
			long startTime = System.nanoTime();
			List<FeeReconcilationData> datas = scriplets.getFeeData(Long.parseLong("29"), Long.parseLong("87"), "'academic','cosd','Examination_Paper','conveyance'", 
					Long.parseLong("24"), "6244964425007147");
			long endTime = System.nanoTime();
			
//			new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(
//					$P{FeeReconcilation_SCRIPTLET}.getFeeData(
//						$F{fee_fund_id}, $P{academicYearId}, $P{clientIdentifiers}, $F{organization_id}, $F{year_access_key}))
			
			long timeElapsed = endTime - startTime;
			System.out.println(timeElapsed / 1000000 + " millisecond");
		} catch (NumberFormatException | JRScriptletException e) {
			e.printStackTrace();
		}
	}
}
