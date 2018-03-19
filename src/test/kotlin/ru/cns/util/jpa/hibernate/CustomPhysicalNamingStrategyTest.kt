package ru.cns.util.jpa.hibernate

import org.hibernate.boot.model.naming.Identifier
import org.junit.Assert.*
import org.junit.Test

class CustomPhysicalNamingStrategyTest {

    private val namingStrategy = CustomPhysicalNamingStrategy()

    @Test
    fun testConvertEntityToTableName() {
        val tableName = namingStrategy.toPhysicalTableName(
                Identifier.toIdentifier("SomeSpecialEntity"), null)

        assertEquals("some_specials", tableName!!.text)
    }

    @Test
    fun testConvertNullEntityToNullTableName() {
        val tableName = namingStrategy.toPhysicalTableName(
                null, null)

        assertNull(tableName)
    }

    @Test
    fun testIllegibleEntityNameToTableName() {
        try {
            namingStrategy.toPhysicalTableName(
                    Identifier.toIdentifier("SomeSpecialModel"), null)
            fail("Exception must be thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid entity name 'SomeSpecialModel'", e.message)
        }
    }

    @Test
    fun testConvertSequenceGeneratorToSequenceName() {
        val sequenceName = namingStrategy.toPhysicalSequenceName(
                Identifier.toIdentifier("TransactionEntityIdSequence"), null)

        assertEquals("transactions_id_seq", sequenceName!!.text)
    }

    @Test
    fun testConvertNullSequenceGeneratorToNullSequenceName() {
        val sequenceName = namingStrategy.toPhysicalSequenceName(null, null)

        assertNull(sequenceName)
    }

    @Test
    fun testIllegibleSequenceEntityNameToSequenceName() {
        try {
            namingStrategy.toPhysicalSequenceName(
                    Identifier.toIdentifier("SomeSpecialModelIdSequence"), null)
            fail("Exception must be thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid sequence name 'SomeSpecialModelIdSequence'", e.message)
        }
    }

    @Test
    fun testIllegibleSequenceNameToSequenceName() {
        try {
            namingStrategy.toPhysicalSequenceName(
                    Identifier.toIdentifier("SomeSpecialEntityIdGenerator"), null)
            fail("Exception must be thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid sequence name 'SomeSpecialEntityIdGenerator'", e.message)
        }
    }
}
