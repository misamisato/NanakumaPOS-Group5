//-*- java -*-
/**********************************************************************************************************************
 *
 *  福岡大学工学部電子情報工学科プロジェクト型ソフトウェア開発演習教材
 *
 *  Copyright (C) 2015-2024 プロジェクト型ソフトウェア開発演習実施チーム
 *
 *  外部データベースにインターフェースするクラス。Singleton パターンで実装している。
 *
 *********************************************************************************************************************/

package jp.ac.fukuoka_u.tl.NanakumaPOS;

import java.sql.*;
import java.time.LocalDate;

import jp.ac.fukuoka_u.tl.NanakumaPOS.Member.Gender;

import static java.lang.System.out;


/*
 *  データベースサーバインターフェースクラス
 */

public class DBServerIF {
    /*
     *  データベースサーバインターフェースクラスの唯一のインスタンス（Singleton パターン）
     */

    private static DBServerIF theDBServerIF = null;


    /*
     *  内部データ
     */
    // データベース接続
    private static Connection conn = null;

    // データベース接続先 URL
    private String url = "DB_URL";

    // データベースユーザ名
    private String user = "DB_USER";
    //private String user = "b3pbl";

    // データベースパスワード
    private String password = "DB_PASSWORD";


    /*
     *  データベースインターフェースに関する障害を通知する例外クラス
     */

    public static class DBServerIFException extends Exception {
        /*
         *  コンストラクタ
         */
        public DBServerIFException(String _message) {
            super(_message);
        }
    }


    /*
     *  コンストラクタ
     */

    private DBServerIF () {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /*
     *  データベースインターフェースの唯一のインスタンスを返す。（Singleton パターン）
     */

    public static DBServerIF getInstance() {
        if (theDBServerIF == null) {
            theDBServerIF = new DBServerIF();
        }
        return theDBServerIF;
    }


    /*
     *  商品コード articleCode の商品を検索する。
     */

    public Article findArticle(String articleCode) throws DBServerIFException {
        Article article = null;

        try {
            int count;
            Statement stmt = conn.createStatement();
            String sql = "select * from articletbl where code = '" + articleCode + "';";
            ResultSet rs = stmt.executeQuery(sql);
            count = 0;
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("name");
                int price = rs.getInt("price");
                article = new Article(code, name, price);
                count++;
            }
            if (count < 1) {
                throw new DBServerIFException("この商品はデータベースに登録されていません。");
            }
            if (count > 1) {
                throw new DBServerIFException("この商品はデータベースに重複登録されています。");
            }
            rs.close();
        }
        catch (SQLException ex) {
            throw new DBServerIFException("SQLException: " + ex.getMessage());
        }
        return article;
    }


    /*
     *  会員情報の登録を受け付ける。
     *  会員情報 member のとおりにデータベースに登録する。
     */

