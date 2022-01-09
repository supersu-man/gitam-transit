log = open('output.txt', "r").readlines()
print(log)
open('output1.txt', "w").write("")
for i in log:
    i = i.replace("\n","],\n")
    open('output1.txt', "a").write("["+i)