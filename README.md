To run from eclipse you need to have the ejade plugin installed (http://selab.fbk.eu/dnguyen/ejade/)
For the first time right click the project and select toggle JADE nature
To stat a single agent
start EJADE
then create a run configuration as follows:
Main class: jade.Boot
Argument: -container -port 1099 -host localhost yourAgentName:yourAgentpackage.YourAgentClassName