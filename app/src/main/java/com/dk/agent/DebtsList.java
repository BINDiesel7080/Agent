package com.dk.agent;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class DebtsList extends ListActivity {
	public dbAdapter dbi;
	Context ctx;
	Cursor c;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debts);
		ctx = this;
		dbi = new dbAdapter(this);
		c = dbi.GetDebts(ClientActivity.ClientID, ClientActivity.fromOrgID);
		startManagingCursor(c);

		setListAdapter(new SimpleCursorAdapter(this, R.layout.debts_list_item,
				c, new String[] { "DocDate", "TNNumber", "DocTotal", "Debt",
						"Late2" }, new int[] { R.id.tvDocDate,
						R.id.tvDocNumber, R.id.tvDocTotal, R.id.tvDebt,
						R.id.tvLate }));
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
