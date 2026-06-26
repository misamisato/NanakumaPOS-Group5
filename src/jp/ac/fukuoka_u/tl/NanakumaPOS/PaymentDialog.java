//-*- java -*-
/**********************************************************************************************************************
 *
 *  福岡大学工学部電子情報工学科プロジェクト型ソフトウェア開発演習教材
 *
 *  Copyright (C) 2015-2024 プロジェクト型ソフトウェア開発演習実施チーム
 *
 *  「決済」ダイアログ。
 *
 *********************************************************************************************************************/

package jp.ac.fukuoka_u.tl.NanakumaPOS;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


/*
 *  決済ダイアログクラス
 */

public class PaymentDialog extends JDialog implements ActionListener {
    /*
     *  内部データ
     */

    // 合計金額
    private int totalPrice = 0;

    // お預かり
    private int paidPrice = 0;

    //使用ポイント
    private int usePoints = 0;


    // OKボタンが押されたか
    private boolean confirmed = false;

    /*
     *  ウィジェット
     */
    // ダイアログ
    private JFrame owner = null;

    // 合計金額欄ラベル
    private JLabel totalPriceLabel = null;

    // 合計金額欄
    private JTextField totalPriceField = null;

    //所有ポイントラベル
    private JLabel havePointsLabel = null;

    //所有ポイント欄
    private JTextField havePointsField = null;

    //使用ポイントラベル
    private JLabel usePointsLabel = null;

    //使用ポイント欄
    private JTextField usePointsField = null;

    // お預かり欄ラベル
    private JLabel paidPriceLabel = null;

    // お預かり欄
    private JTextField paidPriceField = null;

    // 決済ボタン
    private JButton okButton = null;

    // 中止ボタン
    private JButton cancelButton = null;


    /*
     *  コンストラクタ
     */

    public PaymentDialog(JFrame _owner, int _totalPrice) {
        super(_owner, true);
        owner = _owner;
        totalPrice = _totalPrice;
        confirmed = false;

        setLayout(null);
        setTitle("決済");
        setSize(248, 250);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        // 合計金額欄を生成する。
        totalPriceLabel = new JLabel("合計金額");
        totalPriceLabel.setBounds(16, 16, 100, 24);
        contentPane.add(totalPriceLabel);
        totalPriceField = new JTextField (8);
        totalPriceField.setBounds(116, 16, 100, 24);
        totalPriceField.setBackground(Color.YELLOW);
        totalPriceField.setHorizontalAlignment(JTextField.RIGHT);
        totalPriceField.setEditable(false);
        totalPriceField.setFocusable(false);
        totalPriceField.setText(Integer.toString(totalPrice));
        contentPane.add(totalPriceField);

        // お預かり欄を生成する。
        paidPriceLabel = new JLabel("お預かり");
        paidPriceLabel.setBounds(16, 112, 100, 24);
        contentPane.add(paidPriceLabel);
        paidPriceField = new JTextField (8);
        paidPriceField.setBounds(116, 112, 100, 24);
        paidPriceField.setBackground(Color.YELLOW);
        paidPriceField.setHorizontalAlignment(JTextField.RIGHT);
        paidPriceField.setEditable(true);
        contentPane.add(paidPriceField);

        // 現在のポイント残高
        havePointsLabel = new JLabel("ポイント残高");
        havePointsLabel.setBounds(16, 48, 100, 24);
        contentPane.add(havePointsLabel);
        havePointsField = new JTextField(8);
        havePointsField.setBounds(116, 48, 100, 24);
        havePointsField.setBackground(Color.YELLOW);
        havePointsField.setHorizontalAlignment(JTextField.RIGHT);
        havePointsField.setEditable(false);
        havePointsField.setFocusable(false);
        havePointsField.setText(Integer.toString(CheckArticlesScreenPanel.havePoints));
        contentPane.add(havePointsField);

        // ポイント使用欄
        usePointsLabel = new JLabel("使用ポイント");
        usePointsLabel.setBounds(16, 80, 100, 24);
        contentPane.add(usePointsLabel);
        usePointsField = new JTextField(8);
        usePointsField.setBounds(116, 80, 100, 24);
        usePointsField.setBackground(Color.YELLOW);
        usePointsField.setHorizontalAlignment(JTextField.RIGHT);
        usePointsField.setEditable(true);
        contentPane.add(usePointsField);

        // OKボタンを生成する。
        okButton = new JButton("決済");
        okButton.setBounds(26, 160, 80, 24);
        okButton.addActionListener(this);
        okButton.setActionCommand("ok");
        contentPane.add(okButton);

        // キャンセルボタンを生成する。
        cancelButton = new JButton("中止");
        cancelButton.setBounds(126, 160, 80, 24);
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");
        contentPane.add(cancelButton);
    }


    /*
     *  決済ダイアログを閉じるときにOKボタンが押されたかを返す。
     */

    public boolean isConfirmed() {
        return confirmed;
    }


    /*
     *  お預かり額を返す。
     */

    public int getPaidPrice() {
        return paidPrice;
    }

    //使用されたポイントを返す。
    public int getUsedPoints() {
        return usePoints;
    }


    /*
     *  決済の意思が確認されたときに呼び出される。
     */

    private void paymentConfirmed() {

        try {
            String paidPriceText = paidPriceField.getText().trim();
            paidPrice = paidPriceText.isEmpty() ? 0 : Integer.parseInt(paidPriceText);
            String usePointsText = usePointsField.getText().trim();
            usePoints = usePointsText.isEmpty() ? 0 : Integer.parseInt(usePointsText);

            if(paidPrice < 0 || usePoints < 0){
                JOptionPane.showMessageDialog(owner, "マイナスは使用できません。", "エラー", JOptionPane.ERROR_MESSAGE);
                paidPriceField.requestFocusInWindow();
                return;
            }
        }
        catch (NumberFormatException ex) {
            paidPrice = 0;
            usePoints = 0;
            JOptionPane.showMessageDialog(owner, "お預かり、またはポイントの入力が不正です。", "エラー", JOptionPane.ERROR_MESSAGE);
            paidPriceField.requestFocusInWindow();
            return;
        }
        if(paidPrice >= 0 || usePoints >= 0) {

            if (totalPrice > paidPrice + usePoints) {
                JOptionPane.showMessageDialog(owner, "お預かりが不足しています。", "エラー", JOptionPane.ERROR_MESSAGE);
                paidPriceField.requestFocusInWindow();
                return;
            }
            if (CheckArticlesScreenPanel.havePoints < usePoints) {
                JOptionPane.showMessageDialog(owner, "ポイント残高が不足しています。", "エラー", JOptionPane.ERROR_MESSAGE);
                usePointsField.requestFocusInWindow();
                return;
            }
            if (totalPrice < usePoints) {
                JOptionPane.showMessageDialog(owner, "ポイント使用上限を超えています。", "エラー", JOptionPane.ERROR_MESSAGE);
                usePointsField.requestFocusInWindow();
                return;
            }
        }
        confirmed = true;
        dispose();
    }


    /*
     *  決済の意思が中止されたときに呼び出される。
     */

    private void paymentCancelled() {
        paidPrice = 0;
        usePoints = 0;
        confirmed = false;
        dispose();
    }


    /*
     *  ボタンが押されたときに呼び出される。
     */

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            paymentConfirmed();
        } else if (e.getActionCommand().equals("cancel")) {
            paymentCancelled();
        }
    }
}

