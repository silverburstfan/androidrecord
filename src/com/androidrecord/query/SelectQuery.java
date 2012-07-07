package com.androidrecord.query;

import android.database.Cursor;
import com.androidrecord.ActiveRecordBase;
import com.androidrecord.db.Database;

public class SelectQuery<T extends ActiveRecordBase> extends MultiValuedQuery<T> {
    private String whereClause;
    private String [] whereClauseArgs;

    public SelectQuery(QueryContext<T> queryContext, Database database, ActiveRecordBase lookupInstance, String whereClause, String... whereClauseArgs) {
        super(queryContext, (T) lookupInstance, database);
        this.whereClause = whereClause;
        this.whereClauseArgs = whereClauseArgs;
    }

    @Override
    protected Cursor select() {
        return database.select(activeRecordInstance.tableName(), whereClause, whereClauseArgs);
    }
}
