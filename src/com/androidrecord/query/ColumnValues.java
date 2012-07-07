package com.androidrecord.query;

import android.content.ContentValues;
import com.androidrecord.ActiveCollection;
import com.androidrecord.ActiveRecordBase;
import com.androidrecord.DateTime;
import com.androidrecord.associations.BelongsTo;
import com.androidrecord.associations.HasOne;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ColumnValues {
    private ContentValues contentValues = new ContentValues();
    private ActiveRecordBase activeRecordBase;

    public ColumnValues(ActiveRecordBase activeRecordBase) {
        this.activeRecordBase = activeRecordBase;
    }

    public ContentValues generate() {
        for (Field field : fields()) {
            try {
            	field.setAccessible(true);
                handleAssociation(field);
                handleLong(field);
                handleString(field);
                handleDateTime(field);
                handleBooleanField(field);
                handleIntegerField(field);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return contentValues;
    }
    
    private ArrayList<Field> fields() {
    	return fields(this.activeRecordBase.getClass());
    }
    
    private ArrayList<Field> fields(Class<?> activeRecordClass) {
        Field[] allFields = activeRecordClass.getDeclaredFields();
        ArrayList<Field> fields = new ArrayList<Field>();
        for (Field field : allFields) {
        	fields.add(field);
        }
        if(!activeRecordClass.equals(ActiveRecordBase.class)) {
        	fields.addAll(fields(ActiveRecordBase.class));
        }
        return fields;
    }

    private void handleIntegerField(Field field) throws IllegalAccessException {
        if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
            Integer fieldValue = (Integer) field.get(activeRecordBase);
            contentValues.put(field.getName(), fieldValue);
        }
    }

    private void handleBooleanField(Field field) throws IllegalAccessException {
        if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            Boolean booleanField = (Boolean) field.get(activeRecordBase);
            if (booleanField) contentValues.put(field.getName(), 1);
            else contentValues.put(field.getName(), 0);
        }
    }

    private void handleAssociation(Field field) throws IllegalAccessException {
        if (isAssociation(field)) {
            if (isOwned(field)) handleOwned(field);
            else handleOwning(field);
        }
    }

    private boolean isOwned(Field field) {
        return field.isAnnotationPresent(BelongsTo.class);
    }

    private void handleOwned(Field field) throws IllegalAccessException {
        ActiveRecordBase owner = (ActiveRecordBase) field.get(activeRecordBase);
        String ownerColumnName = field.getName() + "_id";

        if (owner == null) {
            contentValues.put(ownerColumnName, (Long) null);
            return;
        }
        if (owner.isTransient()) throw new RuntimeException("Entity is owned by an entity that has not been saved.");

        contentValues.put(ownerColumnName, owner.id);
    }

    private void handleOwning(Field field) throws IllegalAccessException {
        if (field.isAnnotationPresent(HasOne.class)) {
            ActiveRecordBase owned = (ActiveRecordBase) field.get(this.activeRecordBase);
            if (owned == null) return;

            setOwnerOn(owned);
        }
    }

    private void setOwnerOn(ActiveRecordBase ownedEntity) throws IllegalAccessException {
        for (Field ownedEntityField : fields(ownedEntity.getClass())) {
            if (ownsThis(ownedEntityField)) {
                ownedEntityField.set(ownedEntity, activeRecordBase);
            }
        }
    }

    private boolean ownsThis(Field ownedEntityField) {
        return isOwned(ownedEntityField) && ownedEntityField.getType().equals(activeRecordBase.getClass());
    }

    private void handleDateTime(Field field) throws IllegalAccessException {
        if (field.getType().equals(DateTime.class)) {
            DateTime dateTime = (DateTime) field.get(activeRecordBase);
            if (dateTime == null) return;
            contentValues.put(field.getName(), dateTime.toString());
        }
        else if (field.getType().equals(java.util.Date.class)) {
        	java.util.Date date = (java.util.Date) field.get(activeRecordBase);
        	if (date == null) return;
        	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	contentValues.put(field.getName(), format.format(date));
        }
    }

    private boolean isAssociation(Field field) {
        if (ActiveRecordBase.class.isAssignableFrom(field.getType())) return true;
        return field.getType().equals(ActiveCollection.class);
    }

    private void handleString(Field field) throws IllegalAccessException {
        if (field.getType().equals(String.class)) {
            String value = (String) field.get(this.activeRecordBase);
            contentValues.put(field.getName(), value);
        }
    }

    private void handleLong(Field field) throws IllegalAccessException {
        if (field.getType().equals(Long.class)) {
            Long value = (Long) field.get(this.activeRecordBase);
            contentValues.put(field.getName(), value);
        }
    }

}
