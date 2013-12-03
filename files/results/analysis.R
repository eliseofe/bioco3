require(plyr)
require(xtable)

analyze <- function(dir,f){
  print(paste("Parsing ",f))
  
  solName <- unlist(strsplit(f,'_'))[1]
  filename <- paste(dir,"/",f,sep='')
  dataResults <- as.data.frame(read.csv(file=filename,header=TRUE,colClasses=c("numeric")))
  
  print("Before"); 
  print(dataResults)
   
  drops <- c("seed","frequency","duration")
  dataResults <- dataResults[,!(names(dataResults) %in% drops)]

  #dataResults[,1]=as.numeric(dataResults[,1])
  #dataResults[,6]=as.numeric(dataResults[,6])
  #dataResults=matrix(as.numeric(unlist(dataResults)),nrow=nrow(dataResults))
  
  print("After");
  print(dataResults)
  
  #overview <- ddply(dataResults, 'instance',function(x) c(mean=mean(x),sd=sd(x)))
  #dataResults=sapply(dataResults,as.numeric)
  overview <- ddply(dataResults, as.quoted(colnames(dataResults)),function(x) c(mean=mean(x),sd=sd(x)))
  overview <- ddply(dataResults,.(instance),function(x) c(mean=mean(x),sd=sd(x)))
  #overview <- ddply(dataResults, .(instance),function(x) c(mean=mean(x),sd=sd(x)))
  #overview <- ddply(dataResults, .(instance), function(x) c(mean=mean(x, na.rm = TRUE),sd=sd(x)))
  #overview <- ddply(dataResults, .(instance), summarize, c("mean","sd"))
  #overview <- ddply(dataResults, 'instance',function(x) c(mean=mean(as.double(x)),sd=sdclass((as.double(x))))
  
  drops2 <- c("mean.instance","sd.instance")
  overview <- overview[,!(names(overview) %in% drops2)]
   
  outputFileName <- paste(dir,"/",f ,".html",sep='')
  f<- file(outputFileName,"w")
  print(paste("Written", solName,"to:",outputFileName))
  
  print(xtable(overview),f,type="html")
  close(f)
  
  # only select cost and change name to solution
  cost <- overview["mean.cost"]
  colnames(cost) <- c(solName)

  # add column mean (mean over all files)
  newCost <- rbind(cost,mean(cost))
  return(c(solName,newCost))
}


#setwd(dirname(parent.frame(2)$ofile))
setwd("~/git/bioco3/files/results")

print(paste("Parsing",getwd()))

dirs <- dir(path=".",pattern="gendreau",recursive=FALSE)

for( dir in dirs){
  # only return *.txt files
  files <- list.files(path=dir,pattern="\\.txt$")
  
  dirDf <- data.frame(instance=c(1,2,3,'mean'),row.names=1)
  #print(files)
  for( f in files){
    res <- analyze(dir,f)
    dirDf[,paste(res[1])] <- res[2]
  }
  #print(dirDf)
  outputFileName <- paste(dir,"/overview.html",sep='')
  f<- file(outputFileName,"w")
  print(xtable(dirDf),f,type="html")
  close(f)
}