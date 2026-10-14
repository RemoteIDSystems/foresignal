// A hand-made KiwiSDR list: comments, trailing commas, single quotes and unquoted names
/* a block comment
   spanning lines, with "quotes" and // slashes */
var kiwisdr_com =
[
	{
		"id":"aaaa00000001", // the first receiver
		"name":"Test receiver // not a comment",
		'url':'http://example.org:8073',
		antenna:"Dipole /* not a comment either */",
		"bands":"0-30000000",
		"users":"1",
		"users_max":"4",
		"gps":"(51.5, -0.1)",
		"offline":"no",
		"snr":"20,18",
		"sdr_hw":"KiwiSDR 2",
	},
	{
		"id":"aaaa00000002",
		"name":"Offline, no position",
		"gps":"",
		"offline":"yes",
		"bands":"bad",
		"users_max":"many",
	},
	{
		"name":"no id",
	},
	{
		"id":"aaaa00000001",
		"name":"duplicate id",
	},
]
;
