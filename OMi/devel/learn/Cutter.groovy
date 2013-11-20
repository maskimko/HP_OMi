package learn

class Cutter {

	
	public static void main(String[] args){
		String toCut = "Test Incident ;lkjfa;slkjfdporewutwperjt;lsakjd;flnsa/v.,ncxz/.vm;'lkdj;lsjg;fdsugpwoir";
		int len = 15;
		if (toCut != null) {
			if (toCut.length() > len) {
				toCut = toCut.substring(0,len);
			}
		} else {
			toCut = "";
		}
		System.out.println(toCut.length());
		System.out.println(toCut);
	}
}
