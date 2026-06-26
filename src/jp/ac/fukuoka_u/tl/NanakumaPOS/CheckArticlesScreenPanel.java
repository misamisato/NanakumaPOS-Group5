//-*- java -*-
/**********************************************************************************************************************
 *
 *  福岡大学工学部電子情報工学科プロジェクト型ソフトウェア開発演習教材
 *
 *  Copyright (C) 2015-2024 プロジェクト型ソフトウェア開発演習実施チーム
 *
 *  商品チェック画面を実現するクラス。
 *
 *********************************************************************************************************************/

package jp.ac.fukuoka_u.tl.NanakumaPOS;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

import jp.ac.fukuoka_u.tl.NanakumaPOS.DBServerIF.DBServerIFException;

import static java.lang.System.out;


/*
 *  商品チェック画面クラス
 */

public class CheckArticlesScreenPanel extends JPanel implements ActionListener, TableModelListener {
    /*
     *  商品チェック画面の状態を表す列挙型
     */

    public enum CheckArticlesScreenPanelState {
        // 未初期化
        NotInitialized,
        // 初期化済
        Initialized,
        // 商品チェック中
        CheckingArticles,
        // 決済済
        PaymentFinished,
    }

    // データベース接続
    private Connection conn = null;

    // データベース接続先 URL
    private String url = "jdbc:mysql://vhost.cx.tl.fukuoka-u.ac.jp:13306/nanakumapos5";

    // データベースユーザ名
    private String user = "b3pbl";

    // データベースパスワード
    private String password = "nanakumapbl";




    /*
     *  商品チェック画面クラスの唯一のインスタンス（Singleton パターン）
     */

    private static CheckArticlesScreenPanel checkArticlesScreenPanel = null;


    /*
     *  内部データ
     */

    static int havePoints;

    // 商品チェック中の登録会員。会員が確定していない間は null とする。
    private Member memberUnderChecking = null;

    // 商品チェック中の決済対象商品販売のリスト
    private SalesList salesUnderChecking = null;

    // フォーカスされている商品販売の番号
    private int salesIndexInFocus = -1;

    // 商品チェック画面の状態
    private CheckArticlesScreenPanelState state = CheckArticlesScreenPanelState.NotInitialized;


    /*
     *  ウィジェット
     */
    // 商品チェック画面のフレーム
    private JFrame frame = null;

    // スクロールペイン
    private JScrollPane scroll = null;

    // 販売商品表
    private JTable articlesTable = null;

    // 会員番号入力ボタン
    private JButton enterMembershipIDButton = null;

    // 販売単価変更ボタン
    private JButton changeSalesPriceButton = null;

    // 販売個数変更ボタン
    private JButton changeQuantityButton = null;

    // 決済ボタン
    private JButton paymentButton = null;

    // ホームボタン
    private JButton homeButton = null;

    // 商品コードラベル
    private JLabel articleCodeLabel = null;

    // 商品コード入力欄
    private JTextField articleCodeField = null;

    // 会員番号ラベル
    private JLabel memberIDLabel = null;

    // 会員番号欄
    private JTextField memberIDField = null;

    // 会員氏名ラベル
    private JLabel memberNameLabel = null;

    // 会員氏名欄
    private JTextField memberNameField = null;

    // 商品コード入力ボタン
    private JButton articleCodeButton = null;

    // 合計金額ラベル
    private JLabel totalPriceLabel = null;

    // 合計金額欄
    private JTextField totalPriceField = null;

    // お預かりラベル
    private JLabel paidPriceLabel = null;

    // お預かり金額欄
    private JTextField paidPriceField = null;

    //　付与ポイントラベル
    private JLabel havePointLabel = null;

    //　付与ポイント欄
    private JTextField havePointField = null;

    // おつりラベル
    private JLabel changePriceLabel = null;

    // おつり金額欄
    private JTextField changePriceField = null;


