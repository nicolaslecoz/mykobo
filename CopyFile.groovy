//def sourceDir="/media/nas-media/divers/books/"
def sourceDir="/tmp/books/"
def destDir="/tmp/test/"

listeExtensionAutorise = [ ".pdf" , ".epub", ".chm", ".cbr", ".cbz" ]

listeFichierAutorise = []

listeFichier = []

def copyFile(sourceFilename, destFilename) {
  copy = { File src,File dest-> 
    def input = src.newDataInputStream()
    def output = dest.newDataOutputStream()
 
    output << input 
 
    input.close()
    output.close()
  }
  
  File srcFile  = new File(sourceFilename)
  File destFile = new File(destFilename)

  new File(destFilename.replaceAll(/[^\/]*$/,"")).mkdirs()

  copy(srcFile,destFile)
}

def fileContientExtensionAutorise(filename) {
  result = false

  this.listeExtensionAutorise.each {
    if (filename.endsWith(it)) {
      result = true
    }
  }
  result
}

def filtrer(filename) {
  result = true

  if (filename.endsWith(".pdf")) {
    if (listeFichierAutorise.contains(filename.replaceAll(/\.pdf$/, ".epub"))) {
      result = false
    }
  }
  result
}

new File(sourceDir).eachFileRecurse() { file ->
  def filename = file.toString()
  if (fileContientExtensionAutorise(filename)) {
    listeFichierAutorise.add(filename)
  }
}

this.listeFichierAutorise.each {
  def filename = it.toString()

  if (filtrer(filename)) {
    listeFichier.add(filename)
  } else {
    println("Exclusion de $filename")
  }
}

listeFichier.each {
  def sourceFilename = it
  def destFilename = sourceFilename.replaceAll(/^$sourceDir/, destDir)

  destFilename = destFilename.replaceAll(/[éèëê]/, "e")
  destFilename = destFilename.replaceAll(/[öô]/, "o")
  destFilename = destFilename.replaceAll(/[àâä@]/, "a")
  destFilename = destFilename.replaceAll(/[ÿŷ]/, "y")
  destFilename = destFilename.replaceAll(/[ç]/, "c")
  destFilename = destFilename.replaceAll(/[&]/, "et")
  destFilename = destFilename.replaceAll(/['"]/, " ")

  println("Copie de $sourceFilename -> $destFilename")
  copyFile(sourceFilename, destFilename)
}



