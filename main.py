def bldChar(race, plyName):
    player = {
        "race" : "none",
        "playerName" : plyName,
        "skills" : {
            "mining" : {
                "astroid" : 0,
                "comet" : 0,
                "planet" : 0,
                "tools" : {
                    "laser" : 0,
                    "explosive" : 0,
                    "cracker" : 0
                    }
                }
            }
        }
        
    if race == 1:
        player.update({"race" : "Makrath"})
        
    if race == 2:
        player.update({"race" : "Ulinf"})
        
    if race == 3:
        player.update({"race" : "Erlosh"})
    
    elif race != 1 and race != 2 and race != 3:
        print("Please pick valid race")
    
    print ("You have chosen " + player["race"] + ", is this correct " + plyName + "?")
    return player
    
def getMiningSk(ply):
    skills = ply["skills"]
    return skills["mining"]
    
def getMToolSk(ply):
    mining = getMiningSk(ply)
    return mining["tools"]
    
def learnSk(ply, skType, skName):
    if skType == "mining":
        if skName == "astroid":
            mining = getMiningSk(ply)
            update = mining["astroid"] + 1
            mining.update({"astroid" : update})
        if skName == "comet":
            mining = getMiningSk(ply)
            update = mining["comet"] + 1
            mining.update({"comet" : update})
        if skName == "planet":
            mining = getMiningSk(ply)
            update = mining["planet"] + 1
            mining.update({"planet" : update})

if __name__ == "__main__":
    ply = bldChar(1, "Trux")
    print(ply)
        
