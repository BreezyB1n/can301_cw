package com.example.can301_cw.infra.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DB_NAME = "app_local.db"
private const val DB_VERSION = 1

/**
 * 仅封装 SQLite 连接创建与表初始化，提供 DbProvider 获取单例。
 * 后续各表的 CRUD 会独立封装，不在此处实现。
 */
class AppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // TODO：创建各表

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO：更新
    }
}

/**
 * 全局获取 DB Helper 的入口。
 */
object DbProvider {
    @Volatile
    private var instance: AppDatabaseHelper? = null

    fun get(context: Context): AppDatabaseHelper =
        instance ?: synchronized(this) {
            instance ?: AppDatabaseHelper(context).also { instance = it }
        }
}