    public void registerMember(Member member) throws DBServerIFException {
        try {
            Statement stmt = conn.createStatement();
            String gender = (member.getGender() == Gender.Male) ? "m" : "f";
            String sql = String.format(
                    "INSERT INTO membertbl (id, name, furigana, gender, havepoints) VALUES ('%s', '%s', '%s', '%s', %d);",
                    member.getID(),
                    member.getName(),
                    member.getFurigana(),
                    (member.getGender() == Member.Gender.Male) ? "m" : "f",
                    member.getPoint()
            );
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected < 1) {
                throw new DBServerIFException("会員情報の登録に失敗しました。");
            }
        } catch (SQLException ex) {
            throw new DBServerIFException("SQLException: " + ex.getMessage());
        }
    }


    /*
     *  会員番号 membershipID の会員を検索する。
     */

    public Member findMember(String membershipID) throws DBServerIFException {
        Member member = null;

        try {
            int count;
            Statement stmt = conn.createStatement();
            String sql = "select * from membertbl where id='" + membershipID + "';";
            ResultSet rs = stmt.executeQuery(sql);
            count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String furigana = rs.getString("furigana");
                String mf = rs.getString("gender");
                int havepoints = rs.getInt("havepoints");
                Gender gender;
                if (mf.equals("m")) {
                    gender = Gender.Male;
                } else if (mf.equals("f")) {
                    gender = Gender.Female;
                } else {
                    gender = null;
                }
                havepoints = rs.getInt("havepoints");
                member = new Member(id, name, furigana, gender, havepoints);
                count++;
            }
            if (count < 1) {
                throw new DBServerIFException("この会員はデータベースに登録されていません。");
            }
            if (count > 1) {
                throw new DBServerIFException("この会員はデータベースに重複登録されています。");
            }
            rs.close();
        }
        catch (SQLException ex) {
            throw new DBServerIFException("SQLException: " + ex.getMessage());
        }
        return member;
    }


    /*
     *  当該会員情報の変更要求を受け付ける。
     *  会員情報 member のとおりにデータベースを更新する。
     */

    public static void updateMember(Member member) throws DBServerIFException {

        String sql = "UPDATE membertbl SET name = ?, furigana = ?, gender = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // パラメータの設定
            stmt.setString(1, member.getName());// 新しい名前
            stmt.setString(2, member.getFurigana());
            stmt.setString(3, (member.getGender() == Member.Gender.Male) ? "m" : "f");
            stmt.setString(4, member.getID()); // 更新対象のID

            // SQL文の実行
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("データが正常に更新されました！");
            } else {
                System.out.println("対象のデータが見つかりませんでした。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("データ更新中にエラーが発生しました: " + e.getMessage(), e);
        }

    }


    /*
     *  会員番号 memberID の会員を削除する。
     */

    public static void deleteMember(String memberID) throws DBServerIFException {

        try {
            Statement stmt = conn.createStatement();
            String sql = String.format(
                    "UPDATE membertbl SET name = NULL, furigana = NULL, gender = 'n', havepoints = 0 WHERE id = '%s'",
                    memberID
            );
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected < 1) {
                throw new SQLException("会員情報の削除に失敗しました。");
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
    }

    /*
    * 会員が持っている保有ポイントの6ヶ月失効機能
    */

    public static void pointExpire(String memberID) throws DBServerIFException {
        String selectAbleSql = "SELECT SUM(ablepoint) AS total_points FROM pointtbl WHERE id = ? AND date < DATE_SUB(CURDATE(), INTERVAL 6 MONTH)";
        String selectUsedSql = "SELECT SUM(usedpoint) AS total_points FROM pointtbl WHERE id = ? AND date < DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND ablepoint != 0";
        String updateSql = "UPDATE pointtbl SET ablepoint = 0 WHERE id = ? AND date < DATE_SUB(CURDATE(), INTERVAL 6 MONTH)";

        try (
                // SELECTクエリ用のPreparedStatement
                PreparedStatement selectAbleStmt = conn.prepareStatement(selectAbleSql);
                // SELECTクエリ用のPreparedStatement
                PreparedStatement selectUsedStmt = conn.prepareStatement(selectUsedSql);
                // UPDATEクエリ用のPreparedStatement
                PreparedStatement updateStmt = conn.prepareStatement(updateSql)
        ) {
            // SELECTクエリのパラメータ設定と実行
            selectAbleStmt.setString(1, memberID);
            ResultSet resultSet = selectAbleStmt.executeQuery();

            // SELECTクエリのパラメータ設定と実行
            selectUsedStmt.setString(1, memberID);
            ResultSet result = selectUsedStmt.executeQuery();

            int totalAblePoints = 0;
            int totalUsedPoints = 0;
            if (resultSet.next()) {
                totalAblePoints = resultSet.getInt("total_points");

            }
            if(result.next()){
                totalUsedPoints = result.getInt("total_points");
            }

            // 結果表示または処理
            System.out.println("Member ID: " + memberID);
            System.out.println("Total Able Points: " + totalAblePoints);

            // CheckArticlesScreenPanelのポイント更新
            CheckArticlesScreenPanel.havePoints -= (totalAblePoints - totalUsedPoints);

            // UPDATEクエリのパラメータ設定と実行
            updateStmt.setString(1, memberID);
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected < 1) {
                throw new SQLException("ポイント失効に失敗しました。対象となるデータが見つかりません。");
            }

            System.out.println("ポイントが正常に失効されました: " + rowsAffected + "行が更新されました。");
        } catch (SQLException ex) {
            // 詳細なエラー情報を含めて例外をスロー
            ex.getMessage();
        }
    }
}
