package ru.cns.util.jpa.hibernate

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy

class CustomNamingStrategyForTableNames : SpringPhysicalNamingStrategy() {

    override fun toPhysicalTableName(name: Identifier?, context: JdbcEnvironment?): Identifier {
        return super.toPhysicalTableName(name?.let { apply(name) }, context)
    }

    private fun apply(name: Identifier): Identifier {
        if (name.text.endsWith("Entity")) {
            return Identifier.toIdentifier(name.text.replace("Entity", "s"))
        }
        return name
    }
}
