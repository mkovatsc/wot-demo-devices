#if(FUNCTIONALITY == 1)

static char  SEMANTICS_SWITCH[] = "# Switch\n\
# Standard namespaces \n\
@prefix local: <local#>.\n\
@prefix : <ex#>.\n\
@prefix ex: <http://example.org/#>.\n\
@prefix http: <http://www.w3.org/2011/http#>.\n\
@prefix st: <http://purl.org/restdesc/states#>.\n\
@prefix log: <http://www.w3.org/2000/10/swap/log#>.\n\
@prefix owl: <http://www.w3.org/2002/07/owl#>.\n\
local:switch a :switch.\n\
local:devicelocation a :question;\n\
 :text (\"Where is the switch located?\");\n\
	:replyType :location.\n\
{\n\
	local:devicelocation :hasAnswer ?a\n\
} => {\n\
	local:switch :locatedAt  ?a.\n\
}.\n\
{\n\
	local:switch :locatedAt  ?a.\n\
} => {\n\
	local:devicelocation :hasAnswer ?a\n\
}.\n\
{\n\
	?state a st:State;\n\
	log:includes {local:state :power :off}.\n\
	?url a local:url.\n\
	local:switch :locatedAt ?location.\n\
}\n\
=>\n\
{\n\
	_:request http:methodName \"PUT\";\n\
	http:requestURI (?url \"/status\");\n\
	http:reqBody \"1\".\n\
	[ a st:StateChange;\n\
	st:replaced { local:state :power :on.  };\n\
	st:parent ?state ].\n\
}.\n\
{\n\
	?state a st:State;\n\
	log:includes {local:state :power :on}.\n\
	?url a local:url.\n\
	local:switch :locatedAt ?location.\n\
}\n\
=>\n\
{\n\
	_:request http:methodName \"PUT\";\n\
	http:requestURI (?url \"/status\");\n\
	http:reqBody \"0\".\n\
	[ a st:StateChange;\n\
	st:replaced { local:state :power :off. };\n\
	st:parent ?state ].\n\
}.\n\
{\n\
	local:switch :locatedAt ?location.\n\
	?state a st:State;\n\
			log:notIncludes {local:state :power ?xx.}.\n\
	local:state :power ?powerstate.\n\
}=>{\n\
	[ a st:StateChange;\n\
		st:replaced {local:state :power ?powerstate. };\n\
		st:parent ?state ].\n\
}.\n\
{\n\
	local:switch :locatedAt  ?a.\n\
} => {\n\
	?a :hasAction :switch.\n\
}.";


static char  SEMANTICS_SWITCH_STATUS_ON[] ="# Switch (State)\n\
@prefix local: <local#>.\n\
@prefix : <ex#>.\n\
local:state :power :on.\n";

static char  SEMANTICS_SWITCH_STATUS_OFF[] ="# Switch (State)\n\
@prefix local: <local#>.\n\
@prefix : <ex#>.\n\
local:state :power :off.\n";

#else


static char  SEMANTICS_SWITCH[] = "# Switch\n\
# Standard namespaces \n\
@prefix local: <local#>.\n\
@prefix : <ex#>.\n\
@prefix ex: <http://example.org/#>.\n\
@prefix http: <http://www.w3.org/2011/http#>.\n\
@prefix st: <http://purl.org/restdesc/states#>.\n\
@prefix log: <http://www.w3.org/2000/10/swap/log#>.\n\
@prefix owl: <http://www.w3.org/2002/07/owl#>.\n\
local:presence a :presence.\n\
local:devicelocation a :question;\n\
 :text (\"Where is the presence sensor located?\");\n\
	:replyType :location.\n\
{\n\
	local:devicelocation :hasAnswer ?a\n\
} => {\n\
	local:presence :locatedAt  ?a.\n\
}.\n\
{\n\
	local:presence :locatedAt  ?a.\n\
} => {\n\
	local:devicelocation :hasAnswer ?a\n\
}.\n\
{\n\
	local:presence :locatedAt ?location.\n\
	local:state :presence ?powerstate.\n\
}=>{\n\
	?location :presence ?powerstate.\
}.\n\
{\n\
	local:presence :locatedAt  ?a.\n\
} => {\n\
	?a :hasAction :presence.\n\
}.";


static char  SEMANTICS_SWITCH_STATUS_ON[] ="# Switch (State)\n\
@prefix local: <local#>.\n\
@prefix : <ex#>.\n\
local:state :presence :on.\n";

static char  SEMANTICS_SWITCH_STATUS_OFF[] ="# Switch (State)\n\
@prefix local: <local#>.\n\
@prefix : <ex#>.\n\
local:state :presence :off.\n";

#endif

