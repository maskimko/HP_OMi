package learn
import groovy.xml.MarkupBuilder


class XMLBuilderTest {

	Writer writer;
	MarkupBuilder builder; 
	
	
	public static void main(String[] args){
		XMLBuilderTest xmlTest = new XMLBuilderTest();
		xmlTest.writer = new StringWriter();
		xmlTest.builder = new MarkupBuilder(xmlTest.writer);
		xmlTest.goTest();
		System.out.println(xmlTest.writer.toString());
	}
	
	
	private void goTest(){
		builder.records() {
			server(name:'om.sdab.sn', category:'Production'){
				services(name:'HPOM', category:'Production', type:'Monitoring'){
					node('om1.sdab.sn');
					node('om2.sdab.sn');
					node('om.sdab.sn');
				}
			}
		}
	}
	
}
