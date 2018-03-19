package ru.cns.util.jpa.hibernate

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy

class CustomPhysicalNamingStrategy : SpringPhysicalNamingStrategy() {

    private companion object {
        private val TABLE_REGEX = Regex("""^(.*)Entity$""")
        private val SEQUENCE_REGEX = Regex("""^(.*Entity)(.*)Sequence$""")
    }

    override fun toPhysicalTableName(name: Identifier?, context: JdbcEnvironment?): Identifier? {
        return super.toPhysicalTableName(name?.let { applyTable(it) }, context)
    }

    private fun applyTable(tableIdentifier: Identifier): Identifier {
        return Identifier.toIdentifier(entityNameToTableName(tableIdentifier.text))
    }

    private fun entityNameToTableName(entityName: String): String {
        return TABLE_REGEX.find(entityName)?.let { it.groupValues[1] + "s" }
                ?: throw IllegalArgumentException("Invalid entity name '$entityName'")
    }

    override fun toPhysicalSequenceName(name: Identifier?, jdbcEnvironment: JdbcEnvironment?)
            : Identifier? {
        return super.toPhysicalSequenceName(name?.let { applySequence(it) }, jdbcEnvironment)
    }

    private fun applySequence(sequenceIdentifier: Identifier): Identifier {
        return SEQUENCE_REGEX.find(sequenceIdentifier.text)
                ?.let {
                    Identifier.toIdentifier(entityNameToTableName(it.groupValues[1])
                            + it.groupValues[2] + "Seq")
                }
                ?: throw IllegalArgumentException("Invalid sequence name '${sequenceIdentifier.text}'")
    }
}