    /*
     *  コンストラクタ。
     *  商品チェック画面が保有するオブジェクトを生成する。
     */

    private CheckArticlesScreenPanel() {
        // 内部データを初期化する。
        frame = (JFrame)SwingUtilities.getRoot(this);
        state = CheckArticlesScreenPanelState.Initialized;
        salesIndexInFocus = -1;
        salesUnderChecking = new SalesList();

        // 自身を初期化する。
        setLayout(null);

        // 販売商品一覧画面を生成する。
        DefaultTableCellRenderer rightAlignmentRenderer = new DefaultTableCellRenderer();
        rightAlignmentRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        articlesTable = new JTable(salesUnderChecking);
        salesUnderChecking.addTableModelListener(this);
        articlesTable.setEnabled(false);
        articlesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        articlesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        articlesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        articlesTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        articlesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        articlesTable.getColumnModel().getColumn(3).setCellRenderer(rightAlignmentRenderer);
        articlesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        articlesTable.getColumnModel().getColumn(4).setCellRenderer(rightAlignmentRenderer);
        articlesTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        articlesTable.getColumnModel().getColumn(5).setCellRenderer(rightAlignmentRenderer);
        scroll = new JScrollPane(articlesTable);
        scroll.setBounds(16, 16, 800, 524);
        add(scroll);

        // 会員番号入力ボタンを生成する。
        enterMembershipIDButton = new JButton("会員番号入力");
        enterMembershipIDButton.setBounds(832, 16, 160, 48);
        enterMembershipIDButton.addActionListener(this);
        enterMembershipIDButton.setActionCommand("enterMembershipID");
        add(enterMembershipIDButton);

        // 販売単価変更ボタンを生成する。
        changeSalesPriceButton = new JButton("販売単価変更");
        changeSalesPriceButton.setBounds(832, 80, 160, 48);
        changeSalesPriceButton.addActionListener(this);
        changeSalesPriceButton.setActionCommand("changeSalesPrice");
        add(changeSalesPriceButton);

        // 販売個数変更ボタンを生成する。
        changeQuantityButton = new JButton("販売個数変更");
        changeQuantityButton.setBounds(832, 144, 160, 48);
        changeQuantityButton.addActionListener(this);
        changeQuantityButton.setActionCommand("changeQuantity");
        add(changeQuantityButton);

        // 決済ボタンを生成する。
        paymentButton = new JButton("決済");
        paymentButton.setBounds(832, 208, 160, 48);
        paymentButton.addActionListener(this);
        paymentButton.setActionCommand("payment");
        add(paymentButton);

        // ホームボタンを生成する。
        homeButton = new JButton("ホーム画面");
        homeButton.setBounds(832, 272, 160, 48);
        homeButton.addActionListener(this);
        homeButton.setActionCommand("home");
        add(homeButton);

        // 商品コード入力欄を生成する。
        articleCodeLabel = new JLabel("商品コード入力");
        articleCodeLabel.setBounds(16, 568, 100, 24);
        add(articleCodeLabel);
        articleCodeField = new JTextField(8);
        articleCodeField.setBounds(116, 568, 100, 24);
        articleCodeField.setBackground(Color.YELLOW);
        articleCodeField.setHorizontalAlignment(JTextField.LEFT);
        articleCodeField.setEditable(true);
        add(articleCodeField);

        // 商品コード入力ボタンを生成する。
        articleCodeButton = new JButton("入力");
        articleCodeButton.setBounds(226, 568, 80, 24);
        articleCodeButton.addActionListener(this);
        articleCodeButton.setActionCommand("articleCode");
        add(articleCodeButton);

        // 会員番号欄を生成する。
        memberIDLabel = new JLabel("会員番号");
        memberIDLabel.setBounds(396, 568, 100, 24);
        add(memberIDLabel);
        memberIDField = new JTextField(8);
        memberIDField.setBounds(496, 568, 100, 24);
        memberIDField.setBackground(Color.CYAN);
        memberIDField.setEditable(false);
        add(memberIDField);

        // 会員氏名欄を生成する。
        memberNameLabel = new JLabel("会員氏名");
        memberNameLabel.setBounds(396, 600, 100, 24);
        add(memberNameLabel);
        memberNameField = new JTextField(32);
        memberNameField.setBounds(496, 600, 100, 24);
        memberNameField.setBackground(Color.CYAN);
        memberNameField.setEditable(false);
        add(memberNameField);

        // お預かり欄を生成する。
        paidPriceLabel = new JLabel("お預かり");
        paidPriceLabel.setBounds(616, 568, 100, 24);
        add(paidPriceLabel);
        paidPriceField = new JTextField(8);
        paidPriceField.setBounds(716, 568, 100, 24);
        paidPriceField.setBackground(Color.CYAN);
        paidPriceField.setHorizontalAlignment(JTextField.RIGHT);
        paidPriceField.setEditable(false);
        add(paidPriceField);

        // 合計金額欄を生成する。
        totalPriceLabel = new JLabel("合計金額");
        totalPriceLabel.setBounds(616, 600, 100, 24);
        add(totalPriceLabel);
        totalPriceField = new JTextField(8);
        totalPriceField.setBounds(716, 600, 100, 24);
        totalPriceField.setBackground(Color.CYAN);
        totalPriceField.setHorizontalAlignment(JTextField.RIGHT);
        totalPriceField.setEditable(false);
        add(totalPriceField);

        // 所持ポイント欄を生成する。
        havePointLabel = new JLabel("所持ポイント");
        havePointLabel.setBounds(396, 632, 100, 24);
        add(havePointLabel);
        havePointField = new JTextField(8);
        havePointField.setBounds(496, 632, 100, 24);
        havePointField.setBackground(Color.CYAN);
        havePointField.setHorizontalAlignment(JTextField.RIGHT);
        havePointField.setEditable(false);
        add(havePointField);

        // おつり欄を生成する。
        changePriceLabel = new JLabel("おつり");
        changePriceLabel.setBounds(616, 632, 100, 24);
        add(changePriceLabel);
        changePriceField = new JTextField(8);
        changePriceField.setBounds(716, 632, 100, 24);
        changePriceField.setBackground(Color.CYAN);
        changePriceField.setHorizontalAlignment(JTextField.RIGHT);
        changePriceField.setEditable(false);
        add(changePriceField);

        // 内部データとウィジェットを初期化する。
        setState(CheckArticlesScreenPanelState.Initialized);
    }


