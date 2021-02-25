package org.hq;

import org.hq.utils.DataConnect;

import java.io.*;
import java.sql.SQLException;


public class ApplicationMain {
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("正在操作，请等待...\n");

        DataConnect.init();

        //加一个true相当于flush
        //控制台到文件
        System.setOut(new PrintStream(new BufferedOutputStream(
                new FileOutputStream(DataConnect.FILE_PATH)), true));

        try {
            System.out.println(Table.getDiffTables());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //文件回控制台
        System.setOut(new PrintStream(new BufferedOutputStream(
                new FileOutputStream(FileDescriptor.out)), true));

        System.out.println("\n操作完毕");
        System.exit(0);
    }
}
