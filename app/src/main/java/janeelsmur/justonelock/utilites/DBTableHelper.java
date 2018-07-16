package janeelsmur.justonelock.utilites;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;


// TODO При добавлении новых таблиц/полей и пр. нужно будет прописать гибкий алгоритм обновления БД пользователя.

/**
 *
 * Класс-помощник для создания базы данных и изменения значений в ней.
 * Изменения констант недопустимы.
 *
 * При добавлении новых таблиц/полей и пр. нужно будет прописать
 * гибкий алгоритм обновления БД пользователя.
 *
 */

public class DBTableHelper {

    private static final String LOG_TAG = "DBTableHelper";

    public static final String SYSTEM_FOLDER_NAME_PREFIX = "folder";
    public static final String KEY_ID = "_id";

    //Таблица с контрольной строкой
    public static final String CHECK_SUM_TEXT = "There is a cupcake and some ice cream";
    public static final String CHECK_SUM_FIELD = "Sum";
    public static final String CHECK_SUM_TABLE_NAME = "SystemCheckSum";

    //Таблица с информацией о всех папках
    public static final String FOLDER_INDEXER_TABLE = "SystemFolderIndexer";
    public static final String FOLDER_INDEXER_NAMES_FIELD = "header";
    public static final String FOLDER_ICON_CODE = "icon";
    public static final String FOLDER_IS_FAVORITE = "isFavorite";
    public static final String FOLDER_IS_REMOVED = "isRemoved";

    //Таблица с паролями (папка)
    public static final String PASS_IS_FAVORITE = "isFavorite";
    public static final String PASS_IMAGE_CODE = "imageCode";
    public static final String PASS_SERVICE = "service";
    public static final String PASS_LOGIN = "login";
    public static final String PASS_PASSWORD = "password";
    public static final String PASS_DESCRIPTION = "description";
    public static final String PASS_CREATING_DATETIME = "creatingDate";
    public static final String PASS_CHANGING_DATETIME = "changingDate";
    public static final String PASS_IS_REMOVED = "isRemoved";

    //Таблица с паролями, которые не в папке
    public static final String TABLE_PASSWORDS_WITHOUT_FOLDER = "SystemPasswordsWithoutTable";

    //Таблица с заметками
    public static final String NOTES_TABLE = "SystemNotesTable";
    public static final String NOTE_TEXT = "text";
    public static final String NOTE_CREATING_DATETIME = "creatingDate";
    public static final String NOTE_CHANGING_DATETIME = "changingDate";
    public static final String NOTE_IS_REMOVED = "isRemoved";

    /** Создание таблицы папки
     *
     * @param folderName - название папки
     * @param folderProperties - Свойства папки.
     *                         Ключи:
     *                         FOLDER_INDEXER_NAMES_FIELD - Название папки
     *                         FOLDER_ICON_CODE - id ресурса иконки для папки
     *                         FOLDER_IS_FAVORITE - является ли папки избарнной
     *                         FOLDER_IS_REMOVED - является ли папка удаленной
     */
    public static void createFolderTable(SQLiteDatabase database, String folderName, ContentValues folderProperties) {
        database.insert(FOLDER_INDEXER_TABLE, null, folderProperties);
        database.execSQL ("CREATE TABLE " +
                        folderName + "(" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                        PASS_IS_FAVORITE + " NUMERIC" + "," +
                        PASS_IMAGE_CODE + " TEXT" + "," +
                        PASS_SERVICE + " TEXT" + "," +
                        PASS_LOGIN + " BLOB" + "," +
                        PASS_PASSWORD + " BLOB" + "," +
                        PASS_DESCRIPTION + " TEXT" + "," +
                        PASS_CREATING_DATETIME + " TIMESTAMP" + "," +
                        PASS_CHANGING_DATETIME + " TIMESTAMP" + "," +
                        PASS_IS_REMOVED + " NUMERIC" + ");"
        );
    }

    /** Создание пароля в папке-таблице
     *
     * @param systemFolderName - Системное название папки, в котором будет храниться пароль.
     *                         Оно является названием таблицы в базе данных и невидимо для пользователя.
     * @param passwordProperties - Свойства пароля.
     *                         Ключи:
     *                         PASS_IS_FAVORITE - Имя пароля
     *                         PASS_IS_REMOVED - Удален ли пароль
     *                         PASS_CREATING_DATETIME - Дата создания
     *                         PASS_CHANGING_DATETIME - Дата изменения
     *                         PASS_IMAGE_CODE - Id ресурса иконки
     *                         PASS_SERVICE - Название сервиса
     *                         PASS_LOGIN - Логин от сервиса
     *                         PASS_PASSWORD - Пароль от сервиса
     *                         PASS_DESCRIPTION - Описание
     */
    public static void createPasswordInFolder(SQLiteDatabase database, String systemFolderName, ContentValues passwordProperties) {
        database.insert(systemFolderName, null, passwordProperties);
    }