    /*
     *  商品チェック画面の唯一のインスタンスへの参照を返す。
     */

    public static CheckArticlesScreenPanel getInstance() {
        if (checkArticlesScreenPanel == null) {
            checkArticlesScreenPanel = new CheckArticlesScreenPanel();
        }
        return checkArticlesScreenPanel;
    }


    /*
     *  商品チェック画面の状態を変更する。
     */

    public void setState(CheckArticlesScreenPanelState _state) {
        state = _state;
        switch (state) {
            case Initialized:
                // 商品チェック中のユーザが選択されていない状態にする。
                memberUnderChecking = null;
                // 決済対象商品販売リストをクリアする。
                salesUnderChecking.clear();
                salesUnderChecking.fireTableDataChanged();
                // ウィジェットを初期化する。
                articleCodeField.setText("");
                articleCodeField.setEnabled(true);
                articleCodeButton.setEnabled(true);
                memberIDField.setText("");
                memberNameField.setText("");
                paidPriceField.setText("");
                totalPriceField.setText("0");
                changePriceField.setText("");
                enterMembershipIDButton.setEnabled(true);
                changeSalesPriceButton.setEnabled(false);
                changeQuantityButton.setEnabled(false);
                paymentButton.setEnabled(false);
                articleCodeField.requestFocusInWindow();
                break;
            case CheckingArticles:
                articleCodeButton.setEnabled(true);
                enterMembershipIDButton.setEnabled(true);
                changeSalesPriceButton.setEnabled(false);
                changeQuantityButton.setEnabled(false);
                paymentButton.setEnabled(true);
                break;
            case PaymentFinished:
                articlesTable.clearSelection();
                articleCodeField.setEnabled(false);
                articleCodeButton.setEnabled(false);
                enterMembershipIDButton.setEnabled(false);
                changeSalesPriceButton.setEnabled(false);
                changeQuantityButton.setEnabled(false);
                paymentButton.setEnabled(false);
                break;
        }
    }


