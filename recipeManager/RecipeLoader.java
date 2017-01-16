package recipeManager;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class RecipeLoader {
	String path; // Path to the recipe folder

	public RecipeLoader(String path) {
		this.path = path;

	}

	public Recipe load(String itemName) {

		JAXBContext jc;
		Recipe recipe = new Recipe();
		try {
			jc = JAXBContext.newInstance(Recipe.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			File xmlfile = new File(this.path + itemName + ".xml");
			recipe = (Recipe) unmarshaller.unmarshal(xmlfile);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return recipe;

	}
}
