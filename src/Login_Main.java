import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class Login_Main {
    public Login_Main() {
        JFrame frame = new JFrame();
        frame.setTitle("게시판");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.getContentPane().setLayout(null);

        Font font = new Font("SanSerif", Font.BOLD, 50);
        JLabel introducelogin = new JLabel(" 로그인/회원가입", SwingConstants.CENTER);
        JButton loginbutton = new JButton("로그인");
        JButton makeidbutton = new JButton("회원가입");

        introducelogin.setBounds(185, 120, 400, 60);
        loginbutton.setBounds(69, 340, 279, 60);
        makeidbutton.setBounds(416, 340, 279, 60);

        loginbutton.addActionListener(new loginbuttonListener());
        makeidbutton.addActionListener(new makeidbuttonListenner());

        introducelogin.setFont(font);
        frame.getContentPane().add(introducelogin);
        frame.getContentPane().add(loginbutton);
        frame.getContentPane().add(makeidbutton);
    }

    private class loginbuttonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFrame loginFrame = new JFrame("로그인");
            loginFrame.setSize(800, 600);
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
            loginFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            loginFrame.getContentPane().setLayout(null);

            Font loginFont = new Font("SanSerif", Font.BOLD, 50);
            JLabel loginLabel = new JLabel("로그인", SwingConstants.CENTER);
            loginLabel.setFont(loginFont);

            JLabel idLabel = new JLabel("ID:");
            JLabel pwLabel = new JLabel("Password:");

            loginLabel.setBounds(300, 100, 200, 50);
            idLabel.setBounds(160, 200, 50, 40);
            pwLabel.setBounds(160, 270, 80, 40);

            JTextField inputid = new JTextField(1);
            JPasswordField inputpw = new JPasswordField(1);

            inputid.setBounds(250, 200, 300, 40);
            inputpw.setBounds(250, 270, 300, 40);
            inputpw.setFont(new Font("SanSerif", Font.PLAIN, 18));
            inputpw.setEchoChar('*');

            JButton loginsubmitbutton = new JButton("로그인");
            loginsubmitbutton.setBounds(250, 350, 300, 50);

            loginsubmitbutton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String enteredId = inputid.getText();
                    String enteredPw = new String(inputpw.getPassword());

                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bulletin_board", "root", "password");
                         PreparedStatement pstmt = conn.prepareStatement("SELECT name FROM users WHERE id = ? AND pw = ?")) {

                        pstmt.setString(1, enteredId);
                        pstmt.setString(2, enteredPw);

                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            String name = rs.getString("name");
                            JOptionPane.showMessageDialog(loginFrame, "로그인 성공! " + name + "님 환영합니다.");

                            loginFrame.dispose();
                            new Gall_Main(enteredId, name);
                        } else {
                            JOptionPane.showMessageDialog(loginFrame, "아이디 또는 비밀번호가 잘못되었습니다.");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(loginFrame, "로그인 실패: " + ex.getMessage());
                    }
                }
            });

            loginFrame.getContentPane().add(loginLabel);
            loginFrame.getContentPane().add(idLabel);
            loginFrame.getContentPane().add(pwLabel);
            loginFrame.getContentPane().add(inputid);
            loginFrame.getContentPane().add(inputpw);
            loginFrame.getContentPane().add(loginsubmitbutton);
        }
    }

    private class makeidbuttonListenner implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFrame signUpFrame = new JFrame("회원가입");
            signUpFrame.setSize(800, 600);
            signUpFrame.setLocationRelativeTo(null);
            signUpFrame.setVisible(true);
            signUpFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            signUpFrame.getContentPane().setLayout(null);

            Font signUpFont = new Font("SanSerif", Font.BOLD, 50);
            JLabel signUpLabel = new JLabel("회원가입", SwingConstants.CENTER);
            signUpLabel.setFont(signUpFont);

            signUpLabel.setBounds(100, 100, 600, 50);

            JLabel idLabel = new JLabel("ID:");
            JLabel pwLabel = new JLabel("Password:");
            JLabel nameLabel = new JLabel("Name:");

            idLabel.setBounds(180, 200, 70, 40);
            pwLabel.setBounds(180, 270, 70, 40);
            nameLabel.setBounds(180, 340, 70, 40);

            JTextField inputid = new JTextField(1);
            JPasswordField inputpw = new JPasswordField(1);
            JTextField inputname = new JTextField(1);

            inputid.setBounds(250, 200, 300, 40);
            inputpw.setBounds(250, 270, 300, 40);
            inputname.setBounds(250, 340, 300, 40);

            inputpw.setFont(new Font("SanSerif", Font.PLAIN, 18));
            inputpw.setEchoChar('*');

            JButton signUpSubmitButton = new JButton("회원가입");
            signUpSubmitButton.setBounds(250, 410, 300, 50);

            signUpSubmitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String id = inputid.getText();
                    String pw = new String(inputpw.getPassword());
                    String name = inputname.getText();

                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bulletin_board", "root", "hordes0707");
                         PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM users WHERE id = ?");
                         PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (id, pw, name) VALUES (?, ?, ?)")) {

                        checkStmt.setString(1, id);
                        ResultSet rs = checkStmt.executeQuery();

                        if (rs.next()) {
                            JOptionPane.showMessageDialog(signUpFrame, "이미 존재하는 아이디입니다. 다른 아이디를 사용해주세요.");
                        } else {
                            insertStmt.setString(1, id);
                            insertStmt.setString(2, pw);
                            insertStmt.setString(3, name);

                            int rows = insertStmt.executeUpdate();
                            if (rows > 0) {
                                JOptionPane.showMessageDialog(signUpFrame, "회원가입 성공! 로그인 화면으로 돌아갑니다.");
                                signUpFrame.dispose();
                            }
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(signUpFrame, "회원가입 실패: " + ex.getMessage());
                    }
                }
            });

            signUpFrame.getContentPane().add(signUpLabel);
            signUpFrame.getContentPane().add(idLabel);
            signUpFrame.getContentPane().add(pwLabel);
            signUpFrame.getContentPane().add(nameLabel);
            signUpFrame.getContentPane().add(inputid);
            signUpFrame.getContentPane().add(inputpw);
            signUpFrame.getContentPane().add(inputname);
            signUpFrame.getContentPane().add(signUpSubmitButton);
        }
    }

    public static void main(String[] args) {
        new Login_Main();
    }
}