    /*
     *  商品コード入力欄をクリアする。
     */

    public void clearArticleCodeField() {
        articleCodeField.setText("");
        articleCodeField.requestFocusInWindow();
    }


    /*
     *  お預かり欄に金額を表示する。
     */

    public void setPaidPrice(int paidPrice) {
        paidPriceField.setText(Integer.toString(paidPrice));
    }

    public void setTotalPrice(int totalPrice) {
        totalPriceField.setText(Integer.toString(totalPrice));
    }

    // 所持ポイント欄にポイントを表示する
    public void sethavePoint(int havePoints) {
        havePointField.setText(Integer.toString(havePoints));
    }

    /*
     *  おつり欄に金額を表示する。
     */

    public void setChangePrice(int changePrice) {
        changePriceField.setText(Integer.toString(changePrice));
    }


    /*
     *  商品販売表上の idx 番目の商品販売にフォーカスをあてる。
     */

    public void focusSales(int idx) {
        // 商品チェック画面が商品チェック中状態の場合のみ処理する。
        if (state == CheckArticlesScreenPanelState.CheckingArticles) {
            // 商品販売表上の当該商品販売にフォーカスをあてる。
            salesIndexInFocus = idx;
            articlesTable.setRowSelectionInterval(idx, idx);
            // 当該商品販売の販売単価と販売個数を変更できるように販売単価変更ボ
            // タンと販売個数変更ボタンを有効にする。
            changeSalesPriceButton.setEnabled(true);
            changeQuantityButton.setEnabled(true);
        }
    }


    /*
     *  商品販売表上の商品販売のフォーカスをはずす。
     */

    public void unfocusSales() {
        // 商品販売表上のすべての商品販売のフォーカスをはずす。
        salesIndexInFocus = -1;
        articlesTable.clearSelection();
        // 販売単価変更ボタンと販売個数変更ボタンを無効にする。
        changeSalesPriceButton.setEnabled(false);
        changeQuantityButton.setEnabled(false);
    }


    /*
     *  商品コードが入力されたときに呼び出される。
     */

    private void articleCodeEntered() {
        String articleCode = articleCodeField.getText();
        int idx;
        Article article;
        Sale sale;

        // 決済対象商品販売のリストに当該コードの商品の販売が含まれていないか検査する。
        idx = salesUnderChecking.find(articleCode);
        // 含まれている場合，
        if (idx >= 0) {
            // 商品販売表中当該商品販売にフォーカスをあてる。
            focusSales(idx);
            // チェック済みの商品であることを店員に知らせる。
            JOptionPane.showMessageDialog(frame,  "すでにチェックされている商品です。販売個数を確認してください。", "注意", JOptionPane.WARNING_MESSAGE);
            // 商品チェック画面の商品コード入力欄をクリアしフォーカスをあてる。
            clearArticleCodeField();
            return;
        }

        // 含まれていない場合，
        try {
            // 商品の名称と定価をデータベースから検索する。
            article = DBServerIF.getInstance().findArticle(articleCode);
            // 当該商品販売を決済対象商品販売リストに追加する。
            sale = new Sale(article.getArticleCode(), article.getArticleName(), article.getCataloguePrice(), 1);
            salesUnderChecking.add(sale);
            idx = salesUnderChecking.getNumOfSales() - 1;
            salesUnderChecking.fireTableRowsInserted(idx, idx);
            // 商品チェック画面を商品チェック中状態にする。
            setState(CheckArticlesScreenPanelState.CheckingArticles);
            // 商品販売表中の当該商品販売にフォーカスをあてる。
            focusSales(idx);
            // カスタマディスプレイに当該商品販売を表示する。
            displaySalesOnCustomerDisplay(sale);
            // 商品チェック画面の商品コード入力欄をクリアしフォーカスをあてる。
            clearArticleCodeField();
        }
        catch (DBServerIFException ex) {
            // データベースそのものに問題がある場合，
            // またはデータベースのアクセスに問題がある場合，
            // 商品販売表中のすべての商品販売についてフォーカスをはずす。
            unfocusSales();
            // 問題の発生を店員に知らせる。
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }
        articleCodeField.requestFocusInWindow();
    }


