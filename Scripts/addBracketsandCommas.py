from bs4 import BeautifulSoup

log = open('input.gpx', "r").readlines()
soup = BeautifulSoup(str(log), "html.parser")
lst = soup.find_all("trkpt")
open('output.txt', "w").write("")
for i in range(0, len(lst)):
    lat = lst[i].get("lat")
    lon = lst[i].get("lon")
    open('output.txt', "a").write("[" + lat + "," + lon + "]")
    if i + 1 < len(lst):
        open('output.txt', "a").write(",\n")
