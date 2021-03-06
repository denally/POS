package com.floreantpos.ui.forms;

import java.awt.FlowLayout;
import java.lang.ref.SoftReference;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.hibernate.StaleObjectStateException;

import com.floreantpos.bo.ui.BOMessageDialog;
import com.floreantpos.model.ShopTable;
import com.floreantpos.model.dao.ShopTableDAO;
import com.floreantpos.model.util.IllegalModelStateException;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.IntegerTextField;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.util.POSUtil;

public class ShopTableForm extends BeanEditor<ShopTable> {
	private FixedLengthTextField tfTableDescription;
	private IntegerTextField tfTableCapacity;
	private IntegerTextField tfTableNo;
	private FixedLengthTextField tfTableName;

	private JPanel statusPanel;
	private JRadioButton rbFree;
	private JRadioButton rbBooked;
	private JRadioButton rbDirty;
	private JRadioButton rbServing;
	private JRadioButton rbDisable;

	private SoftReference<ShopTableForm> instance;

	public ShopTableForm() {
		setLayout(new MigLayout("", "[][grow]", "[][][][][]"));

		JLabel lblName = new JLabel("Table no");
		add(lblName, "cell 0 0,alignx trailing,aligny center");

		tfTableNo = new IntegerTextField(6);
		add(tfTableNo, "cell 1 0,aligny top");

		tfTableNo.setEnabled(false);

		JLabel lblPhone = new JLabel("Name");
		add(lblPhone, "cell 0 1,alignx trailing");

		tfTableName = new FixedLengthTextField(20);
		tfTableName.setColumns(20);
		add(tfTableName, "cell 1 1,growx");

		JLabel lblAddress = new JLabel("Description");
		add(lblAddress, "cell 0 2,alignx trailing");

		tfTableDescription = new FixedLengthTextField(60);
		tfTableDescription.setColumns(20);
		add(tfTableDescription, "cell 1 2,growx");

		JLabel lblCitytown = new JLabel("Capacity");
		add(lblCitytown, "cell 0 3,alignx trailing");

		tfTableCapacity = new IntegerTextField();
		tfTableCapacity.setColumns(6);
		add(tfTableCapacity, "flowx,cell 1 3");

		statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(statusPanel, "cell 1 4,grow");
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		rbFree = new JRadioButton("Free");
		statusPanel.add(rbFree);

		rbServing = new JRadioButton("Serving");
		statusPanel.add(rbServing);

		rbBooked = new JRadioButton("Booked");
		statusPanel.add(rbBooked);

		rbDirty = new JRadioButton("Dirty");
		statusPanel.add(rbDirty);

		rbDisable = new JRadioButton("Disable");
		statusPanel.add(rbDisable);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rbFree);
		buttonGroup.add(rbServing);
		buttonGroup.add(rbBooked);
		buttonGroup.add(rbDirty);
		buttonGroup.add(rbDisable);

	}

	@Override
	public void createNew() {
		ShopTable bean2 = new ShopTable();
		bean2.setTemporary(true);
		bean2.setCapacity(4);

		try {

			int nextTableNumber = ShopTableDAO.getInstance().getNextTableNumber();

			bean2.setId(nextTableNumber);
			bean2.setName("" + nextTableNumber);
			bean2.setDescription("" + nextTableNumber);

		} catch (Exception e) {
			BOMessageDialog.showError(this, "Problem while creating new table.");
		}

		setBean(bean2);
	}

	@Override
	public void cancel() {
		//		ShopTable table = (ShopTable) getBean();
		//		if(table == null) {
		//			return;
		//		}
		//		
		//		if(table.isTemporary()) {
		//			ShopTableDAO.getInstance().delete(table);
		//		}
		//		
		//		setBean(null);
	}

	@Override
	public boolean delete() {
		try {
			ShopTable bean2 = getBean();
			if (bean2 == null)
				return false;

			int option = POSMessageDialog.showYesNoQuestionDialog(POSUtil.getBackOfficeWindow(), "Are you sure to delete selected table?", "Confirm");
			if (option != JOptionPane.YES_OPTION) {
				return false;
			}

			ShopTableDAO.getInstance().delete(bean2);

			return true;
		} catch (Exception e) {
			POSMessageDialog.showError(POSUtil.getBackOfficeWindow(), e.getMessage(), e);
		}

		return false;
	}

	public void setFieldsEditable(boolean editable) {
		tfTableName.setEditable(editable);
		tfTableDescription.setEditable(editable);
		tfTableCapacity.setEditable(editable);
	}

	@Override
	public void setFieldsEnable(boolean enable) {
		tfTableName.setEditable(enable);
		tfTableDescription.setEditable(enable);
		tfTableCapacity.setEditable(enable);

		rbFree.setEnabled(enable);
		rbServing.setEnabled(enable);
		rbBooked.setEnabled(enable);
		rbDirty.setEnabled(enable);
		rbDisable.setEnabled(enable);
	}

	public void setOnlyStatusEnable() {
		tfTableNo.setEditable(false);
		tfTableName.setEditable(false);
		tfTableDescription.setEditable(false);
		tfTableCapacity.setEditable(false);

		rbFree.setEnabled(true);
		rbServing.setEnabled(true);
		rbBooked.setEnabled(true);
		rbDirty.setEnabled(true);
		rbDisable.setEnabled(true);
	}

	@Override
	public boolean save() {
		try {
			if (!updateModel())
				return false;

			ShopTable table = (ShopTable) getBean();
			ShopTableDAO.getInstance().saveOrUpdate(table);
			table.setTemporary(false);

			updateView();
			return true;

		} catch (IllegalModelStateException e) {
		} catch (StaleObjectStateException e) {
			BOMessageDialog.showError(this, "It seems this table is modified by some other person or terminal. Save failed.");
		}

		return false;
	}

	@Override
	protected void updateView() {
		ShopTable table = (ShopTable) getBean();

		if (table == null) {
			return;
		}

		tfTableNo.setText(String.valueOf(table.getTableNumber()));
		tfTableName.setText(table.getName());
		tfTableDescription.setText(table.getDescription());
		tfTableCapacity.setText(String.valueOf(table.getCapacity()));

		rbFree.setSelected(true);
		rbServing.setSelected(table.isServing());
		rbBooked.setSelected(table.isBooked());
		rbDirty.setSelected(table.isDirty());
		rbDisable.setSelected(table.isDisable());

	}

	@Override
	protected boolean updateModel() throws IllegalModelStateException {
		ShopTable table = (ShopTable) getBean();

		if (table == null) {
			table = new ShopTable();
			setBean(table, false);
		}

		if (table.isTemporary()) {
			table.setId(null);
		}

		table.setName(tfTableName.getText());
		table.setDescription(tfTableDescription.getText());
		table.setCapacity(tfTableCapacity.getInteger());

		if (rbFree.isSelected()) {
			table.setFree(true);
			table.setServing(false);
			table.setBooked(false);
			table.setDirty(false);
			table.setDisable(false);
		}
		else if (rbServing.isSelected()) {
			table.setFree(false);
			table.setServing(true);
			table.setBooked(false);
			table.setDirty(false);
			table.setDisable(false);
		}
		else if (rbBooked.isSelected()) {
			table.setFree(false);
			table.setServing(false);
			table.setBooked(true);
			table.setDirty(false);
			table.setDisable(false);
		}
		else if (rbDirty.isSelected()) {
			table.setFree(false);
			table.setServing(false);
			table.setBooked(false);
			table.setDirty(true);
			table.setDisable(false);
		}
		else if (rbDisable.isSelected()) {
			table.setFree(false);
			table.setServing(false);
			table.setBooked(false);
			table.setDirty(false);
			table.setDisable(true);
		}

		return true;
	}

	@Override
	public String getDisplayText() {
		if (editMode) {
			return "Edit table";
		}
		return "Create table";
	}

	public ShopTableForm getInstance() {
		if (instance == null || instance.get() == null) {
			instance = new SoftReference<ShopTableForm>(new ShopTableForm());
		}

		return instance.get();
	}
}
