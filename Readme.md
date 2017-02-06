Japan Times Feature Vector Generator
==

# What is this?

Generate feature vectors of 
- only Japan times Article
- Train: Japan times article, Test: Japan times article
- Train: Japan times article, Test: Japan times title

# Generating Methods

- TFIDF
- TFIDF vector ++ word2vec

(`++` concats two vectors together)

# Category of articles

The following is a part of `Main.scala`. This shows what category I used.

1. Economy, Politic, Tech, Figure Skating and Sumo (Each page limit 30)
2. ~~Economy, Politic, Crime and Legal, Editorial, Corporate~~ (Not used because this is too big to calc for my PC. this caused `OutOfMemoryError`)
3. Economy, Politic, Tech, Figure Skating and Sumo (Each page limit 46)

```scala
// Download Information
val `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo` = DownloadInfo(
  storedPath = "./data/japan-times-multi-articles.json",
  pageLimit = 30,
  pageToUrls = Seq(
    TimesGetterJsoup.economyPage _,
    TimesGetterJsoup.politicPage _,
    TimesGetterJsoup.techPage _,
    TimesGetterJsoup.figurePage _,
    TimesGetterJsoup.sumoPage _
  )
)

// Download Information
val `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate` = DownloadInfo(
  storedPath = "./data/jptimes-epcec-multi-each184-articles.json",
  pageLimit = 184,
  pageToUrls = Seq(
    TimesGetterJsoup.economyPage _,
    TimesGetterJsoup.politicPage _,
    TimesGetterJsoup.crimeLegalPage _,
    TimesGetterJsoup.editorialsPage _,
    TimesGetterJsoup.corporatePage _
  )
)

// Download Information
val `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo` = DownloadInfo(
  storedPath = "./data/jptimes-eptfs-multi-each46-articles.json",
  pageLimit = 46,
  pageToUrls = Seq(
    TimesGetterJsoup.economyPage _,
    TimesGetterJsoup.politicPage _,
    TimesGetterJsoup.techPage _,
    TimesGetterJsoup.figurePage _,
    TimesGetterJsoup.sumoPage _
  )
)
```


# How to run?

## Way1 - IntelliJ IDEA

1. Download this repo
2. Open this repo as a `sbt` project in IntelliJ IDEA
3. Open `<this-repo>/build.sbt` in IntelliJ IDEA and click the `Refresh project`
4. Run `<this-repo>/src/main/scala/io.github.nwtgck.japan_times_feature_vector/Main.scala`

## Way2 - Command Line (sbt)

*CAUTION: this way doen't work, but it can work usually*




```sh
$ cd <this-repo>
$ sbt run
```

That's all. (Dependencies(libraries) and compiling is finished by only `sbt run`)


(SBT is Simple Build Tool)


But this doesn't work. It has runtime error. I think Spark has problem.


# How to change the code

`Main.scala` has the code such as 

```scala
if(false){
  ...
}

if(false){
   ...
}
```

If you want to execute a process, you should change `false` to `true`. It's easy to toggle the process.

# CAUTION

This source code has many repeated sources. In other word, this ignore DRY(Don't Repeat Yourself). 

So, don't think why the creator repeat the similar code.
