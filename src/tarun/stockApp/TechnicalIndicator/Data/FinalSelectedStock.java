package tarun.stockApp.TechnicalIndicator.Data;
import java.time.LocalDate;

public class FinalSelectedStock {

	public String stockCode;
	public double stockPrice;
	public LocalDate tradeddate;
	public float percentagePriceChange;
	public boolean PNSMAcrossover = false;
	public boolean SMNSMcrossover = false;
	public float percentageChangeInVolumeInLastDay;
	public String BBIndicator;
	public float rsiValue;
	public float chandelierExitLong;
	public float chandelierExitShort;
	
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
