package tarun.stockApp.TechnicalIndicator.Data;
import java.util.ArrayList;

public class OnBalanceVolumeData {
	public String stockName;
	public ArrayList<String> tradeddate;
	public ArrayList<Float> closePrice;
	public ArrayList<Long> volume;
	public ArrayList<Long> onBalanceVolume;
	float percentageChange;
}
