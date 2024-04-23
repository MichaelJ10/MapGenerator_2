import pymeshlab
import sys

arguments = {}

args = sys.argv
 
for i in range(1,len(args),2):
    arguments[args[i]] = args[i+1]
    

input = ''
output = ''
script = ''

try:
    input = arguments['-i']
except:
    raise Exception("no input file")

try:
    script = arguments['-s']
except:
    raise Exception("no script file")

try:
    output = arguments['-o']
except:
    output = input

ms = pymeshlab.MeshSet()
ms.load_new_mesh(input)
ms.load_filter_script(script)
ms.apply_filter_script()
ms.save_current_mesh(output)

print("output file at: " + output)
