module ktorm.support.kingbase {
    requires ktorm.core;
    exports org.ktorm.support.kingbase;
    provides org.ktorm.database.SqlDialect with org.ktorm.support.kingbase.KingbaseDialect;
}
