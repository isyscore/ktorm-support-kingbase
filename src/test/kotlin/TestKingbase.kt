import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.entity.Entity
import org.ktorm.entity.map
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.take
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import org.ktorm.support.kingbase.KingbaseDialect
import kotlin.test.Test

class TestKingbase {

    @Test
    fun test() {
        val db = Database.connect(url = "jdbc:kingbase8://localhost:54321/root?currentSchema=yugiohapi2", driver = "com.kingbase8.Driver", user = "root", password = "rootroot", dialect = KingbaseDialect(), logger = ConsoleLogger(LogLevel.DEBUG))
        val list = db.cardNames.take(5).map { it }
        println(list)

        val list2 = db.from(CardNames).select().limit(0, 5).map { CardNames.createEntity(it) }
        println(list2)
    }

}

interface CardName : Entity<CardName> {
    companion object : Entity.Factory<CardName>()

    var id: Long
    var kanji: String
    var kk: String
    var donetime: Long
}

object CardNames : Table<CardName>("card_name_texts") {
    var id = long("id").primaryKey().bindTo { it.id }
    var kanji = varchar("kanji").bindTo { it.kanji }
    var kk = varchar("kk").bindTo { it.kk }
    var donetime = long("donetime").bindTo { it.donetime }
}

val Database.cardNames get() = this.sequenceOf(CardNames)