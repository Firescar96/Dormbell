if (Meteor.isClient) {
  Template.hello.greeting = function () {
    return "Welcome to Backend.";
  };

  Template.hello.events({
    'click input': function () {
      // template data, if any, is available in 'this'
      if (typeof console !== 'undefined')
        console.log("You pressed the button");
    }
  });
  
  rests = new Meteor.Collection("RESTS")

	Meteor.subscribe(function() {
 		rests.find().observe({
   		added: function(item){
			window.location = "com.firescar96.nom.appUser";
    		}
 	 	});
	});
}

if (Meteor.isServer) {

	Users = new Meteor.Collection('users');
	Events = new Meteor.Collection('events');

	Router.map(function () {
		this.route("/", {
	    		where: "server",
	    		action: function(){
	      		console.log('################################################');
			      //console.log(this.request.method);
			      //console.log(this.request.headers);

			      //console.log('------------------------------');
			      console.log(this.request.body);
			      //console.log('------------------------------');

			      this.response.statusCode = 200;
			      this.response.setHeader("Content-Type", "application/json");
			      this.response.setHeader("Access-Control-Allow-Origin", "*");
			      this.response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

		     		if (this.request.method == 'POST') {
					HandleData(this.request.body);
					this.response.writeHead(200, {'Content-Type': 'text/plain'});
					this.response.end("");
		      	}

		      	if (this.request.method == 'GET') {
			      	if(this.request.query.checkName != null)
			      	{
			      		console.log(this.request.query.checkName);
			      		var resUsr = Users.findOne({username:this.request.query.checkName});
			      		var exists = resUsr != undefined;
			      		this.response.writeHead(200, {'Content-Type': 'text/plain'});
			      		if(this.request.query.regId != null)
			      		{
			      			if(exists && resUsr.regId == this.request.query.regId) //return true if matching user found, or if the requesting user is in the database
								this.response.end("true");
							else
								this.response.end("false");
						}
						else
							this.response.end(exists.toString());
					}      
	      		}
	    		}
	  	});
	});

		
	var gcm = Meteor.npmRequire('node-gcm');
	var sender = new gcm.Sender('AIzaSyAk_PxK_3WfDeFQOL0fDpPpqaA5scekrEk');//TODO: Make this a legit apikey

	var HandleData = function(query)
	{
		//Users.remove({}); Should this line even exist?
		if(query.update == true)
		{
			if(query.lock != undefined)
			{
				Users.update({username: query.username}, {$set: {lock: query.lock}});	
				console.log("Updated user: " + query.username + " locks");
			}

			if(query.location != undefined)
			{
				Users.update({username: query.username}, {$set: {location: query.location}});	
				console.log("Updated user: " + query.username + " location");
				//console.log(Users.find({}).fetch());
			}
			return;
		}

		if(query.username != undefined && query.regId != undefined) {
			nameUser = Users.findOne({username:query.username});
			regIdUser = Users.findOne({username:query.regId});
			if (nameUser == undefined && regIdUser == undefined)
					Users.insert({username: query.username, regId: query.regId});	
			else if (nameUser == undefined)
				if(regIdUser != undefined)
					Users.update({regId: query.regId}, {$set: {username: query.username}});
			else if (regIdUser == undefined)
				if(nameUser != undefined)
					Users.update({username: query.username}, {$set: {regId: query.regId}});
			return;
		}

		var allUsr = Users.find({}).fetch();
		toUsr = []
		for(var i in toUsr)
		{
			if(allUsr[i].indexOf(query.lock) != -1)
				toUsr.push(allUsr[i]);
		}
		
		/*if(Events.findOne({hash:query.event.hash}) == undefined) //TODO: save events to be sent when a user reconnets
		{
			Events.insert({
				privacy:query.event.privacy,
	    		location:query.event.location,
	    		date:query.event.date,
	    		hash:query.event.hash,
	    		host:query.event.host
		    });
		}*/

		console.log(toUsr)	
		var registrationIds = [];
		for(var i in toUsr)
		{
			console.log(toUsr[i]);
			if (toUsr[i] == null) 
				continue;
			
			var hostUsr = Users.findOne({username:query.username});
			//console.log(toUsr[i].location);
			//console.log(Users.findOne({username:query.event.host}));
			if(toUsr[i].location.latitude != undefined)
			{
				var latPow = Math.pow(parseInt(hostUsr.location.latitude)-parseInt(toUsr[i].location.latitude),2);
				var longPow = Math.pow(parseInt(hostUsr.location.longitude)-parseInt(toUsr[i].location.longitude),2);
				var dist = Math.sqrt(latPow+longPow);
				if(dist > 1)
					continue;
			}	
			
			registrationIds.push(toUsr[i].regId);
		}

		var message = new  gcm.Message();
		if(query.ring == true)
		{
			message.sender = "\""+query.sender+"\"",
			message.lock="\""+query.lock+"\""
		}
		
		sender.send(message, registrationIds, 10, function (err, result) {
	  		if(err) console.error(err);
	  		else    console.log(result);
		});
	}
}
