from flask import Flask, request, jsonify
import math
import ast

app = Flask(__name__)

silent = {"array":[]}

global test_data
test_data = {}

file = open("silentzones.txt", 'r')
db = file.read()
silent = ast.literal_eval(db)
file.close()

def calcDist(A,B):
    A[1] = math.radians(A[1])
    B[1] = math.radians(B[1])
    A[0] = math.radians(A[0])
    B[0] = math.radians(B[0])
    #Haversine Formula
    dlon = B[1]-A[1]
    dlat = B[0]-A[0]
    a1 = math.sin(dlat / 2.0)**2 + math.cos(A[0]) * math.cos(B[0]) * math.sin(dlon / 2.0)**2
    c = 2 * math.asin(math.sqrt(a1))
    # Radius of earth in kilometers
    r = 6371
    return(c *r*1000.0)

def inZone(X):
    for i in silent["array"]:
        b = [i['centerx'],i['centery']]
        rad = i['radius']
        tria = X[:]
        trib = b[:]
        if calcDist(tria,trib)<=rad:
            return True
    return False

@app.route('/')
def welcome():
    return jsonify(silent)

@app.route('/testget')
def testresponse():
    global test_data
    return jsonify(test_data)

@app.route('/add', methods=['POST'])
def add():
    data = request.get_json(force=True)
    silent["array"].append(data)
    file = open("silentzones.txt", 'w')
    file.write(str(silent))
    file.close()
    return "Success!"

@app.route('/get', methods=['POST'])
def get():
    loc_data = request.get_json(force=True)
    global test_data
    test_data = loc_data
    x = loc_data['x']
    y = loc_data['y']
    a = [x,y]
    if inZone(a):
        return "silent"
    return "none"

@app.route('/remove', methods=['POST'])
def delete():
    del_data = request.get_json(force=True)
    x = del_data['x']
    y = del_data['y']
    a = [x,y]
    for i in silent["array"][:]:
        b = [i['centerx'],i['centery']]
        if a == b:
            silent["array"].remove(i)
            file = open("silentzones.txt", 'w')
            file.write(str(silent))
            file.close()
            return "success"
    return "failure"




if __name__ == "__main__":
    app.run(debug=True)