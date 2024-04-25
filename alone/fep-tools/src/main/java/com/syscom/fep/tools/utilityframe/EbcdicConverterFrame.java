package com.syscom.fep.tools.utilityframe;

import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.gui.AbstractFrame;
import com.syscom.fep.frmcommon.gui.AbstractPanel;
import com.syscom.fep.frmcommon.gui.AdvCombobox;
import com.syscom.fep.frmcommon.gui.GuiProperties;
import com.syscom.fep.frmcommon.gui.util.GuiUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class EbcdicConverterFrame extends AbstractFrame {
    private CcsidCombo ccsidCombo;
    private JButton toPlainBtn, toEbcdicBtn, clearBtn;
    private JTextArea plainArea, ebcdicArea;
    private JCheckBox plainHexChk, ebcdicHexChk, wholeChk;

    public static void main(String[] args) {
        EbcdicConverterFrame frame = new EbcdicConverterFrame("EBCDIC Convert Frame");
        frame.showFrame();
    }

    public EbcdicConverterFrame(String title) {
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
                toEbcdicBtn = GuiUtil.createButton("▼", listener);
                clearBtn = GuiUtil.createButton("×", listener);
                ccsidCombo = new CcsidCombo();
                plainArea = GuiUtil.createTextArea("", null, false);
                ebcdicArea = GuiUtil.createTextArea("0E5BBD5A41615D5D600F40404040407A40F1F1F6F4F1F9F64040404040404040404040404040400E5D785A414CC959E50F40404040407A40F2F0F2F261F0F861F0F340404040404040400E5BBD5A415F4166C90F40404040407A400E4C4B4CC10F4040404040404040404040404040404040400E5BBD5A414E880F404040404040407A40F8F0F5F0F0F1F24040404040404040400E4E4D5A414C4957485D600F4040407A40F0F2F2F0F3F0F0F0F5F6F5F5F2F0404040404040404040404040404040404040404040404040404040404040404040404040404040404040400E5C5B4E4D5A414C4957485D600F407A40F0F2F2F0F3F0F0F0F3F1F7F2F1F0404040404040404040404040404040404040404040404040404040404040404040404040404040404040400E5BBD5A4151D965EB0F40404040407A40404040404040404040404040F5F0F94BF0F04040404040404040404040404040404040404040404040404040404040404040404040404040400E4E4D5A414C4950904DD70F4040407A404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040400E5A615D7C5D9254E80F404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040400E5BBD5A414C4950904DD70F4040407A404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040400E5A615D7C4CC14DD70F404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040400E51E04FC50F4040404040404040407A40404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040C2C3D76DF0F7F2F84040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040400E5841404040404D920F40404040407A400E649B4E885FA74C4B0F4040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040", null, false);
                plainHexChk = GuiUtil.createCheckBox("Plain HEX");
                ebcdicHexChk = GuiUtil.createCheckBox("Ebcdic HEX");
                ebcdicHexChk.setSelected(true);
                ebcdicHexChk.setEnabled(false);
                wholeChk = GuiUtil.createCheckBox("Whole", listener);
                wholeChk.setSelected(true);
                listener.actionPerformed(new ActionEvent(wholeChk, ActionEvent.ACTION_PERFORMED, null));
            }

            @Override
            protected Component guiLayout() {
                JPanel contentPane = GuiUtil.createPanel(new GridBagLayout());
                contentPane.setBorder(GuiUtil.createLineBorder(GuiProperties.CLR_BORDER));
                contentPane.add(GuiUtil.createLabel(ccsidCombo.getCaption()), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        GuiUtil.createInsets(GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(ccsidCombo, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        GuiUtil.createInsets(GuiProperties.GAP, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(plainHexChk, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        GuiUtil.createInsets(GuiProperties.GAP, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(ebcdicHexChk, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        GuiUtil.createInsets(GuiProperties.GAP, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(wholeChk, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        GuiUtil.createInsets(GuiProperties.GAP, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

                contentPane.add(GuiUtil.createLabel("PLAIN"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(createJScrollPane(plainArea), new GridBagConstraints(1, 1, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        GuiUtil.createInsets(0, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

                JPanel btnPane = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, GuiProperties.GAP, 0));
                btnPane.add(toPlainBtn);
                btnPane.add(toEbcdicBtn);
                btnPane.add(clearBtn);
                contentPane.add(btnPane, new GridBagConstraints(0, 2, 5, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

                contentPane.add(GuiUtil.createLabel("EBCDIC"), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(createJScrollPane(ebcdicArea), new GridBagConstraints(1, 3, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
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
                        ebcdicArea.setText(StringUtils.EMPTY);
                    } else if (e.getSource().equals(toPlainBtn)) {
                        makeToPlain();
                    } else if (e.getSource().equals(toEbcdicBtn)) {
                        makeToEbcdic();
                    } else if (e.getSource().equals(wholeChk)) {
                        makeWholeChk();
                    }
                }

                private void makeWholeChk() {
                    if (wholeChk.isSelected()) {
                        ccsidCombo.setEnabled(false);
                        ccsidCombo.setSelectedIndex(1);
                        toEbcdicBtn.setEnabled(false);
                    } else {
                        ccsidCombo.setEnabled(true);
                        ccsidCombo.setSelectedIndex(0);
                        toEbcdicBtn.setEnabled(true);
                    }
                }

                private void makeToPlain() {
                    clearMessage();
                    String ebcdic = ebcdicArea.getText().trim();
                    if (StringUtils.isBlank(ebcdic)) {
                        showErrorMessage("Please input EBCDIC!!!");
                        return;
                    }
                    plainArea.setText(StringUtils.EMPTY);
                    boolean isPlainHex = plainHexChk.isSelected();
                    boolean isEbcdicHex = ebcdicHexChk.isSelected();
                    boolean isWhole = wholeChk.isSelected();
                    CCSID ccsid = (CCSID) ccsidCombo.getData();
                    String plain = StringUtils.EMPTY;
                    try (BufferedReader br = new BufferedReader(new StringReader(ebcdic))) {
                        String readLine = null;
                        while ((readLine = br.readLine()) != null) {
                            if (isPlainHex && isEbcdicHex) {
                                plain = isWhole ? EbcdicConverter.iWholeHexToJHex(readLine, ccsid) : EbcdicConverter.iHexToJHex(ccsid, readLine);
                            } else if (isPlainHex && !isEbcdicHex) {
                                plain = StringUtil.toHex(EbcdicConverter.toString(ccsid, readLine.getBytes(StandardCharsets.UTF_8)));
                            } else if (!isPlainHex && isEbcdicHex) {
                                plain = isWhole ? EbcdicConverter.fromWholeHex(readLine, ccsid) : EbcdicConverter.fromHex(ccsid, readLine);
                            } else if (!isPlainHex && !isEbcdicHex) {
                                plain = EbcdicConverter.toString(ccsid, readLine.getBytes(StandardCharsets.UTF_8));
                            }
                            plainArea.insert(plain, plainArea.getText().length());
                            plainArea.append("\r\n");
                        }
                        showMessage("Convert successful");
                    } catch (Throwable t) {
                        showErrorMessage(t, "Convert failed, cause ", t.getMessage());
                    }
                }

                private void makeToEbcdic() {
                    clearMessage();
                    String plain = plainArea.getText().trim();
                    if (StringUtils.isBlank(plain)) {
                        showErrorMessage("Please input PLAIN!!!");
                        return;
                    }
                    ebcdicArea.setText(StringUtils.EMPTY);
                    boolean isPlainHex = plainHexChk.isSelected();
                    boolean isEbcdicHex = ebcdicHexChk.isSelected();
                    CCSID ccsid = (CCSID) ccsidCombo.getData();
                    String ebcdic = StringUtils.EMPTY;
                    try (BufferedReader br = new BufferedReader(new StringReader(plain))) {
                        String readLine = null;
                        while ((readLine = br.readLine()) != null) {
                            if (isPlainHex && isEbcdicHex) {
                                ebcdic = EbcdicConverter.jHexToIHex(ccsid, readLine.length(), readLine);
                            } else if (isPlainHex && !isEbcdicHex) {
                                ebcdic = ConvertUtil.toString(EbcdicConverter.toBytes(ccsid, readLine.length() / 2, StringUtil.fromHex(readLine)), StandardCharsets.UTF_8);
                            } else if (!isPlainHex && isEbcdicHex) {
                                ebcdic = EbcdicConverter.toHex(ccsid, readLine.length() * (ccsid == CCSID.Traditional_Chinese ? 2 : 1), readLine);
                            } else if (!isPlainHex && !isEbcdicHex) {
                                ebcdic = ConvertUtil.toString(EbcdicConverter.toBytes(ccsid, readLine.length() * (ccsid == CCSID.Traditional_Chinese ? 2 : 1), readLine), StandardCharsets.UTF_8);
                            }
                            ebcdicArea.insert(ebcdic, ebcdicArea.getText().length());
                            ebcdicArea.append("\r\n");
                        }
                        showMessage("Convert successful");
                    } catch (Throwable t) {
                        showErrorMessage(t, "Convert failed, cause ", t.getMessage());
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

    private class CcsidCombo extends AdvCombobox {
        public CcsidCombo() {
            super();
            setEditable(false);
        }

        /**
         * @return
         */
        @Override
        public String getCaption() {
            return "CCSID";
        }

        /**
         * @return
         */
        @Override
        public List<ComboItem> getComboItemList() {
            return Arrays.asList(
                    new ComboItem(CCSID.English.name(), CCSID.English),
                    new ComboItem(CCSID.Traditional_Chinese.name(), CCSID.Traditional_Chinese)
            );
        }
    }
}
