module vrpServer
{
	//Make the following packages visible outside
	exports core;
	exports server;
	exports test;
	exports test.busExamples;

	//Declare the needed libraries
	requires graphhopper.web;
	requires java.json;
	requires json.simple;
	requires jsprit.core;
}