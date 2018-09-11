package com.dk.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class dbAdapter {
	public static String FullDbPath;
	public static String FullDbPathWithName;
	public static String dbName;
	public static String dbToSendFolder;
	public static String dbToSendName;
	private Context ctx;
	public SQLiteDatabase db;
	public static final long MadyaroffOrgID = 1;// бали/сфера/тдс
	public static final long STOrgID = 99112;// сибирская трапеза
	public static final long RadugaOrgID = 2;// радуга/акб
	public static final long IrbisOrgID = 98076;// МИТ-Инвест

	dbAdapter(Context ctx) {
		this.ctx = ctx;
		open();
	}

	void open() {// проверим существование БД
		File f = Environment.getExternalStorageDirectory();
		FullDbPath = f.getPath() + ctx.getString(R.string.dbPath);
		FullDbPathWithName = FullDbPath + ctx.getString(R.string.dbName);
		dbName = ctx.getString(R.string.dbName);
		dbToSendFolder = FullDbPath + ctx.getString(R.string.dbToSendFolder);
		dbToSendName = dbToSendFolder + dbName;

		f = new File(FullDbPathWithName);
		if (f.exists()) {
			db = SQLiteDatabase.openDatabase(FullDbPathWithName, null,
					SQLiteDatabase.OPEN_READWRITE);
			db.execSQL("create trigger if not exists doc_docrow before delete on Docs "
					+ "begin delete from DocRows where Doc=old._id; end;");

			SQLiteCursor c = (SQLiteCursor) db.rawQuery(
					"PRAGMA journal_mode = DELETE;", null);

			// c = (SQLiteCursor)
			// db.rawQuery("PRAGMA locking_mode = EXCLUSIVE;",
			// null);

			// c = (SQLiteCursor) db.rawQuery("PRAGMA synchronous;", null);

			// c.moveToFirst();
			// ShowToast.show(ctx, "wsw");
			// db.setLockingEnabled(false);
		} else {
			// ShowToast.show(ctx, "БД не обнаружена!");
			// android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	void close() {
		Cursor c = (SQLiteCursor) db.rawQuery("PRAGMA wal_checkpoint(FULL);",
				null);
		db.close();
	}

	void CompactAndClose() {// ВСТАВИТЬ ПРОГРЕССДИАЛОГ!
		// ShowToast.show(ctx, db.inTransaction() ? "есть транзакции"
		// : "нет транзакций");
		open();
		db.execSQL("VACUUM");
		close();
		db.releaseReference();
		db = null;
	}

	public boolean loadUpdateDB() {
		boolean res;
		res = true;
		Cursor c;
		ContentValues v = new ContentValues();

		SQLiteDatabase tdb = SQLiteDatabase.openDatabase(
				FullDbPath + ctx.getString(R.string.updateDBname), null,
				SQLiteDatabase.OPEN_READWRITE);

		tdb.beginTransaction();

		try {
			c = db.rawQuery("select * from Docs", null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					v.clear();
					v.put("Note", GetString(c, "Note"));
					v.put("Store", GetLong(c, "Store"));
					v.put("_id", GetLong(c, "_id"));
					v.put("Client", GetString(c, "Client"));
					v.put("OrderDate", GetString(c, "OrderDate"));
					v.put("isReturn", GetInt(c, "isReturn"));
					v.put("QuickSale", GetInt(c, "QuickSale"));
					v.put("DocNumber", GetString(c, "DocNumber"));
					v.put("fromOrg", GetLong(c, "fromOrg"));
					v.put("AltDocNumber", GetLong(c, "AltDocNumber"));
					tdb.insertOrThrow("Docs", null, v);
				} while (c.moveToNext());
			}
			c = db.rawQuery("select * from DocRows", null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					v.clear();
					v.put("_id", GetLong(c, "_id"));
					v.put("Goods", GetString(c, "Goods"));
					v.put("Quantity", GetFloat(c, "Quantity"));
					v.put("Articles", GetFloat(c, "Articles"));
					v.put("Price", GetFloat(c, "Price"));
					v.put("NDS", GetFloat(c, "NDS"));
					v.put("Doc", GetLong(c, "Doc"));
					tdb.insertOrThrow("DocRows", null, v);
				} while (c.moveToNext());
			}
			tdb.setTransactionSuccessful();
		} catch (Exception e) {
			res = false;
			ShowToast.show(ctx, "ошибка обновления бд!");
		} finally {
			tdb.endTransaction();
			tdb.execSQL("VACUUM");
		}
		tdb.close();
		db.close();

		return res;

	}

	boolean makeDBToSend(String dbToSend) {
		boolean res = true;
		Cursor c;
		ContentValues v = new ContentValues();
		SQLiteDatabase tdb = SQLiteDatabase.openDatabase(dbToSend, null,
				SQLiteDatabase.OPEN_READWRITE);

		c = tdb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",
				null);
		if (c.moveToFirst()) {
			while (!c.isAfterLast()) {
				if ((!c.getString(0).equals("Docs"))
						&& (!c.getString(0).equals("DocRows"))
						&& (!c.getString(0).equals("TA"))
						&& (!c.getString(0).equals("Client"))
						&& (!c.getString(0).equals("android_metadata"))
						&& (!c.getString(0).equals("sqlite_stat1"))) {
					tdb.execSQL("drop table if exists " + c.getString(0));
				}
				c.moveToNext();
			}
		}
		c.close();

		tdb.delete("Docs", null, null);
		tdb.delete("DocRows", null, null);
		tdb.delete("TA", null, null);
		open();
		tdb.beginTransaction();

		try {
			c = db.rawQuery("select * from Docs", null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					v.clear();
					v.put("Note", GetString(c, "Note"));
					v.put("Store", GetLong(c, "Store"));
					v.put("_id", GetLong(c, "_id"));
					v.put("Client", GetString(c, "Client"));
					v.put("OrderDate", GetString(c, "OrderDate"));
					v.put("isReturn", GetInt(c, "isReturn"));
					v.put("QuickSale", GetInt(c, "QuickSale"));
					v.put("DocNumber", GetString(c, "DocNumber"));
					v.put("fromOrg", GetLong(c, "fromOrg"));
					v.put("AltDocNumber", GetLong(c, "AltDocNumber"));
					tdb.insertOrThrow("Docs", null, v);
				} while (c.moveToNext());
			}
			c = db.rawQuery("select * from DocRows", null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					v.clear();
					v.put("_id", GetLong(c, "_id"));
					v.put("Goods", GetString(c, "Goods"));
					v.put("Quantity", GetFloat(c, "Quantity"));
					v.put("Articles", GetFloat(c, "Articles"));
					v.put("Price", GetFloat(c, "Price"));
					v.put("NDS", GetFloat(c, "NDS"));
					v.put("Doc", GetLong(c, "Doc"));
					v.put("boxes", GetLong(c, "boxes"));
					tdb.insertOrThrow("DocRows", null, v);
				} while (c.moveToNext());
			}
			c = db.rawQuery("select * from TA", null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					v.clear();
					v.put("_id", GetString(c, "_id"));
					v.put("Name", GetString(c, "Name"));
					v.put("Sort", GetString(c, "Sort"));
					v.put("Short", GetString(c, "Short"));
					v.put("Type", GetInt(c, "Type"));
					tdb.insertOrThrow("TA", null, v);
				} while (c.moveToNext());
			}
			tdb.setTransactionSuccessful();
		} catch (Exception e) {
			res = false;
			ShowToast.show(ctx, "ошибка формирования бд!");
		} finally {
			tdb.endTransaction();
			tdb.execSQL("VACUUM");
		}
		c.close();
		tdb.close();
		db.close();
		return res;
	}

	public static boolean copyTo(File f1, File f2) throws IOException {
		if (f2.exists())
			f2.delete();
		if (!f2.exists()) {
			f2.exists();
			String path = f2.getParent();
			File parentDir = new File(path);
			boolean parentExists = true;
			if (!parentDir.exists())
				parentExists = parentDir.mkdirs();
			f2.createNewFile();

			FileInputStream fin = new FileInputStream(f1);
			FileOutputStream fout = new FileOutputStream(f2);
			byte[] b = new byte[512];
			int n = 0;
			while ((n = fin.read(b)) != -1)
				fout.write(b, 0, n);
			fout.close();
			fin.close();
			return true;
		} else
			return false;
	}

	int deleteDoc(long DocID) {
		int res;
		int res2 = 0;
		db.beginTransaction();
		try {
			res = db.delete("Docs", "_id=" + DocID, null);
			db.delete("DocRows", "Doc=" + DocID, null);
			res2 = res;
			db.setTransactionSuccessful();
		} catch (Exception e) {
		} finally {
			db.endTransaction();
		}
		return res2;
	}

	int deleteDocRow(long DocRowID, boolean deleteEmptyDoc) {
		int res = 0;
		long DocID;
		// получим строку заказа по ее айди
		Cursor c = GetDocRowsByDocRowID(DocRowID);
		if (c.getCount() > 0) {
			// это айди документа содержащего строку
			DocID = GetLong(c, "Doc");
			// получим все строки этого документа
			c = GetDocRowsByDocID(DocID);
			// если строк не равно 1, удалять документ нельзя, иначе - в
			// зависимости от входного deleteEmptyDoc
			if (c.getCount() != 1) {
				deleteEmptyDoc = false;
			}
			res = db.delete("DocRows", "_id=" + DocRowID, null);
			if (deleteEmptyDoc) {
				deleteDoc(DocID);
			}
		}
		return res;
	}

	int deleteDocs() {
		int res = 0;
		db.beginTransaction();
		try {// удалим документы, их строки должны подчиститься триггером бд
			res = db.delete("Docs", "", null);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ShowToast.show(ctx, e.toString());
		} finally {
			db.endTransaction();
		}
		return res;
	}

	int deleteDocs(String ClientID, int isReturn) {
		int res = 0;
		db.beginTransaction();
		try {// удалим документы, их строки должны подчиститься триггером бд
			res = db.delete("Docs", "Client='" + ClientID + "' and isReturn="
					+ isReturn, null);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ShowToast.show(ctx, e.toString());
		} finally {
			db.endTransaction();
		}
		return res;
	}

	public long getClientCount() {
		Cursor c = db.rawQuery(
				"SELECT COUNT(distinct Client) AS ClientCount FROM Docs", null);
		c.moveToFirst();
		return GetLong(c, "ClientCount");
	}

	public Cursor GetClients(long fromOrgID) {
		Cursor c = null;
		if (fromOrgID == MadyaroffOrgID) {
			c = db.rawQuery(
					"SELECT c._id, c.Name, SUM(dr.Quantity * g.Weight * .001) AS Weight FROM Client c CROSS JOIN TA ON (c.Store = TA._id) or (c.Master = TA._id) LEFT OUTER JOIN Docs d ON c._id = d.Client LEFT OUTER JOIN DocRows dr ON dr.Doc = d._id LEFT OUTER JOIN Goods g ON dr.Goods = g._id where c.[on]=-1 GROUP BY c._id, c.Name ORDER BY c.Name",
					null);
		}
		if (fromOrgID == STOrgID) {
			c = db.rawQuery(
					"SELECT c._id, c.Name, SUM(dr.Quantity * g.Weight * .001) AS Weight FROM Client c CROSS JOIN TA ON (c.StoreST = TA._id) or (c.MasterST = TA._id) LEFT OUTER JOIN Docs d ON c._id = d.Client LEFT OUTER JOIN DocRows dr ON dr.Doc = d._id LEFT OUTER JOIN Goods g ON dr.Goods = g._id where c.onST=-1 GROUP BY c._id, c.Name ORDER BY c.Name",
					null);
		}
		if ((fromOrgID == RadugaOrgID) || (fromOrgID == IrbisOrgID)) {
			c = db.rawQuery(
					"SELECT c._id, c.Name, SUM(dr.Quantity * g.Weight * .001) AS Weight FROM Client c CROSS JOIN TA ON "
							+ "(c.Store = TA._id) or (c.Master = TA._id) LEFT OUTER JOIN Docs d ON c._id = d.Client LEFT OUTER JOIN "
							+ "DocRows dr ON dr.Doc = d._id LEFT OUTER JOIN Goods g ON dr.Goods = g._id where c.onRain=-1 GROUP BY c._id, "
							+ "c.Name ORDER BY c.Name", null);
		}
		return c;
	}

	// public Cursor GetDocRows(long ClientID) {
	// return db.rawQuery("Select * from DocRows where Client=" + ClientID,
	// null);
	// }

	public Cursor GetDocRows(long DocID, String GoodsID) {
		return db.rawQuery("Select * from DocRows where Doc=" + DocID
				+ " and Goods='" + GoodsID + "'", null);
	}

	// используется для читабельного списка заказов клиента
	public Cursor GetOldOrders(String clientID, long fromOrgID, int docType) {
		return db
				.rawQuery(
						"SELECT dr._id, g._id AS GoodsID, g.Name AS GoodsName, dr.Quantity, dr.Price "
								+ "FROM OldDocRows dr INNER JOIN Goods g ON dr.Goods = g._id INNER JOIN "
								+ "OldDocs d ON dr.Doc = d._id "
								+ "WHERE (d.Client ='" + clientID
								+ "') and d.fromOrg=" + fromOrgID
								+ " and OrderDate is "
								+ (docType == 0 ? "not null" : "null")
								+ " ORDER BY GoodsName", null);
	}

	public Cursor GetDocRowsByDocID(long DocID) {
		Cursor c = db
				.rawQuery("Select * from DocRows where Doc=" + DocID, null);
		c.moveToFirst();
		return c;
	}

	public Cursor GetDocRowsByDocRowID(long DocRowID) {
		Cursor c = db.rawQuery("Select * from DocRows where _id=" + DocRowID,
				null);
		c.moveToFirst();
		return c;
	}

	public Cursor GetDocs(long DocID) {
		Cursor c;
		c = db.rawQuery("Select * from Docs where _id=" + DocID, null);
		c.moveToFirst();
		return c;
	}

	public Cursor GetDebts(String clientID, long fromOrgID) {
		Cursor c;
		c = db.rawQuery(
				"Select *, round(julianday('now')-julianday(DocDate)-late) Late2 from Debts where Client='"
						+ clientID
						+ "' and fromOrg="
						+ fromOrgID
						+ " order by date(DocDate)", null);
		return c;
	}

	public int GetDocsCount(long fromOrgID, String ClientID, int isReturn) {
		Cursor c;
		c = db.rawQuery("Select _id from Docs where Client='" + ClientID
				+ "' and isReturn=" + isReturn + " and fromOrg=" + fromOrgID,
				null);
		return c.getCount();
	}

	public Cursor GetDebts(long fromOrgID) {
		Cursor c;
		c = db.rawQuery(
				"Select c._id, Late, sum(case when julianday('now')-julianday(date(DocDate))-late>=0 then Debt else 0 end) LatedDebt, sum(Debt) TotalDebt, c.Name from Debts d inner join Client c on d.Client=c._id where d.FromOrg="
						+ fromOrgID + " group by c._id order by c.name", null);
		return c;
	}

	public float GetDebtsTotals(int mode, long fromOrgID) {
		// mode 0 - общий долг, 1 - просроченный
		Cursor c;
		c = db.rawQuery(
				"Select sum(case when julianday('now')-julianday(date(DocDate))-late>=0 then Debt else 0 end) LatedDebt, sum(Debt) TotalDebt from Debts d where FromOrg="
						+ fromOrgID, null);
		c.moveToFirst();
		if (mode == 0) {
			return GetFloat(c, "TotalDebt");
		} else {
			return GetFloat(c, "LatedDebt");
		}
	}

	public int GetDocsCount(long fromOrgID, String ClientID) {
		Cursor c;
		c = db.rawQuery("Select _id from Docs where Client='" + ClientID
				+ "' and fromOrg=" + fromOrgID, null);
		return c.getCount();
	}

	public String GetDocDate(String clientID, long fromOrgID) {
		Cursor c;
		c = db.rawQuery("Select OrderDate from OldDocs where Client='"
				+ clientID + "' and fromOrg=" + fromOrgID
				+ " and OrderDate is not null", null);
		if (c.getCount() != 0) {
			c.moveToFirst();
			return GetString(c, "OrderDate");
		} else {
			return "";
		}
	}

	public Cursor GetDocs(long fromOrgID, String ClientID, int isReturn,
			int docNumber) {
		Cursor c;
		switch (docNumber) {
		case -1:
			c = db.rawQuery("Select * from Docs where Client='" + ClientID
					+ "' and isReturn=" + isReturn + " and fromOrg="
					+ fromOrgID + " order by OrderDate", null);
			c.moveToFirst();
			return c;
		default:
			c = db.rawQuery("Select * from Docs where Client='" + ClientID
					+ "' and isReturn=" + isReturn + " and fromOrg="
					+ fromOrgID + " and AltDocNumber=" + docNumber
					+ " order by OrderDate", null);
			c.moveToFirst();
			return c;
		}
	}

	float GetFloat(Cursor c, String columnName) {
		int i = c.getColumnIndex(columnName);
		return c.getFloat(i);
	}

	public Cursor GetGoods() {
		return db.rawQuery("Select * from Goods order by Name", null);
	}

	public Cursor GetGoods(String GoodsID) {
		Cursor c;
		c = db.rawQuery("Select * from Goods where _id='" + GoodsID + "'", null);
		c.moveToFirst();
		return c;
	}

	public Cursor GetGoods(long group, long subgroup, long subgroup2,
			long fromOrgID, int forReturn) {
		Cursor c;
		if (fromOrgID == dbAdapter.MadyaroffOrgID) {
			c = db.rawQuery(
					"Select * from Goods where GoodsGroup="
							+ group
							+ (subgroup == 0 ? "" : " and GoodsSubGroup="
									+ subgroup)
							+ (subgroup2 == 0 ? "" : " and GoodsSubGroup2="
									+ subgroup2)
							+ (forReturn == 0 ? " and [on]=-1" : "")
							+ " order by Name", null);
		} else if (fromOrgID == dbAdapter.STOrgID) {
			c = db.rawQuery(
					"Select * from Goods where GoodsGroup="
							+ group
							+ (subgroup == 0 ? "" : " and GoodsSubGroup="
									+ subgroup)
							+ (subgroup2 == 0 ? "" : " and GoodsSubGroup2="
									+ subgroup2)
							+ (forReturn == 0 ? " and [onST]=-1" : "")
							+ " order by Name", null);
		} else if ((fromOrgID == dbAdapter.RadugaOrgID)
				|| (fromOrgID == dbAdapter.IrbisOrgID)) {
			c = db.rawQuery(
					"Select * from Goods where GoodsGroup="
							+ group
							+ (subgroup == 0 ? "" : " and GoodsSubGroup="
									+ subgroup)
							+ (subgroup2 == 0 ? "" : " and GoodsSubGroup2="
									+ subgroup2)
							+ (forReturn == 0 ? " and [onRain]=-1" : "")
							+ " order by QuantityRain<=0, Name", null);
		} else
			c = db.rawQuery("Select * from Goods where 0=1", null);
		return c;
	}

	public Cursor GetGroups(long fromOrgID) {
		return db.rawQuery("Select * from Groups where fromOrg=0 or fromOrg="
				+ fromOrgID + " order by Sort, Name", null);
	}

	int GetInt(Cursor c, String columnName) {
		int i = c.getColumnIndex(columnName);
		return c.getInt(i);
	}

	static long GetLong(Cursor c, String columnName) {
		int i = c.getColumnIndex(columnName);
		return c.getLong(i);
	}

	// используется для читабельного списка заказов клиента
	public Cursor GetOrders(long fromOrgID, String ClientID, int isReturn,
			int docNumber) {
		return db
				.rawQuery(
						"SELECT dr._id, g._id AS GoodsID, g.Name AS GoodsName, dr.Quantity, dr.Price, dr.boxes "
								+ "FROM DocRows dr INNER JOIN Goods g ON dr.Goods = g._id INNER JOIN "
								+ "Docs d ON dr.Doc = d._id "
								+ "WHERE (d.Client ='"
								+ ClientID
								+ "') AND (d.isReturn ="
								+ isReturn
								+ ") and d.fromOrg="
								+ fromOrgID
								+ (docNumber == -1 ? "" : " and AltDocNumber="
										+ docNumber) + " ORDER BY GoodsName",
						null);
	}

	Cursor getOrgs(long orgID) {
		Cursor c = db.rawQuery("SELECT * from Orgs where _id=" + orgID, null);
		c.moveToFirst();
		return c;
	}

	String GetString(Cursor c, String columnName) {
		int i = c.getColumnIndex(columnName);
		return c.getString(i);
	}

	public Cursor GetSubgroups() {
		return db.rawQuery("Select * from SubGroups order by Sort, Name", null);
	}

	public Cursor GetSubgroups2() {
		return db
				.rawQuery("Select * from SubGroups2 order by Sort, Name", null);
	}

	public Cursor GetSubgroups(long parentGroup) {
		return db.rawQuery("Select * from SubGroups where ParentGroup="
				+ parentGroup + " or ParentGroup=0 order by Sort, Name", null);
	}

	public Cursor GetSubgroups2(long parentGroup) {
		return db.rawQuery("Select * from SubGroups2 where ParentGroup="
				+ parentGroup + " or ParentGroup=0 order by Sort, Name", null);
	}

	public Cursor GetOrdersStatistics() {
		return db
				.rawQuery(
						"SELECT gr._id, gr.Name, SUM(round(g.Weight * dr.Quantity * .001, 3)) AS TotalWeight, "
								+ "SUM(round(dr.Price * dr.Quantity, 2)) AS TotalTotal "
								+ "FROM DocRows dr inner join Goods g on dr.Goods=g._id inner join Groups gr on "
								+ "g.GoodsGroup=gr._id group by gr._id, gr.Name order by gr.Sort",
						null);
	}

	public float getTotalTotal() {
		Cursor c = db.rawQuery(
				"SELECT SUM(round(dr.Price * dr.Quantity, 2)) AS TotalWeight"
						+ " FROM Docs d INNER JOIN"
						+ " DocRows dr ON d._id = dr.Doc", null);
		c.moveToFirst();
		return GetFloat(c, "TotalWeight");
	}

	public float getTotalTotal(String ClientID, int isReturn) {
		Cursor c = db.rawQuery(
				"SELECT SUM(round(dr.Price * dr.Quantity, 2)) AS TotalWeight"
						+ " FROM Docs d INNER JOIN"
						+ " DocRows dr ON d._id = dr.Doc" + " WHERE Client='"
						+ ClientID + "' AND isReturn=" + isReturn, null);
		c.moveToFirst();
		return GetFloat(c, "TotalWeight");
	}

	public float getWeightTotal() {
		Cursor c = db.rawQuery(
				"SELECT SUM(round(g.Weight * dr.Quantity * .001, 3)) AS TotalWeight"
						+ " FROM Docs d INNER JOIN"
						+ " DocRows dr ON d._id = dr.Doc INNER JOIN"
						+ " Goods g ON dr.Goods = g._id", null);
		c.moveToFirst();
		return GetFloat(c, "TotalWeight");
	}

	public float getWeightTotal(String ClientID, int isReturn) {
		Cursor c = db.rawQuery(
				"SELECT SUM(round(g.Weight * dr.Quantity * .001, 3)) AS TotalWeight"
						+ " FROM Docs d INNER JOIN"
						+ " DocRows dr ON d._id = dr.Doc INNER JOIN"
						+ " Goods g ON dr.Goods = g._id" + " WHERE Client='"
						+ ClientID + "' AND isReturn=" + isReturn, null);
		c.moveToFirst();
		return GetFloat(c, "TotalWeight");
	}

	public String getTAName() {
		Cursor c = db.rawQuery("SELECT t.Name FROM TA t", null);
		c.moveToFirst();
		return GetString(c, "Name").substring(0, 7);
	}

	public long insertDoc(long fromOrgID, String ClientID, String Note,
			int isReturn, int docNumber) {
		ContentValues v = new ContentValues();
		v.put("Client", ClientID);
		v.put("fromOrg", fromOrgID);
		v.put("Note", Note);
		v.put("isReturn", isReturn);
		v.put("OrderDate", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
				.format(java.util.Calendar.getInstance().getTime()));
		v.put("AltDocNumber", docNumber);
		// v.put("_id", "NULL");
		return db.insertOrThrow("Docs", null, v);
	}

	public long insertDocRow(long DocID, String GoodsID, float q, float Price,
			int places) {
		ContentValues v = new ContentValues();
		v.put("Goods", GoodsID);
		v.put("Quantity", q);
		v.put("Price", Price);
		v.put("Doc", DocID);
		v.put("boxes", places);
		return db.insertOrThrow("DocRows", null, v);

	}

	public void insertOrder(long fromOrgID, String ClientID, String GoodsID,
			float q, String Note, int isReturn, int DocNumber, int places) {
		long DocID;
		Cursor Doc;
		Doc = GetDocs(fromOrgID, ClientID, isReturn, DocNumber);
		if (Doc.getCount() == 0) {
			DocID = insertDoc(fromOrgID, ClientID, Note, isReturn, DocNumber);
		} else {
			DocID = GetLong(Doc, "_id");
		}
		if (DocID > 0) {
			if (((Cursor) GetDocRows(DocID, GoodsID)).getCount() == 0) {
				// вытащим цену из таблицы товара и передадим в инсерт
				if (insertDocRow(DocID, GoodsID, q,
						GetFloat(GetGoods(GoodsID), "Price"), places) == -1) {
					ShowToast
							.show(ctx,
									"Что-то пошло не так! У Вас точно последняя версия программы?");
				}
				;
			} else
				ShowToast
						.show(ctx,
								"Этот товар уже заказан! Перейдите на вкладку заказанного товара и отредактируйте количество.");

		} else {
			ShowToast.show(ctx, "Ошибка создания документа!");
		}
	}

	int setNote(long fromOrg, String ClientID, int isReturn, String note,
			int docNumber) {
		if (docNumber >= 0) {
			ContentValues v = new ContentValues();
			v.put("Note", note);
			return db.update("Docs", v, "Client='" + ClientID
					+ "' and isReturn=" + isReturn + " and fromOrg=" + fromOrg
					+ " and AltDocNumber=" + docNumber, null);
		} else
			return 0;
	}

	int updateOrderRow(long DocRowID, float q, int p) {
		ContentValues v = new ContentValues();
		v.put("Quantity", q);
		v.put("boxes", p);
		return db.update("DocRows", v, "_id=" + DocRowID, null);
	}
}