    /** Создание пароля вне папки */
    public static void createPassword(SQLiteDatabase database, ContentValues passwordProperties) {
        database.insert(TABLE_PASSWORDS_WITHOUT_FOLDER, null, passwordProperties);
    }

    /** Создание заметки
     *
     * @param noteProperties - Свойства заметки.
     *                       Ключи:
     *                       NOTE_TEXT - Текст заметки. Первая строка - это заголовок
     *                       NOTE_CREATING_DATETIME - Дата создания
     *                       NOTE_CHANGING_DATETIME - Дата изменения
     *                       NOTE_IS_REMOVED - Удалена ли заметка
     */
    public static void createNote(SQLiteDatabase database, ContentValues noteProperties) {
        database.insert(NOTES_TABLE, null, noteProperties);
    }

    /** Пометить заметку как удаленную или восстановить
     *
     * @param noteId - id заметки
     * @param isDeleted - есть возможность как удалить, так и восстановить
     *                  0 - Не удален
     *                  1 - Удален
     */
    public static void markNoteDeleted(SQLiteDatabase database, int noteId, int isDeleted){
        database.execSQL("UPDATE " + NOTES_TABLE + " SET isRemoved = " + isDeleted + " WHERE _id = " + noteId);
    }

    /** Пометить пароль как удаленный или восстановить
     *
     * @param systemFolderName - системное название папки
     * @param passwordId - id пароля
     * @param isDeleted - есть возможность как удалить, так и восстановить
     *                  0 - Не удален
     *                  1 - Удален
     */
    public static void markPasswordDeleted(SQLiteDatabase database, String systemFolderName, int passwordId, int isDeleted) {
        database.execSQL("UPDATE " + systemFolderName + " SET isRemoved = " + isDeleted + " WHERE _id = " + passwordId);
    }

    /** Пометить папку как удаленную или восстановить
     *
     * @param folderId - id папки
     * @param isDeleted - есть возможность как удалить, так и восстановить
     *                  0 - Не удален
     *                  1 - Удален
     */
    public static void markFolderDeleted(SQLiteDatabase database, int folderId, int isDeleted){
        database.execSQL("UPDATE " + FOLDER_INDEXER_TABLE + " SET isRemoved = " + isDeleted + " WHERE _id = " + folderId);
    }

    /** Создание таблицы с контрольной строкой.
     * В этой таблице хранится проверочная строка, которая шифруется под ключ пользователя.
     * При в ходе хранилище, она расшифровывается, если ключ введен правильно. Тогда осущиствляется вход в таблицу.
     * Иначе выводится сообщение о том, что ключ не верный */
    public static void createCheckSumTable(SQLiteDatabase database, ContentValues encryptedString) {
        database.execSQL("CREATE TABLE " +
                CHECK_SUM_TABLE_NAME + " (" +
                CHECK_SUM_FIELD + " BLOB" + ");"
        );
        database.insert(CHECK_SUM_TABLE_NAME, null , encryptedString);
    }

    /** Создание таблицы индексации папок
     *      Столбцы:
     *      FOLDER_INDEXER_NAMES_FIELD - название папки
     *      FOLDER_ICON_CODE - id ресурса иконки папки
     *      FOLDER_IS_FAVORITE - является ли папка избранной
     *      FOLDER_IS_REMOVED - является ли папка удаленной
     */
    public static void createFolderIndexerTable(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " +
                FOLDER_INDEXER_TABLE + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                FOLDER_INDEXER_NAMES_FIELD + " TEXT" + "," +
                FOLDER_ICON_CODE + " SMALLINT" + "," +
                FOLDER_IS_FAVORITE + " NUMERIC" + "," +
                FOLDER_IS_REMOVED + " NUMERIC" + ");"
        );
    }

    /** Создание таблицы с паролями, которые не привязаны к папкам */
    public static void createPasswordsWithoutFolderTable(SQLiteDatabase database){
        database.execSQL("CREATE TABLE " +
                TABLE_PASSWORDS_WITHOUT_FOLDER + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " +
                PASS_IS_FAVORITE + " NUMERIC" + ", " +
                PASS_IMAGE_CODE + " TEXT" + ", " +
                PASS_SERVICE + " BLOB" + ", " +
                PASS_LOGIN + " BLOB" + ", " +
                PASS_PASSWORD + " BLOB" + ", " +
                PASS_DESCRIPTION + " TEXT" + ", " +
                PASS_CREATING_DATETIME + " TIMESTAMP" + ", " +
                PASS_CHANGING_DATETIME + " TIMESTAMP" + ", " +
                PASS_IS_REMOVED + " NUMERIC" + ");"
        );
    }

    /** Создание таблицы с заметками */
    public static void createNotesTable(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " +
                NOTES_TABLE + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " +
                NOTE_TEXT + " BLOB" + ", " +
                NOTE_IS_REMOVED + " NUMERIC" + ", " +
                NOTE_CREATING_DATETIME + " TIMESTAMP" + ", " +
                NOTE_CHANGING_DATETIME + " TIMESTAMP" + ");"
        );
    }

}