    /*
     *  販売単価の変更が要求されたときに呼び出される。
     */

    private void changeSalesPriceRequested() {
        // フォーカスされている商品販売の販売単価を取得する。
        int salesPrice = salesUnderChecking.getIthSale(salesIndexInFocus).getSalesPrice();

        // 店員に販売単価の入力を求める。
        String str = JOptionPane.showInputDialog(frame, "販売単価を入力してください。", salesPrice);
        if (str != null) {
            // 入力があった場合，
            try {
                // 入力内容（文字列）を販売単価（整数）に変換する。
                salesPrice = Integer.parseInt(str);
                // 販売単価が0未満の場合は例外を投げる。
                if (salesPrice < 0) {
                    throw new NumberFormatException();
                }
                // 決済対象商品販売リストを更新する。
                salesUnderChecking.getIthSale(salesIndexInFocus).setSalesPrice(salesPrice);
                salesUnderChecking.fireTableRowsUpdated(salesIndexInFocus, salesIndexInFocus);
                // カスタマディスプレイに表示する。
                displaySalesOnCustomerDisplay(salesUnderChecking.getIthSale(salesIndexInFocus));
            }
            // 入力はあってもその書式が不正の場合，
            catch (NumberFormatException ex) {
                // 店員にエラーを通知する。
                JOptionPane.showMessageDialog(frame, "販売単価の入力が不正です。", "エラー", JOptionPane.ERROR_MESSAGE);
            }
        }
        // 商品コード入力欄にフォーカスをあてる。
        articleCodeField.requestFocusInWindow();
    }


    /*
     *  会員番号の入力が要求されたときに呼び出される。
     */

