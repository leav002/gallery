import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Gall_Main {
    private String UserId;
    private String UserName;

    public Gall_Main(String userId, String userName) {
        this.UserId = userId;
        this.UserName = userName;

        JFrame frame = new JFrame();
        frame.setTitle("게시판");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("메뉴");

        JMenuItem createpost = new JMenuItem("게시글 작성");
        JMenuItem searchpost = new JMenuItem("게시글 검색");
        JMenuItem viewAllposts = new JMenuItem("전체게시글");
        JMenuItem myposts = new JMenuItem("내 게시글 보기");

        menu.add(createpost);
        menu.add(viewAllposts);
        menu.add(searchpost);
        menu.add(myposts);
        menuBar.add(menu);

        //메뉴바 오른쪽 끝에 이름, 아이디
        JLabel userLabel = new JLabel(UserId + "/" + UserName);
        userLabel.setForeground(Color.BLACK);
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel.setPreferredSize(new Dimension(200, 30));

        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(userLabel);
        frame.setJMenuBar(menuBar);

        //게시글 테이블 설정
        String[] columnNames = {"제목", "작성자", "작성일"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable postTable = new JTable(tableModel);
        postTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postTable.setRowHeight(30);
        postTable.setFont(new Font("SanSerif", Font.PLAIN, 14));

        TableColumnModel columnModel = postTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(400);
        columnModel.getColumn(1).setPreferredWidth(10);
        columnModel.getColumn(2).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(postTable);
        scrollPane.setPreferredSize(new Dimension(900, 500));
        frame.add(scrollPane, BorderLayout.CENTER);

        loadPosts(tableModel);

        postTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = postTable.getSelectedRow();
                if (row != -1) {
                    String title = (String) tableModel.getValueAt(row, 0);
                    showPostDetail(title);
                }
            }
        });

        createpost.addActionListener(e -> {
            JFrame postFrame = new JFrame("게시글 작성");
            postFrame.setSize(800, 600);
            postFrame.setLocationRelativeTo(null);
            postFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            postFrame.setVisible(true);

            JLabel titleLabel = new JLabel("제목");
            JTextField titleField = new JTextField();
            titleLabel.setBounds(50, 50, 100, 30);
            titleField.setBounds(150, 50, 600, 30);

            JLabel contentLabel = new JLabel("내용");
            JTextArea contentArea = new JTextArea();
            JScrollPane contentScrollPane = new JScrollPane(contentArea);
            contentLabel.setBounds(50, 100, 100, 30);
            contentScrollPane.setBounds(150, 100, 600, 300);

            JButton submitButton = new JButton("게시글 작성");
            submitButton.setBounds(350, 450, 100, 40);

            submitButton.addActionListener(e1 -> {
                String title = titleField.getText();
                String content = contentArea.getText();

                try (Connection conn = connect()) {
                    String query = "INSERT INTO posts (user_id, title, content) VALUES (?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, UserId);
                    pstmt.setString(2, title);
                    pstmt.setString(3, content);
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(postFrame, "게시글 작성 완료: " + title);
                    postFrame.dispose();

                    loadPosts(tableModel);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(postFrame, "게시글 작성 실패");
                }
            });

            postFrame.setLayout(null);
            postFrame.add(titleLabel);
            postFrame.add(titleField);
            postFrame.add(contentLabel);
            postFrame.add(contentScrollPane);
            postFrame.add(submitButton);
        });

        viewAllposts.addActionListener(e -> loadPosts(tableModel));

        searchpost.addActionListener(e -> {
            String searchTerm = JOptionPane.showInputDialog(frame, "검색어를 입력하세요");
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                loadSearchedPosts(tableModel, searchTerm);
            }
        });

        myposts.addActionListener(e -> loadMyPosts(tableModel));
    }

    private void loadPosts(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        try (Connection conn = connect()) {
            String query = "SELECT title, user_id, created_at FROM posts ORDER BY id DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String title = rs.getString("title");
                String userId = rs.getString("user_id");
                Timestamp createdAt = rs.getTimestamp("created_at");

                Object[] row = {title, userId, createdAt.toString()};
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadSearchedPosts(DefaultTableModel tableModel, String searchTerm) {
        tableModel.setRowCount(0);

        try (Connection conn = connect()) {
            String query = "SELECT title, user_id, created_at FROM posts WHERE title LIKE ? OR content LIKE ? ORDER BY id DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + searchTerm + "%");
            pstmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String userId = rs.getString("user_id");
                Timestamp createdAt = rs.getTimestamp("created_at");

                Object[] row = {title, userId, createdAt.toString()};
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadMyPosts(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        try (Connection conn = connect()) {
            String query = "SELECT title, user_id, created_at FROM posts WHERE user_id = ? ORDER BY id DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, UserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String userId = rs.getString("user_id");
                Timestamp createdAt = rs.getTimestamp("created_at");

                Object[] row = {title, userId, createdAt.toString()};
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showPostDetail(String title) {
        try (Connection conn = connect()) {
            String query = "SELECT id, content, user_id FROM posts WHERE title = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int postId = rs.getInt("id");
                String content = rs.getString("content");
                String userId = rs.getString("user_id");

                JFrame detailFrame = new JFrame("게시글 상세보기");
                detailFrame.setSize(600, 600);
                detailFrame.setLocationRelativeTo(null);

                JPanel panel = new JPanel(new BorderLayout(10, 10));

                JPanel titlePanel = new JPanel(new BorderLayout());
                JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
                titleLabel.setFont(new Font("SanSerif", Font.BOLD, 18));
                titlePanel.add(titleLabel, BorderLayout.CENTER);

                if (userId.equals(UserId)) {
                    JButton editButton = new JButton("수정");
                    editButton.addActionListener(e -> editPost(title, content));
                    titlePanel.add(editButton, BorderLayout.EAST);
                }

                panel.add(titlePanel, BorderLayout.NORTH);

                JTextArea textArea = new JTextArea(content);
                textArea.setFont(new Font("SanSerif", Font.PLAIN, 14));
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                panel.add(scrollPane, BorderLayout.CENTER);

                JPanel commentPanel = new JPanel(new BorderLayout(10, 10));
                commentPanel.setBorder(BorderFactory.createTitledBorder("댓글"));

                DefaultListModel<String> commentListModel = new DefaultListModel<>();
                JList<String> commentList = new JList<>(commentListModel);
                JScrollPane commentScrollPane = new JScrollPane(commentList);
                commentScrollPane.setPreferredSize(new Dimension(550, 200));

                JTextField commentField = new JTextField();
                JButton addCommentButton = new JButton("댓글 추가");
                addCommentButton.addActionListener(e -> {
                    String comment = commentField.getText().trim();
                    if (!comment.isEmpty()) {
                        try (Connection commentConn = connect()) {
                            String insertCommentQuery = "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)";
                            PreparedStatement insertPstmt = commentConn.prepareStatement(insertCommentQuery);
                            insertPstmt.setInt(1, postId);
                            insertPstmt.setString(2, UserId);
                            insertPstmt.setString(3, comment);
                            insertPstmt.executeUpdate();

                            loadComments(postId, commentListModel);
                            commentField.setText("");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(detailFrame, "댓글 추가 실패");
                        }
                    }
                });

                JPanel commentInputPanel = new JPanel(new BorderLayout(5, 5));
                commentInputPanel.add(commentField, BorderLayout.CENTER);
                commentInputPanel.add(addCommentButton, BorderLayout.EAST);

                commentPanel.add(commentScrollPane, BorderLayout.CENTER);
                commentPanel.add(commentInputPanel, BorderLayout.SOUTH);

                panel.add(commentPanel, BorderLayout.SOUTH);

                detailFrame.add(panel);
                detailFrame.setVisible(true);

                loadComments(postId, commentListModel);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadComments(int postId, DefaultListModel<String> commentListModel) {
        commentListModel.clear();
        try (Connection conn = connect()) {
            String query = "SELECT user_id, content, created_at FROM comments WHERE post_id = ? ORDER BY created_at ASC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String userId = rs.getString("user_id");
                String content = rs.getString("content");
                Timestamp createdAt = rs.getTimestamp("created_at");
                commentListModel.addElement(userId + ": " + content + " (" + createdAt + ")");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void editPost(String title, String content) {
        JFrame editFrame = new JFrame("게시글 수정");
        editFrame.setSize(600, 400);
        editFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);

        JLabel titleLabel = new JLabel("제목:");
        titleLabel.setBounds(20, 20, 100, 30);
        JTextField titleField = new JTextField(title);
        titleField.setBounds(120, 20, 400, 30);

        JLabel contentLabel = new JLabel("내용:");
        contentLabel.setBounds(20, 70, 100, 30);
        JTextArea contentArea = new JTextArea(content);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentScrollPane.setBounds(120, 70, 400, 200);

        JButton saveButton = new JButton("저장");
        saveButton.setBounds(250, 300, 100, 40);
        saveButton.addActionListener(e -> {
            String newTitle = titleField.getText();
            String newContent = contentArea.getText();

            try (Connection conn = connect()) {
                String query = "UPDATE posts SET title = ?, content = ? WHERE title = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, newTitle);
                pstmt.setString(2, newContent);
                pstmt.setString(3, title);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(editFrame, "게시글 수정 완료");
                editFrame.dispose();
                loadPosts((DefaultTableModel) ((JTable) ((JScrollPane) editFrame.getContentPane().getComponent(0)).getViewport().getView()).getModel());
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editFrame, "게시글 수정 실패");
            }
        });

        panel.add(titleLabel);
        panel.add(titleField);
        panel.add(contentLabel);
        panel.add(contentScrollPane);
        panel.add(saveButton);

        editFrame.add(panel);
        editFrame.setVisible(true);
    }

    private Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/bulletin_board", "root", "password");
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            throw new SQLException("DB 연결 실패");
        }
    }

    public static void main(String[] args) {
        new Gall_Main("testUser", "테스트 사용자");
    }
}
