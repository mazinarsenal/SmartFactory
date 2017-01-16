package recipeManager;

import java.io.File;
import java.util.*;
import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Recipe {
	@XmlElementWrapper(name = "Processes")
	@XmlElement(name = "Process")
	private List<Process> processes;

	public Recipe() {
		this.processes = new ArrayList<Process>();
	}

	public List<Process> getProcesses() {
		return this.processes;
	}

	public void setProcess(List<Process> processes) {
		this.processes = processes;
	}

}