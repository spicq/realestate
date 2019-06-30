# realestate
web crawler dedicated to realestate, retrieving data from many websites into a database. Data can then be queried on many different criteria, like price per square meter etc...

To Start without a database:
* Edit the realestate.properties and :
    * remove info on database connection
    * edit urls from realestate web sites you want to explore. For now just seloger is supported so you should enter a seloger search url
    
* Compile and run => A csv file will be generated with all data extracted for the given url

To Start with a database:
* You must have postgres installed and running
* Create a database realestate (for instance) and run the create-all.sql script provided
* Edit the realestate.properties and:
    * specify the database connection properties
    * edit urls from realestate web sites you want to explore. For now just seloger is supported so you should enter a seloger search url
    
* Compile and run => The realestate table will be filled in with all the data extracted for the given url
