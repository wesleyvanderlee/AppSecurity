#!/usr/bin/python
# Exec: ./shortlabels.py new_graph 
import sys
from pprint import pprint
#applicationid = "com.whatsapp:id"
#applicationid = "whyuas.fullversion.update2017:id"
applicationid = "nl.negentwee:id"	#for delimiterpurpose
#applicationid = "com.android.insecurebankv2"
print "Shortening for " + applicationid

if not len(sys.argv) is 2:
	print "No input name"
else:
	infile = str(sys.argv[1])
	destfile = open(infile + '_shortened','w')
	with open(infile, 'r') as f:
		for line in f:
			if '->' in line and '__start' not in line:
				res = ''
				split1 = line.split('%',1)
				action = split1[0]
				if '@resource-id' in line:
					split2 = line.split('@resource-id')[-1]
					if applicationid in split2: # grab resource-id
						id = split2.split('/')[1].split('\'')[0]
						result = split2.split('/')[-1].split('"')[0]
					elif split2.startswith("='' and contains(@text, '') and @content-desc='']"): 					#No info	
						continue
					elif "@content-desc=''" not in split2: #grab desc
						id = split2.split("@content-desc='")[1].split('\'')[0]
						result = split2.split('/')[-1].split('"')[0]
					else:
						raise Exception("Unprocessed line for: %s\n" + line)

					# only for updatewhatsapp
	#				if applicationid in split2 and "contains(@text, \'\'" not in split2:
	#				id = split2.split("contains(@text, \'")[-1].split("\') and @")[0]
	#				result = split2.split('/')[-1].split('"')[0]
					argument = ""
					res = action + "-" +  id + "-" + result + "\"];\n"
					if "enterText" in line:
						argument = line.split("#")[-1].split(" / ")[0]
						res = action + "-" +  id + "#" +argument + "/" + result + "\"];\n"
				

				else:
					res = line

				destfile.write(res)
			else:
				destfile.write(line)
