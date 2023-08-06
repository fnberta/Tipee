package ch.berta.fabio.tipee.tip

data class TipCalculation(val tip: Tip, val total: Total, val totalPerPerson: TotalPerPerson)

@JvmInline value class Total(val value: Double)

@JvmInline value class TotalPerPerson(val value: Double)
