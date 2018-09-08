package com.dk.agent;

import java.text.NumberFormat;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TotalDebtsList extends ListActivity {
	public dbAdapter dbi;
	Context ctx;
	Cursor c;
	TextView totals;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.total_debts);
		ctx = this;
		dbi = new dbAdapter(this);
		c = dbi.GetDebts(MainActivity.fromOrgID);
		startManagingCursor(c);
		setListAdapter(new SimpleCursorAdapter(this,
				R.layout.total_debts_list_item, c, new String[] { "Name",
						"LatedDebt", "TotalDebt" }, new int[] {
						R.id.total_debt_client, R.id.total_debt_lated,
						R.id.total_debt }));
		totals = (TextView) findViewById(R.id.total_debt_totals);
		totals.setText("Просрочено "
				+ NumberFormat.getCurrencyInstance().format(
						dbi.GetDebtsTotals(1, MainActivity.fromOrgID))
				+ ", общий долг "
				+ NumberFormat.getCurrencyInstance().format(
						dbi.GetDebtsTotals(0, MainActivity.fromOrgID)));
	}

	@Override
	protected void onPause() {
		dbi.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		dbi.open();
		super.onResume();
	}

}
