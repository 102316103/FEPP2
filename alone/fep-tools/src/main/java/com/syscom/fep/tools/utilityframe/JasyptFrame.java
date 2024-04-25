package com.syscom.fep.tools.utilityframe;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.gui.AbstractFrame;
import com.syscom.fep.frmcommon.gui.AbstractPanel;
import com.syscom.fep.frmcommon.gui.GuiProperties;
import com.syscom.fep.frmcommon.gui.util.GuiUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.StringReader;

public class JasyptFrame extends AbstractFrame {
    private static final String ENC_PREFIX = "ENC(";
    private JButton toPlainBtn, toEncryptBtn, clearBtn;
    private JTextArea plainArea, encryptArea;

    public static void main(String[] args) {
        JasyptFrame frame = new JasyptFrame("Jasypt Frame");
        frame.showFrame();
    }

    public JasyptFrame(String title) {
        super(title);
    }

    /**
     * @return
     */
    @Override
    protected Component guiLayout() {
        return new AbstractPanel() {
            @Override
            protected void initComponents() {
                MyActionListener listener = new MyActionListener();
                toPlainBtn = GuiUtil.createButton("▲", listener);
                toEncryptBtn = GuiUtil.createButton("▼", listener);
                clearBtn = GuiUtil.createButton("×", listener);
                plainArea = GuiUtil.createTextArea("", null, false);
                encryptArea = GuiUtil.createTextArea("tyVkEyFbW6yxYLwdOjnkK6DgJus9gSfDF2oIzlbWc646yy4Mrbpf8dYkrpLfe/9n\r\ntTRJ1NxEW5sURnNLFlKMd0MMIJ1oKYQBrO114sz+zat8547VnDhUKqyhFHQS/eZ4\r\nms0MUvpkRI1Y95+DFbo76uNbcact0LqDfpr/kXzNLpl1NCEM6sTWUb0tL/QEmEjf", null, false);
            }

            @Override
            protected Component guiLayout() {
                JPanel contentPane = GuiUtil.createPanel(new GridBagLayout());
                contentPane.setBorder(GuiUtil.createLineBorder(GuiProperties.CLR_BORDER));

                contentPane.add(GuiUtil.createLabel("PLAIN"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        GuiUtil.createInsets(GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(createJScrollPane(plainArea), new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        GuiUtil.createInsets(GuiProperties.GAP, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

                JPanel btnPane = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, GuiProperties.GAP, 0));
                btnPane.add(toPlainBtn);
                btnPane.add(toEncryptBtn);
                btnPane.add(clearBtn);
                contentPane.add(btnPane, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

                contentPane.add(GuiUtil.createLabel("ENCRYPT"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(createJScrollPane(encryptArea), new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        GuiUtil.createInsets(0, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                return contentPane;
            }

            private JScrollPane createJScrollPane(JTextArea area) {
                JScrollPane scrollPane = new JScrollPane();
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setViewportView(area);
                return scrollPane;
            }

            class MyActionListener implements ActionListener {

                /**
                 * Invoked when an action occurs.
                 *
                 * @param e the event to be processed
                 */
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource().equals(clearBtn)) {
                        plainArea.setText(StringUtils.EMPTY);
                        encryptArea.setText(StringUtils.EMPTY);
                    } else if (e.getSource().equals(toPlainBtn)) {
                        makePlain();
                    } else if (e.getSource().equals(toEncryptBtn)) {
                        makeEncrypt();
                    }
                }

                private void makePlain() {
                    clearMessage();
                    String encrypt = encryptArea.getText().trim();
                    if (StringUtils.isBlank(encrypt)) {
                        showErrorMessage("Please input ENCRYPT!!!");
                        return;
                    }
                    plainArea.setText(StringUtils.EMPTY);
                    String plain = StringUtils.EMPTY;
                    try (BufferedReader br = new BufferedReader(new StringReader(encrypt))) {
                        String readLine = null;
                        while ((readLine = br.readLine()) != null) {
                            int index = readLine.indexOf(ENC_PREFIX);
                            if (index == -1) {
                                plain = Jasypt.decrypt(readLine);
                            } else {
                                plain = StringUtils.join(
                                        readLine.substring(0, index),
                                        Jasypt.decrypt(readLine.substring(index + ENC_PREFIX.length(), readLine.length()))
                                );
                            }
                            plainArea.insert(plain, plainArea.getText().length());
                            plainArea.append("\r\n");
                        }
                        showMessage("Decrypt successful");
                    } catch (Throwable t) {
                        showErrorMessage(t, "Decrypt failed, cause ", t.getMessage());
                    }
                }

                private void makeEncrypt() {
                    clearMessage();
                    String plain = plainArea.getText().trim();
                    if (StringUtils.isBlank(plain)) {
                        showErrorMessage("Please input PLAIN!!!");
                        return;
                    }
                    encryptArea.setText(StringUtils.EMPTY);
                    String encrypt = StringUtils.EMPTY;
                    try (BufferedReader br = new BufferedReader(new StringReader(plain))) {
                        String readLine = null;
                        while ((readLine = br.readLine()) != null) {
                            encrypt = Jasypt.encrypt(readLine);
                            encryptArea.insert(encrypt, encryptArea.getText().length());
                            encryptArea.append("\r\n");
                        }
                        showMessage("Encrypt successful");
                    } catch (Throwable t) {
                        showErrorMessage(t, "Encrypt failed, cause ", t.getMessage());
                    }
                }
            }
        };
    }

    /**
     * @return
     */
    @Override
    protected JMenuBar createMenuBar() {
        return null;
    }
}