    private void memberIDEnteringRequested() {
        // 店員に会員番号の入力を求める。
        String memberID = JOptionPane.showInputDialog(frame, "会員番号を入力してください。");
        try {
            Member member = DBServerIF.getInstance().findMember(memberID);
            if (member != null) {
                if (unavailableMemberID(member)) {
                    throw new Exception("現在この会員コードは利用されていません") ;
                }
                memberUnderChecking = member;
                memberIDField.setText(member.getID());
                memberNameField.setText(member.getName());
                havePoints = member.getPoint();
                if(havePoints>0) {
                    DBServerIF.pointExpire(member.getID());
                }
                sethavePoint(havePoints);
            }
        }
        catch (DBServerIFException ex) {
            // 問題の発生を店員に知らせる。
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception e) {
            // その他のエラーを店員に知らせる。
            JOptionPane.showMessageDialog(frame, e.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
        articleCodeField.requestFocusInWindow();
    }


    /*
     *  商品販売 sale に関する情報をカスタマディスプレイに表示する。
     */

    private void displaySalesOnCustomerDisplay(Sale sale) {
        AbstractedCustomerDisplayIF customerDisplayIF = POSTerminalApp.getInstance().getCustomerDisplayIF();
        customerDisplayIF.displayUpperMessage(sale.getArticleName(), AbstractedCustomerDisplayIF.Alignment.LEFT);
        String buf = "@" + Integer.toString(sale.getSalesPrice()) + "x" + Integer.toString(sale.getSalesQuantity());
        customerDisplayIF.displayLowerMessage(buf, AbstractedCustomerDisplayIF.Alignment.RIGHT);
    }


    /*
     *  販売個数の変更が要求されたときに呼び出される。
     */

    private void changeSalesQuantityRequested() {
        // フォーカスされている商品販売の販売個数を取得する。
        int salesQuantity = salesUnderChecking.getIthSale(salesIndexInFocus).getSalesQuantity();

        // 店員に販売個数の入力を求める。
        String str = JOptionPane.showInputDialog(frame, "販売個数を入力してください。", salesQuantity);
        if (str != null) {
            // 入力があった場合，
            try {
                // 入力内容（文字列）を販売個数（整数）に変換する。
                salesQuantity = Integer.parseInt(str);
                // 販売個数が0未満の場合は例外を投げる。
                if (salesQuantity < 0) {
                    throw new NumberFormatException();
                }
                // 販売個数の変更を確定する。
                salesUnderChecking.getIthSale(salesIndexInFocus).setSalesQuantity(salesQuantity);
                salesUnderChecking.fireTableRowsUpdated(salesIndexInFocus, salesQuantity);
                displaySalesOnCustomerDisplay(salesUnderChecking.getIthSale(salesIndexInFocus));
            }
            // 入力はあってもその書式が不正の場合，
            catch (NumberFormatException ex) {
                // 店員にエラーを通知する。
                JOptionPane.showMessageDialog(frame, "販売個数の入力が不正です。", "エラー", JOptionPane.ERROR_MESSAGE);
            }
        }
        // 商品コード入力欄にフォーカスをあてる。
        articleCodeField.requestFocusInWindow();
    }

    public void receiptPrint(int _usePoint, int _totalPrice, int _paidPrice, int _changePrice){

        int idx = salesUnderChecking.getNumOfSales();
        AbstractedPrinterIF printerIF = POSTerminalApp.getInstance().getPrinterIF();


        for(int i = 0;i < idx; i++){
            printerIF.print(salesUnderChecking.getIthSale(i).getArticleName());
            printerIF.print("  ");
            printerIF.print(salesUnderChecking.getIthSale(i).getSalesPrice());
            printerIF.print("円");
            printerIF.print("  ");
            printerIF.print(salesUnderChecking.getIthSale(i).getSalesQuantity());
            printerIF.printLine("個");
        }

        if(memberUnderChecking != null){
            printerIF.printLine("");
            printerIF.print("使用ポイント：");
            printerIF.print(_usePoint);
            printerIF.printLine("ポイント");
        }

        printerIF.printLine("");
        printerIF.print("合計金額：");
        printerIF.print(_totalPrice);
        printerIF.printLine("円");

        printerIF.printLine("");
        printerIF.print("預かり金額：");
        printerIF.print(_paidPrice);
        printerIF.printLine("円");

        printerIF.printLine("");
        printerIF.print("おつり：");
        printerIF.print(_changePrice);
        printerIF.printLine("円");
    }


    /*
     *  決済が要求された場合に呼び出される。
     */

    public boolean paymentRequested() throws SQLException, DBServerIFException {
        AbstractedCustomerDisplayIF customerDisplayIF = POSTerminalApp.getInstance().getCustomerDisplayIF();
        AbstractedCashDrawerIF cashDrawerIF = POSTerminalApp.getInstance().getCashDrawerIF();


        // 決済対象商品販売の合計金額を得る。
        int totalPrice = salesUnderChecking.getTotalPrice();

        // カスタマディスプレイに合計金額を表示する。
        customerDisplayIF.displayUpperMessage("合計金額", AbstractedCustomerDisplayIF.Alignment.LEFT);
        customerDisplayIF.displayLowerMessage(Integer.toString(totalPrice), AbstractedCustomerDisplayIF.Alignment.RIGHT);

        // お預かりの入力を求める。
        PaymentDialog paymentDialog = new PaymentDialog(frame, totalPrice);
        paymentDialog.setVisible(true);

        // お預かりの入力がキャンセルされた場合は決済もキャンセルする。
        if (!paymentDialog.isConfirmed()) {
            articleCodeField.requestFocusInWindow();
            return false;
        }

        int usePoints = paymentDialog.getUsedPoints();

        totalPrice -= usePoints;

        // お預かり額を得る。
        int paidPrice = paymentDialog.getPaidPrice();

        //　付与ポイントを計算する
        int getPoint = (int)(totalPrice*0.01);

        havePoints += getPoint;

        havePoints -= usePoints;

        // おつりを計算する。
        int changePrice = paidPrice - totalPrice;

        // お預かり額とおつりを商品チェック画面に表示する。
        setPaidPrice(paidPrice);
        setTotalPrice(totalPrice);
        setChangePrice(changePrice);

        if(memberUnderChecking != null) {
            sethavePoint(havePoints);
        }


        // カスタマディスプレイにおつりを表示する。
        customerDisplayIF.displayUpperMessage("おつり", AbstractedCustomerDisplayIF.Alignment.LEFT);
        customerDisplayIF.displayLowerMessage(Integer.toString(changePrice), AbstractedCustomerDisplayIF.Alignment.RIGHT);

        // レシートを印刷する。
        receiptPrint(usePoints, totalPrice, paidPrice, changePrice);

        // キャッシュドロワを開ける。
        cashDrawerIF.openDrawer();


        this.conn = DriverManager.getConnection(url, user, password);

        // データベースを更新する。
        if(memberUnderChecking != null){

            String sql = "UPDATE membertbl SET havepoints = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // パラメータの設定
                stmt.setInt(1, havePoints); // 新しいポイント値
                stmt.setString(2, memberUnderChecking.getID()); // 更新対象のID

                // SQL文の実行
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    out.println("データが正常に更新されました！");
                } else {
                    out.println("対象のデータが見つかりませんでした。");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("データ更新中にエラーが発生しました: " + e.getMessage(), e);
            }

        }

        //購入データをデータベースに登録
        try {

            int idx = salesUnderChecking.getNumOfSales();

            Statement stmt = conn.createStatement();

            for(int i = 0;i < idx; i++) {
                String sql = String.format(
                        "INSERT INTO purchasetbl (code, price, num, date) VALUES ('%s', '%d', '%d', NOW());",

                        salesUnderChecking.getIthSale(i).getArticleCode(),
                        salesUnderChecking.getIthSale(i).getSalesPrice(),
                        salesUnderChecking.getIthSale(i).getSalesQuantity()
                );
                int rowsAffected = stmt.executeUpdate(sql);
                if (rowsAffected < 1) {
                    throw new DBServerIFException("購入履歴の登録に失敗しました。");
                }
            }
        } catch (SQLException ex) {
            throw new DBServerIFException("SQLException: " + ex.getMessage());
        }

        // 購入データに基づくポイントをデータベースに登録
            try {
                if(memberUnderChecking != null ){
                    Statement stmt = conn.createStatement();  // SQLステートメント作成
                    // ポイント登録SQL
                    String sql = String.format(
                            "INSERT INTO pointtbl(id, getPoint, usedPoint,date,ablePoint) VALUES ('%s','%d','%d', NOW(),'%d');",
                            memberUnderChecking.getID(),
                            getPoint,
                            paymentDialog.getUsedPoints(),
                            getPoint
                    );
                    int rowsAffected = stmt.executeUpdate(sql);
                    if (rowsAffected < 1) {
                        throw new DBServerIFException("会員情報の登録に失敗しました。");
                    }
                }
            } catch (SQLException ex) {
                throw new DBServerIFException("SQLException: " + ex.getMessage());
            }

        // 商品チェック画面を決済済み状態にする。
        checkArticlesScreenPanel.setState(CheckArticlesScreenPanelState.PaymentFinished);
        homeButton.requestFocusInWindow();
        return true;
    }


    /*
     *  商品チェックのキャンセルが要求されたときに呼び出される。
     */

    private void checkArticlesCancelled() {
        AbstractedCustomerDisplayIF customerDisplayIF = POSTerminalApp.getInstance().getCustomerDisplayIF();
        AbstractedPrinterIF printerIF = POSTerminalApp.getInstance().getPrinterIF();
        switch (state) {
            case Initialized:
            case PaymentFinished:
                // 商品チェック画面が初期化済み状態の場合，商品は何もチェックされて
                // いない状態なので問答無用でキャンセルして構わない。
                // 商品チェック画面が決済済みの場合，商品はすべてチェックされて決済
                // が完了している状態なので問答無用でキャンセルして構わない。
                // カスタマディスプレイの表示をクリアする。
                customerDisplayIF.clear();
                //プリンタの表示をクリアする
                printerIF.initialize();
                havePointField.setText("");
                POSTerminalApp.getInstance().returnToHomeScreen();
                break;
            case CheckingArticles:
                // 商品チェック画面が商品チェック中状態の場合，キャンセルするとそれ
                // までの入力が無駄になるので，店員に確認が必要である。
                if (JOptionPane.showConfirmDialog(frame, "商品のチェックをキャンセルしますか？", "確認", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    // キャンセル応諾の場合，商品チェックをキャンセルする。
                    customerDisplayIF.clear();
                    havePointField.setText("");
                    POSTerminalApp.getInstance().returnToHomeScreen();
                } else {
                    // キャンセル拒否の場合，商品コード入力欄にフォーカスをあてる。
                    articleCodeField.requestFocusInWindow();
                }
                break;
        }
    }


    /*
     *  商品チェック画面上のボタンが押されるときに呼び出される。
     */

    @Override
    public void actionPerformed (ActionEvent e) {

        String cmd = e.getActionCommand();
        switch (cmd)
        {
            case "articleCode":
                // 商品コード入力ボタンが押下されたときは商品コード入力処理を呼び出す。
                articleCodeEntered();
                break;
            case "enterMembershipID":
                // 会員番号入力ボタンが押下されたときは会員番号入力処理を呼び出す。
                memberIDEnteringRequested();
                break;
            case "changeSalesPrice":
                // 商品単価変更ボタンが押下されたときは商品単価変更処理を呼び出す。
                changeSalesPriceRequested();
                break;
            case "changeQuantity":
                // 商品個数変更ボタンが押下されたときは商品個数変更処理を呼び出す。
                changeSalesQuantityRequested();
                break;
            case "payment":
                // 決済ボタンが押下されたときは決済処理を呼び出す。
                try {
                    paymentRequested();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } catch (DBServerIFException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case "home":
                // ホーム画面ボタンが押下されたときは商品チェックキャンセル処理を呼び出す。
                checkArticlesCancelled();
                break;
        }
    }


    /*
     *  決済対象商品販売リストが更新されたら呼び出される。
     */

    @Override
    public void tableChanged(TableModelEvent e) {
        // 合計金額欄の表示を更新する。
        totalPriceField.setText(Integer.toString(salesUnderChecking.getTotalPrice()));
    }

    /*
     *  会員が利用できるかどうか確認する
     *  使えないのであれば真を返す
     */
    private boolean unavailableMemberID(Member member) {
        if (member.getName() == null && member.getFurigana() == null && member.getGender() == null && member.getPoint() == 0)
            return true;
        return false;
    }
}
