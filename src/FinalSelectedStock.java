import java.time.LocalDate;

public class FinalSelectedStock {

	String stockCode;
	double stockPrice;
	LocalDate tradeddate;
	float percentagePriceChange;
	boolean PNSMAcrossover = false;
	boolean SMNSMcrossover = false;
	float percentageChangeInVolumeInLastDay;
	String BBIndicator;
	float rsiValue;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stockCode == null) ? 0 : stockCode.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FinalSelectedStock other = (FinalSelectedStock) obj;
		if (stockCode == null) {
			if (other.stockCode != null)
				return false;
		} else if (!stockCode.equals(other.stockCode))
			return false;
		return true;
	} 
	
	
}
