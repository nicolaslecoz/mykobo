import groovy.sql.Sql

def today = String.format('%tY-%<tm-%<tdT%<tTZ', new Date())

// Les clés sont triés par les étagères les moins prioritaires au plus prioritaires
assoShelfWithContentId = [
    'IT' : /file:\/\/\/mnt\/sd\/it\/.*\.(pdf|epub)/,
    'René Barjavel' : /file:\/\/\/mnt\/sd\/roman\/Rene Barjavel.*\.epub/, 
    'Herbert George Wells' : /file:\/\/\/mnt\/sd\/roman\/Herbert George Wells.*\.epub/,
    'Cuisine' : /file:\/\/\/mnt\/sd\/cuisine\/.?\..?/,
    'Nicholas Starks' : /file:\/\/\/mnt\/sd\/roman\/Nicholas Sparks.*\.epub/,
    'BD - Divers' : /file:\/\/\/mnt\/sd\/bd\/.*\.cb[rz]/,
    'BD - Marvel' : /file:\/\/\/mnt\/sd\/bd\/Ultimate Universe.*\/.*\.cb[rz]/,
    'BD - Asterix le gaulois' : /file:\/\/\/mnt\/sd\/bd\/Asterix le gaulois\/.*\.cb[rz]/,
    'BD - Tintin et Milou' : /file:\/\/\/mnt\/sd\/bd\/Tintin et Milou\/.*\.cb[rz]/,
    'Christian Jacq' : /file:\/\/\/mnt\/sd\/roman\/Christian Jacq.*\.epub/,
    'Bernard Werber' : /file:\/\/\/mnt\/sd\/roman\/Bernard Werber.*\.epub/,
    'Dan Brown' : /file:\/\/\/mnt\/sd\/roman\/Dan Brown.*\.epub/,
    'Revues diverses' : /file:\/\/\/mnt\/sd\/divers\/.?\..?/
]

def computeShelfFromContentId(contentId) {  
  def result = ""

  this.assoShelfWithContentId.each{
    if (contentId ==~ it.value) {
      result = it.key
    }
  }
  result
}

def sourceFolder="/media/nicolas/6433-3035/"
def destFolder="file:///mnt/sd/"

println "Mise à jour des étagères du répertoire $sourceFolder au $destFolder"

this.getClass().classLoader.rootLoader.addURL(
  new File("sqlite-jdbc-3.7.2.jar").toURL())

def sql = Sql.newInstance( 'jdbc:sqlite:KoboReader.sqlite', 
  'org.sqlite.JDBC' )

// Effacer le contenu des étagères
sql.execute("delete from ShelfContent")
sql.execute("delete from Shelf")

// Création des étagères

assoShelfWithContentId.each{
  sql.execute('insert into Shelf(CreationDate,Id,InternalName,LastModified,Name,_IsDeleted,_IsVisible,_IsSynced) values(?,?,?,?,?,?,?,?)',
      [today,it.key,it.key,today,it.key,'false','true','false'])
}


// Remplissage des étagères

def file = new File('liste_fichier.txt')

def listeFichier = []

file.eachLine { line ->
    listeFichier.add(line.replaceAll(sourceFolder, destFolder))
}

def listeRowShelfContent = []

/*
// Dans le cas des mises à jour - je pense que cette technique sera abandonné
sql.rows("select * from ShelfContent").each{
  listeRowShelfContent.add(it)
}
*/

listeRowShelfContent.each{i ->
  listeFichier.remove(i.ContentId)
}

listeFichier.each{i ->
  def shelfSelected = computeShelfFromContentId(i)

  if (shelfSelected != "") {
    def contentId = i.replaceAll("'", "\'")

    println "tentative d'ajout de $contentId"

    sql.execute('insert into ShelfContent(ShelfName,ContentId,DateModified,_IsDeleted,_IsSynced) values(?,?,?,?,?)',
      [shelfSelected,contentId,today,'false','false'])

    println "  => ajout réussi dans l'étagère $shelfSelected"

  } else {
    println "pas de correspondance d'étagère trouvé pour $i"
  }
}

sql.close()
