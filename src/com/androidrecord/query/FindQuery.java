package com.androidrecord.query;

import android.database.Cursor;
import com.androidrecord.ActiveRecordBase;
import com.androidrecord.db.Database;

public class FindQuery<T extends ActiveRecordBase> extends Query<T> {
    private String whereClause;
    private String[] whereClauseArgs;

    public FindQuery(QueryContext<T> context, Database database, T activeRecordInstance, String whereClause, String... whereClauseArgs) {
        super(context, activeRecordInstance, database);
        this.whereClause = whereClause;
        this.whereClauseArgs = whereClauseArgs;
    }

    public T run() {
        Cursor queryResult = queryResult();
        T record = recordFrom(queryResult);
        queryResult.close();
        return record;
    }

    private Cursor queryResult() {
        Cursor result = database.select(activeRecordInstance.tableName(), whereClause);
        if (result.getCount() > 1)
            throw new RuntimeException("Got " + result.getCount() + " results for " + activeRecordInstance.getClass().getSimpleName() + " [" + whereClause + "]");
        result.moveToFirst();
        return result;
    }

}