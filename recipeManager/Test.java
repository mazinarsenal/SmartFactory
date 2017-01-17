package recipeManager;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class Test {

	private static final String xmlFilePath = "c:\\Users\\Mazin\\workspace\\SmartFactory\\recipes\\assembledBearingBox.xml";

	public static void main(String[] args) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(Recipe.class);
		File xmlfile = new File(xmlFilePath);

		Recipe recipe = new Recipe();
		Process assemble = new Process();
		assemble.setName("assembleBearingBox");
		assemble.getInputMaterials().add("Bearing");
		assemble.getInputMaterials().add("Box");
		assemble.getInputMaterials().add("AssemblyTray");
		assemble.getOutputMaterials().add("AssembledBearingBox");
		assemble.getOutputMaterials().add("AssemblyTray");

		recipe.getProcesses().add(assemble);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		// marshaller.marshal(recipe, System.out);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		// unmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(recipe, xmlfile);

		Recipe recipe2 = (Recipe) unmarshaller.unmarshal(xmlfile);
		System.out.print("hello");
	}
}
