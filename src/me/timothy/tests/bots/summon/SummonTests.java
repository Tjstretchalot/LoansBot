package me.timothy.tests.bots.summon;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	CheckSummonTests.class,
	ConfirmSummonTests.class,
	UnpaidSummonTests.class,
	PaidSummonTests.class,
	BadLoanSummonTests.class,
	LoanSummonTests.class,
	RecentPostSummonTests.class
})
public class SummonTests {

}
