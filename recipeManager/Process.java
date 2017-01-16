package recipeManager;

import java.util.*;
import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Process {
	@XmlElement(name = "Name")
	private String name;
	@XmlElementWrapper(name = "InputMaterials")
	@XmlElement(name = "InputMaterial")
	private List<String> inputMatrerials;
	@XmlElementWrapper(name = "OutputMaterials")
	@XmlElement(name = "OutputMaterial")
	private List<String> outputMatrerials;

	public Process() {
		this.inputMatrerials = new ArrayList<String>();
		this.outputMatrerials = new ArrayList<String>();
	}

	public List<String> getInputMaterials() {
		return this.inputMatrerials;
	}

	public void setInputMaterials(List<String> materials) {
		this.inputMatrerials = materials;
	}

	public List<String> getOutputMaterials() {
		return this.outputMatrerials;
	}

	public void setOutputMaterials(List<String> materials) {
		this.outputMatrerials = materials;
	}

	public void setName(String name) {
		this.name = name;

	}

	public String getName() {
		return this.name;

	}
}
