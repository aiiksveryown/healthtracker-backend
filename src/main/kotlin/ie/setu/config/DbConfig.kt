package ie.setu.config

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database

class DbConfig{
    //NOTE: you need the ?sslmode=require otherwise you get an error complaining about the ssl certificate
    fun getDbConnection() :Database{

        val logger = KotlinLogging.logger {}
        logger.info{"Starting DB Connection..."}

//        val PGUSER = System.getenv("PGUSER")
//        val PGPASSWORD = System.getenv("PGPASSWORD")
//        val PGHOST = System.getenv("PGHOST")
//        val PGPORT = System.getenv("PGPORT")
//        val PGDATABASE = System.getenv("PGDATABASE")

        val PGUSER = "maayrkmw"
        val PGPASSWORD = "7gGbJs4SLYItUKzmdX7tMwOXVRv0tmKU"
        val PGHOST = "lucky.db.elephantsql.com"
        val PGPORT = "5432"
        val PGDATABASE = "maayrkmw"

        //url format should be jdbc:postgresql://host:port/database
        val url = "jdbc:postgresql://$PGHOST:$PGPORT/$PGDATABASE"

        val dbConfig = Database.connect(url,
            driver="org.postgresql.Driver",
            user = PGUSER,
            password = PGPASSWORD
        )

        logger.info{"db url - connection: " + dbConfig.url}

        return dbConfig
    }
}