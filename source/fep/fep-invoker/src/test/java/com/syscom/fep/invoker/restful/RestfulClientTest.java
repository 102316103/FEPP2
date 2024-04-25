package com.syscom.fep.invoker.restful;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.syscom.fep.invoker.RestfulClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.gui.AbstractFrame;
import com.syscom.fep.frmcommon.gui.AbstractPanel;
import com.syscom.fep.frmcommon.gui.AdvCombobox;
import com.syscom.fep.frmcommon.gui.AdvCombobox.ComboItem;
import com.syscom.fep.frmcommon.gui.GuiProperties;
import com.syscom.fep.frmcommon.gui.util.GuiUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.invoker.InvokerBaseTest;
import com.syscom.fep.vo.communication.ToFISCCommu;
import com.syscom.fep.vo.enums.ClientType;

public class RestfulClientTest extends InvokerBaseTest {
	static {
		System.setProperty("java.awt.headless", "false");
	}
	@Autowired
	private RestfulClientFactory factory;

	@Test
	public void testSendToFISC() {
		try {
			ToFISCCommu request = new ToFISCCommu();
			request.setEj(1);
			request.setMessage(
					"000000303230303231333035363032353733393530303030303830373030303031303039303831353036343130303030E5905ED024008004000008012B303030303030303034393930303033393435303030303131303039303738303735333535363330AA7822AA");
			request.setMessageId("02002130");
			request.setStan("100000");
			request.setTimeout(50);
			String rtn = factory.sendReceive(ClientType.TO_FISC_GW, null, request, 5000);
			LogHelperFactory.getUnitTestLogger().info(rtn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFrame() throws InterruptedException {
		new RestfulClientFrame("Restful Test Frame").showFrame();
		while (true) {
			Thread.sleep(24 * 60 * 60 * 60 * 1000);
		}
	}

	private class RestfulClientFrame extends AbstractFrame {
		private static final long serialVersionUID = 1L;

		public RestfulClientFrame(String title) {
			super(title);
		}

		@Override
		protected Component guiLayout() {
			return new RestfulClientPanel();
		}

		@Override
		protected JMenuBar createMenuBar() {
			return null;
		}
	}

	private class RestfulClientPanel extends AbstractPanel {
		private static final long serialVersionUID = 1L;
		private JButton sendBtn;
		private AdvCombobox restfulClientTypeCombo;
		private JPanel restfulPane;

		@Override
		protected void initComponents() {
			ActionLster listener = new ActionLster();
			restfulPane = GuiUtil.createPanel(new BorderLayout());
			sendBtn = GuiUtil.createButton("發送", listener);
			List<ComboItem> list = Arrays.asList(
					new ComboItem(ClientType.InQueue11X1.name(), new InQueue11X1Panel()),
					new ComboItem(ClientType.TO_FISC_GW.name(), new FISCPanel()));
			restfulClientTypeCombo = new AdvCombobox() {
				private static final long serialVersionUID = 1L;

				@Override
				public List<ComboItem> getComboItemList() {
					return list;
				}

				@Override
				public String getCaption() {
					return "類型";
				}
			};
			restfulClientTypeCombo.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						changePanel((JPanel) restfulClientTypeCombo.getData());
					}
				}
			});
//			changePanel((JPanel) ((ComboItem) list.get(0)).value);
		}

		private void changePanel(JPanel panel) {
			restfulPane.removeAll();
			restfulPane.add(panel, BorderLayout.CENTER);
			restfulPane.updateUI();
		}

		@Override
		protected Component guiLayout() {
			JPanel contentPane = GuiUtil.createPanel(new GridBagLayout());
			contentPane.setBorder(GuiUtil.createLineBorder(GuiProperties.CLR_BORDER));
			contentPane.add(GuiUtil.createLabel(restfulClientTypeCombo.getCaption()), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
					GuiUtil.createInsets(GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
			contentPane.add(restfulClientTypeCombo, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					GuiUtil.createInsets(GuiProperties.GAP, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

			contentPane.add(restfulPane, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

			JPanel btnPane = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT, GuiProperties.GAP, 0));
			btnPane.add(sendBtn);
			contentPane.add(btnPane, new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
			return contentPane;
		}

		final class ActionLster implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (sendBtn.equals(e.getSource())) {
					clearMessage();
					try {
						((RestfulMessageMesagePane) restfulClientTypeCombo.getData()).sendRestfulMessage();
						showMessage("發送成功");
					} catch (Exception ex) {
						showErrorMessage(ex, ex.getMessage());
						GuiUtil.showErrorMessage(RestfulClientPanel.this, "錯誤", ex.getMessage());
					}
				}
			}
		}

		final class FISCPanel extends RestfulMessageMesagePane {
			private static final long serialVersionUID = -1L;

			@Override
			public void sendRestfulMessage() throws Exception {
				// TODO 自動生成的方法存根

			}

		}

		final class InQueue11X1Panel extends RestfulMessageMesagePane {
			private static final long serialVersionUID = -1L;
			private JTextArea messageSendToArea;

			public InQueue11X1Panel() {
				messageSendToArea = GuiUtil.createTextArea(StringUtils.EMPTY, null, false);

				this.setLayout(new GridBagLayout());
				this.setBorder(GuiUtil.createLineBorder(GuiProperties.CLR_BORDER));

				this.add(GuiUtil.createLabel("Message To Send"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						GuiUtil.createInsets(GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
				this.add(new JScrollPane(messageSendToArea), new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						GuiUtil.createInsets(GuiProperties.GAP, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
			}

			@Override
			public void sendRestfulMessage() throws Exception {
				String message = messageSendToArea.getText();
				if (StringUtils.isBlank(message)) {
					String errorMessage = "請輸入發送訊息";
					messageSendToArea.requestFocus();
					throw ExceptionUtil.createException(errorMessage);
				}
				SpringBeanFactoryUtil.getBean(RestfulClientFactory.class).sendReceive(ClientType.InQueue11X1, "http://localhost:8082/inqueue/11X1", message, 1000);
			}
		}

		abstract class RestfulMessageMesagePane extends JPanel {
			private static final long serialVersionUID = -1L;

			public abstract void sendRestfulMessage() throws Exception;

		}
	}
}
