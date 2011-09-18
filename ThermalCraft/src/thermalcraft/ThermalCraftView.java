/*
 * ThermalCraftView.java
 */
package thermalcraft;

// <editor-fold defaultstate="collapsed" desc="Imports">
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import jxl.Cell;
import jxl.read.biff.BiffException;
import jxl.Workbook;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WriteException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;

// </editor-fold>

/**
 * The application's main frame.
 */
public class ThermalCraftView extends FrameView {

	// <editor-fold defaultstate="collapsed" desc="Constructor">
    public ThermalCraftView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        /*********************/
        /* BEGIN Custom Code */
        /*********************/

        // NetBeans just hides the window by default - kill it!
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // allocate memory for dynamic structures that change dynamically
        mySelectedBoxes = new ArrayList<Integer>();
        myParsedTextArray = new ArrayList<String>();
        myFileChooser = new JFileChooser();

        // set up the long text for the forms
        initLongText();

        // store the checkboxes in an array
        initCheckBoxes();

        // store the checboxes that need params
        initParamPanels();

        // store the checkboxes that need params
        initParamCheckBoxes();

        // store the replacable param fields
        initParamFields();

        // set up the text for the details pane
        initDetailsTitleText();

        // set up the Invoice Bill To fields
        initInvoiceBillToFields();

        // set up the Invoice Job Site fields
        initInvoiceJobSiteFields();

        // disable the forward tabs
        tabbedPanel.setEnabledAt(theDetailsIndex_c, false);
        tabbedPanel.setEnabledAt(theExportIndex_c, false);

		JFrame mainFrame = ThermalCraftApp.getApplication().getMainFrame();

		// create the error dialog
        myErrorDialog = new ThermalCraftDialog(mainFrame, true);
        myErrorDialog.setLocationRelativeTo(tabbedPanel);

		// create the number editor dialog
        myNumberEditor = new ThermalCraftNumberEditor(mainFrame, true);
        myNumberEditor.setLocationRelativeTo(tabbedPanel);

        /*********************/
        /* END   Custom Code */
        /*********************/
    }
// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Action Methods">
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = ThermalCraftApp.getApplication().getMainFrame();
            aboutBox = new ThermalCraftAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ThermalCraftApp.getApplication().show(aboutBox);
    }

	@Action
	public void showInvoiceNumberEditor() {
		// set the title
		myNumberEditor.setHeader("Invoice Number");

		// get the current value
		String val = readInvoiceNumberFromFile();
		BigDecimal bd = new BigDecimal(val);
		bd = bd.setScale(0, BigDecimal.ROUND_UP);
		myNumberEditor.setCurrentValue(bd.toString());

		// show the box
		myNumberEditor.pack();
		ThermalCraftApp.getApplication().show(myNumberEditor);
	}
	// </editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        tabbedPanel = new javax.swing.JTabbedPane();
        serviceJPanel = new javax.swing.JPanel();
        serviceMainText = new javax.swing.JTextField();
        choice1 = new javax.swing.JCheckBox();
        choice2 = new javax.swing.JCheckBox();
        choice3 = new javax.swing.JCheckBox();
        choice4 = new javax.swing.JCheckBox();
        choice5 = new javax.swing.JCheckBox();
        choice6 = new javax.swing.JCheckBox();
        choice7 = new javax.swing.JCheckBox();
        choice8 = new javax.swing.JCheckBox();
        choice9 = new javax.swing.JCheckBox();
        choice10 = new javax.swing.JCheckBox();
        choice11 = new javax.swing.JCheckBox();
        choice12 = new javax.swing.JCheckBox();
        choice13 = new javax.swing.JCheckBox();
        choice14 = new javax.swing.JCheckBox();
        choice15 = new javax.swing.JCheckBox();
        choice16 = new javax.swing.JCheckBox();
        choice17 = new javax.swing.JCheckBox();
        choice18 = new javax.swing.JCheckBox();
        choice19 = new javax.swing.JCheckBox();
        choice20 = new javax.swing.JCheckBox();
        choice21 = new javax.swing.JCheckBox();
        choice22 = new javax.swing.JCheckBox();
        choice23 = new javax.swing.JCheckBox();
        choice24 = new javax.swing.JCheckBox();
        serviceBottomPanel = new javax.swing.JPanel();
        serviceNextButton = new javax.swing.JButton();
        serviceSelectAllButton = new javax.swing.JButton();
        detailsDeselectAllButton = new javax.swing.JButton();
        detailsJPanel = new javax.swing.JPanel();
        detailsMainText = new javax.swing.JTextField();
        choice1DetailsPanel = new javax.swing.JPanel();
        choice1DetailsR = new javax.swing.JTextField();
        choice1DetailsTitle = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        choice1DetailsSqFt = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        choice1DetailsDepth = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        choice2DetailsPanel = new javax.swing.JPanel();
        choice2DetailsTitle = new javax.swing.JLabel();
        choice2DetailsSqFt = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        choice3DetailsPanel = new javax.swing.JPanel();
        choice3DetailsTitle = new javax.swing.JLabel();
        choice3DetailsCFM = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        choice7DetailsPanel = new javax.swing.JPanel();
        choice7DetailsTitle = new javax.swing.JLabel();
        choice7DetailsLinFt = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        choice8DetailsPanel = new javax.swing.JPanel();
        choice8DetailsTitle = new javax.swing.JLabel();
        choice8DetailsFlues = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        detailsBottomPanel = new javax.swing.JPanel();
        detailsPreviousButton = new javax.swing.JButton();
        detailsNextButton = new javax.swing.JButton();
        choice10DetailsPanel = new javax.swing.JPanel();
        choice10DetailsTitle = new javax.swing.JLabel();
        choice10DetailsSqFt = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        choice11DetailsPanel = new javax.swing.JPanel();
        choice11DetailsTitle = new javax.swing.JLabel();
        choice11DetailsSqFt = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        choice12DetailsPanel = new javax.swing.JPanel();
        choice12DetailsTitle = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        choice12DetailsSqFt = new javax.swing.JTextField();
        choice13DetailsPanel = new javax.swing.JPanel();
        choice13DetailsTitle = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        choice13DetailsSqFt = new javax.swing.JTextField();
        choice15DetailsPanel = new javax.swing.JPanel();
        choice15DetailsTitle = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        choice15DetailsSqFt = new javax.swing.JTextField();
        choice17DetailsPanel = new javax.swing.JPanel();
        choice17DetailsTitle = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        choice17DetailsSqFt = new javax.swing.JTextField();
        choice18DetailsPanel = new javax.swing.JPanel();
        choice18DetailsTitle = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        choice18DetailsSqFt = new javax.swing.JTextField();
        choice23DetailsPanel = new javax.swing.JPanel();
        choice23DetailsTitle = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        choice23DetailsChutes = new javax.swing.JTextField();
        choice24DetailsPanel = new javax.swing.JPanel();
        choice24DetailsTitle = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        choice24DetailsSqFt = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        noAddDetailsPanel = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        exportJPanel = new javax.swing.JPanel();
        cardMainPanel = new javax.swing.JPanel();
        cardMainTitleText = new javax.swing.JTextField();
        cardMainBottomPanel = new javax.swing.JPanel();
        cardMainExportPreviousButton = new javax.swing.JButton();
        cardMainInvoiceButton = new javax.swing.JButton();
        cardMainProposalButton = new javax.swing.JButton();
        cardInvoicePanel = new javax.swing.JPanel();
        cardInvoiceTitleText = new javax.swing.JTextField();
        cardInvoiceBottomPanel = new javax.swing.JPanel();
        cardInvoicePreviousButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        cardInvoiceBillName = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        cardInvoiceBillAddr1 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        cardInvoiceBillAddr2 = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        cardInvoiceBillAddr3 = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        cardInvoiceBillPhone = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        cardInvoiceJobName = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        cardInvoiceJobAddr1 = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        cardInvoiceJobAddr2 = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        cardInvoiceJobAddr3 = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        cardInvoiceJobPhone = new javax.swing.JTextField();
        cardInvoiceGenerateButton = new javax.swing.JButton();
        cardProposalPanel = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        cardProposalBottomPanel = new javax.swing.JPanel();
        cardProposalPreviousButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        optionMenu = new javax.swing.JMenu();
        invoiceNumMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jLabel11 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        mainPanel.setName("mainPanel"); // NOI18N

        tabbedPanel.setName("tabbedPanel"); // NOI18N

        serviceJPanel.setName("serviceJPanel"); // NOI18N

        serviceMainText.setEditable(false);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(thermalcraft.ThermalCraftApp.class).getContext().getResourceMap(ThermalCraftView.class);
        serviceMainText.setFont(resourceMap.getFont("serviceMainText.font")); // NOI18N
        serviceMainText.setText(resourceMap.getString("serviceMainText.text")); // NOI18N
        serviceMainText.setName("serviceMainText"); // NOI18N

        choice1.setFont(resourceMap.getFont("choice1.font")); // NOI18N
        choice1.setText(resourceMap.getString("choice1.text")); // NOI18N
        choice1.setName("choice1"); // NOI18N

        choice2.setFont(resourceMap.getFont("choice2.font")); // NOI18N
        choice2.setText(resourceMap.getString("choice2.text")); // NOI18N
        choice2.setName("choice2"); // NOI18N

        choice3.setFont(resourceMap.getFont("choice3.font")); // NOI18N
        choice3.setText(resourceMap.getString("choice3.text")); // NOI18N
        choice3.setName("choice3"); // NOI18N

        choice4.setFont(resourceMap.getFont("choice4.font")); // NOI18N
        choice4.setText(resourceMap.getString("choice4.text")); // NOI18N
        choice4.setName("choice4"); // NOI18N

        choice5.setFont(resourceMap.getFont("choice5.font")); // NOI18N
        choice5.setText(resourceMap.getString("choice5.text")); // NOI18N
        choice5.setName("choice5"); // NOI18N

        choice6.setFont(resourceMap.getFont("choice6.font")); // NOI18N
        choice6.setText(resourceMap.getString("choice6.text")); // NOI18N
        choice6.setName("choice6"); // NOI18N

        choice7.setFont(resourceMap.getFont("choice7.font")); // NOI18N
        choice7.setText(resourceMap.getString("choice7.text")); // NOI18N
        choice7.setName("choice7"); // NOI18N

        choice8.setFont(resourceMap.getFont("choice8.font")); // NOI18N
        choice8.setText(resourceMap.getString("choice8.text")); // NOI18N
        choice8.setName("choice8"); // NOI18N

        choice9.setFont(resourceMap.getFont("choice9.font")); // NOI18N
        choice9.setText(resourceMap.getString("choice9.text")); // NOI18N
        choice9.setName("choice9"); // NOI18N

        choice10.setFont(resourceMap.getFont("choice10.font")); // NOI18N
        choice10.setText(resourceMap.getString("choice10.text")); // NOI18N
        choice10.setName("choice10"); // NOI18N

        choice11.setFont(resourceMap.getFont("choice11.font")); // NOI18N
        choice11.setText(resourceMap.getString("choice11.text")); // NOI18N
        choice11.setName("choice11"); // NOI18N

        choice12.setFont(resourceMap.getFont("choice12.font")); // NOI18N
        choice12.setText(resourceMap.getString("choice12.text")); // NOI18N
        choice12.setName("choice12"); // NOI18N

        choice13.setFont(resourceMap.getFont("choice13.font")); // NOI18N
        choice13.setText(resourceMap.getString("choice13.text")); // NOI18N
        choice13.setName("choice13"); // NOI18N

        choice14.setFont(resourceMap.getFont("choice14.font")); // NOI18N
        choice14.setText(resourceMap.getString("choice14.text")); // NOI18N
        choice14.setName("choice14"); // NOI18N

        choice15.setFont(resourceMap.getFont("choice15.font")); // NOI18N
        choice15.setText(resourceMap.getString("choice15.text")); // NOI18N
        choice15.setName("choice15"); // NOI18N

        choice16.setFont(resourceMap.getFont("choice16.font")); // NOI18N
        choice16.setText(resourceMap.getString("choice16.text")); // NOI18N
        choice16.setName("choice16"); // NOI18N

        choice17.setFont(resourceMap.getFont("choice17.font")); // NOI18N
        choice17.setText(resourceMap.getString("choice17.text")); // NOI18N
        choice17.setName("choice17"); // NOI18N

        choice18.setFont(resourceMap.getFont("choice18.font")); // NOI18N
        choice18.setText(resourceMap.getString("choice18.text")); // NOI18N
        choice18.setName("choice18"); // NOI18N

        choice19.setFont(resourceMap.getFont("choice19.font")); // NOI18N
        choice19.setText(resourceMap.getString("choice19.text")); // NOI18N
        choice19.setName("choice19"); // NOI18N

        choice20.setFont(resourceMap.getFont("choice20.font")); // NOI18N
        choice20.setText(resourceMap.getString("choice20.text")); // NOI18N
        choice20.setName("choice20"); // NOI18N

        choice21.setFont(resourceMap.getFont("choice21.font")); // NOI18N
        choice21.setText(resourceMap.getString("choice21.text")); // NOI18N
        choice21.setName("choice21"); // NOI18N

        choice22.setFont(resourceMap.getFont("choice22.font")); // NOI18N
        choice22.setText(resourceMap.getString("choice22.text")); // NOI18N
        choice22.setName("choice22"); // NOI18N

        choice23.setFont(resourceMap.getFont("choice23.font")); // NOI18N
        choice23.setText(resourceMap.getString("choice23.text")); // NOI18N
        choice23.setName("choice23"); // NOI18N

        choice24.setFont(resourceMap.getFont("choice24.font")); // NOI18N
        choice24.setText(resourceMap.getString("choice24.text")); // NOI18N
        choice24.setName("choice24"); // NOI18N

        serviceBottomPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        serviceBottomPanel.setName("serviceBottomPanel"); // NOI18N

        serviceNextButton.setFont(resourceMap.getFont("serviceNextButton.font")); // NOI18N
        serviceNextButton.setText(resourceMap.getString("serviceNextButton.text")); // NOI18N
        serviceNextButton.setName("serviceNextButton"); // NOI18N
        serviceNextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                serviceNextButtonMouseClicked(evt);
            }
        });

        serviceSelectAllButton.setFont(resourceMap.getFont("serviceSelectAllButton.font")); // NOI18N
        serviceSelectAllButton.setText(resourceMap.getString("serviceSelectAllButton.text")); // NOI18N
        serviceSelectAllButton.setName("serviceSelectAllButton"); // NOI18N
        serviceSelectAllButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                serviceSelectAllButtonMouseClicked(evt);
            }
        });

        detailsDeselectAllButton.setFont(resourceMap.getFont("detailsDeselectAllButton.font")); // NOI18N
        detailsDeselectAllButton.setText(resourceMap.getString("detailsDeselectAllButton.text")); // NOI18N
        detailsDeselectAllButton.setName("detailsDeselectAllButton"); // NOI18N
        detailsDeselectAllButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                detailsDeselectAllButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout serviceBottomPanelLayout = new javax.swing.GroupLayout(serviceBottomPanel);
        serviceBottomPanel.setLayout(serviceBottomPanelLayout);
        serviceBottomPanelLayout.setHorizontalGroup(
            serviceBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, serviceBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(serviceSelectAllButton)
                .addGap(18, 18, 18)
                .addComponent(detailsDeselectAllButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 210, Short.MAX_VALUE)
                .addComponent(serviceNextButton)
                .addContainerGap())
        );
        serviceBottomPanelLayout.setVerticalGroup(
            serviceBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serviceBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(serviceBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceNextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(serviceSelectAllButton)
                    .addComponent(detailsDeselectAllButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout serviceJPanelLayout = new javax.swing.GroupLayout(serviceJPanel);
        serviceJPanel.setLayout(serviceJPanelLayout);
        serviceJPanelLayout.setHorizontalGroup(
            serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serviceJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(serviceMainText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(serviceJPanelLayout.createSequentialGroup()
                        .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(choice1)
                            .addComponent(choice2)
                            .addComponent(choice3)
                            .addComponent(choice4)
                            .addComponent(choice5)
                            .addComponent(choice6)
                            .addComponent(choice7)
                            .addComponent(choice8)
                            .addComponent(choice12)
                            .addComponent(choice10)
                            .addComponent(choice9)
                            .addComponent(choice11))
                        .addGap(48, 48, 48)
                        .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(choice14)
                            .addComponent(choice13)
                            .addComponent(choice15)
                            .addComponent(choice16)
                            .addComponent(choice17)
                            .addComponent(choice18)
                            .addComponent(choice19)
                            .addComponent(choice21)
                            .addComponent(choice20)
                            .addComponent(choice23)
                            .addComponent(choice24)
                            .addComponent(choice22, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(serviceBottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        serviceJPanelLayout.setVerticalGroup(
            serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serviceJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(serviceMainText, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice1)
                    .addComponent(choice13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice2)
                    .addComponent(choice14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice3)
                    .addComponent(choice15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice4)
                    .addComponent(choice16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice5)
                    .addComponent(choice17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice6)
                    .addComponent(choice18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice7)
                    .addComponent(choice19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(serviceJPanelLayout.createSequentialGroup()
                        .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(choice8)
                            .addComponent(choice20))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(choice9))
                    .addComponent(choice21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice10)
                    .addComponent(choice22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice11)
                    .addComponent(choice23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(serviceJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice12)
                    .addComponent(choice24))
                .addGap(167, 167, 167)
                .addComponent(serviceBottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPanel.addTab(resourceMap.getString("serviceJPanel.TabConstraints.tabTitle"), serviceJPanel); // NOI18N

        detailsJPanel.setName("detailsJPanel"); // NOI18N

        detailsMainText.setEditable(false);
        detailsMainText.setFont(resourceMap.getFont("detailsMainText.font")); // NOI18N
        detailsMainText.setText(resourceMap.getString("detailsMainText.text")); // NOI18N
        detailsMainText.setName("detailsMainText"); // NOI18N

        choice1DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice1DetailsPanel.setName("choice1DetailsPanel"); // NOI18N

        choice1DetailsR.setFont(resourceMap.getFont("choice1DetailsR.font")); // NOI18N
        choice1DetailsR.setText(resourceMap.getString("choice1DetailsR.text")); // NOI18N
        choice1DetailsR.setName("choice1DetailsR"); // NOI18N

        choice1DetailsTitle.setFont(resourceMap.getFont("choice1DetailsTitle.font")); // NOI18N
        choice1DetailsTitle.setText(resourceMap.getString("choice1DetailsTitle.text")); // NOI18N
        choice1DetailsTitle.setName("choice1DetailsTitle"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        choice1DetailsSqFt.setFont(resourceMap.getFont("choice1DetailsSqFt.font")); // NOI18N
        choice1DetailsSqFt.setText(resourceMap.getString("choice1DetailsSqFt.text")); // NOI18N
        choice1DetailsSqFt.setName("choice1DetailsSqFt"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        choice1DetailsDepth.setFont(resourceMap.getFont("choice1DetailsDepth.font")); // NOI18N
        choice1DetailsDepth.setText(resourceMap.getString("choice1DetailsDepth.text")); // NOI18N
        choice1DetailsDepth.setName("choice1DetailsDepth"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout choice1DetailsPanelLayout = new javax.swing.GroupLayout(choice1DetailsPanel);
        choice1DetailsPanel.setLayout(choice1DetailsPanelLayout);
        choice1DetailsPanelLayout.setHorizontalGroup(
            choice1DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice1DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice1DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice1DetailsDepth, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice1DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice1DetailsR, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice1DetailsPanelLayout.setVerticalGroup(
            choice1DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice1DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice1DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice1DetailsTitle)
                    .addComponent(choice1DetailsR)
                    .addComponent(jLabel4)
                    .addComponent(choice1DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(choice1DetailsDepth)
                    .addComponent(jLabel2))
                .addGap(3, 3, 3))
        );

        choice2DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice2DetailsPanel.setName("choice2DetailsPanel"); // NOI18N
        choice2DetailsPanel.setPreferredSize(new java.awt.Dimension(490, 26));

        choice2DetailsTitle.setFont(resourceMap.getFont("choice2DetailsTitle.font")); // NOI18N
        choice2DetailsTitle.setText(resourceMap.getString("choice2DetailsTitle.text")); // NOI18N
        choice2DetailsTitle.setName("choice2DetailsTitle"); // NOI18N

        choice2DetailsSqFt.setFont(resourceMap.getFont("choice2DetailsSqFt.font")); // NOI18N
        choice2DetailsSqFt.setText(resourceMap.getString("choice2DetailsSqFt.text")); // NOI18N
        choice2DetailsSqFt.setName("choice2DetailsSqFt"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout choice2DetailsPanelLayout = new javax.swing.GroupLayout(choice2DetailsPanel);
        choice2DetailsPanel.setLayout(choice2DetailsPanelLayout);
        choice2DetailsPanelLayout.setHorizontalGroup(
            choice2DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice2DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice2DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 287, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice2DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice2DetailsPanelLayout.setVerticalGroup(
            choice2DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice2DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(choice2DetailsTitle)
                .addComponent(choice2DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel5))
        );

        choice3DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice3DetailsPanel.setName("choice3DetailsPanel"); // NOI18N

        choice3DetailsTitle.setFont(resourceMap.getFont("choice3DetailsTitle.font")); // NOI18N
        choice3DetailsTitle.setText(resourceMap.getString("choice3DetailsTitle.text")); // NOI18N
        choice3DetailsTitle.setName("choice3DetailsTitle"); // NOI18N

        choice3DetailsCFM.setFont(resourceMap.getFont("choice3DetailsCFM.font")); // NOI18N
        choice3DetailsCFM.setText(resourceMap.getString("choice3DetailsCFM.text")); // NOI18N
        choice3DetailsCFM.setName("choice3DetailsCFM"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout choice3DetailsPanelLayout = new javax.swing.GroupLayout(choice3DetailsPanel);
        choice3DetailsPanel.setLayout(choice3DetailsPanelLayout);
        choice3DetailsPanelLayout.setHorizontalGroup(
            choice3DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice3DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice3DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 262, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice3DetailsCFM, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice3DetailsPanelLayout.setVerticalGroup(
            choice3DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice3DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(choice3DetailsTitle)
                .addComponent(choice3DetailsCFM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel1))
        );

        choice7DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice7DetailsPanel.setName("choice7DetailsPanel"); // NOI18N

        choice7DetailsTitle.setFont(resourceMap.getFont("choice7DetailsTitle.font")); // NOI18N
        choice7DetailsTitle.setText(resourceMap.getString("choice7DetailsTitle.text")); // NOI18N
        choice7DetailsTitle.setName("choice7DetailsTitle"); // NOI18N

        choice7DetailsLinFt.setFont(resourceMap.getFont("choice7DetailsLinFt.font")); // NOI18N
        choice7DetailsLinFt.setText(resourceMap.getString("choice7DetailsLinFt.text")); // NOI18N
        choice7DetailsLinFt.setName("choice7DetailsLinFt"); // NOI18N

        jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout choice7DetailsPanelLayout = new javax.swing.GroupLayout(choice7DetailsPanel);
        choice7DetailsPanel.setLayout(choice7DetailsPanelLayout);
        choice7DetailsPanelLayout.setHorizontalGroup(
            choice7DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice7DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice7DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 287, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice7DetailsLinFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice7DetailsPanelLayout.setVerticalGroup(
            choice7DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice7DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice7DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice7DetailsTitle)
                    .addComponent(choice7DetailsLinFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice8DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice8DetailsPanel.setName("choice8DetailsPanel"); // NOI18N

        choice8DetailsTitle.setFont(resourceMap.getFont("choice8DetailsTitle.font")); // NOI18N
        choice8DetailsTitle.setText(resourceMap.getString("choice8DetailsTitle.text")); // NOI18N
        choice8DetailsTitle.setName("choice8DetailsTitle"); // NOI18N

        choice8DetailsFlues.setFont(resourceMap.getFont("choice8DetailsFlues.font")); // NOI18N
        choice8DetailsFlues.setText(resourceMap.getString("choice8DetailsFlues.text")); // NOI18N
        choice8DetailsFlues.setName("choice8DetailsFlues"); // NOI18N

        jLabel8.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        javax.swing.GroupLayout choice8DetailsPanelLayout = new javax.swing.GroupLayout(choice8DetailsPanel);
        choice8DetailsPanel.setLayout(choice8DetailsPanelLayout);
        choice8DetailsPanelLayout.setHorizontalGroup(
            choice8DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice8DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice8DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 292, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice8DetailsFlues, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice8DetailsPanelLayout.setVerticalGroup(
            choice8DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice8DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice8DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice8DetailsTitle)
                    .addComponent(choice8DetailsFlues, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        detailsBottomPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        detailsBottomPanel.setName("detailsBottomPanel"); // NOI18N

        detailsPreviousButton.setFont(resourceMap.getFont("detailsPreviousButton.font")); // NOI18N
        detailsPreviousButton.setText(resourceMap.getString("detailsPreviousButton.text")); // NOI18N
        detailsPreviousButton.setName("detailsPreviousButton"); // NOI18N
        detailsPreviousButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                detailsPreviousButtonMouseClicked(evt);
            }
        });

        detailsNextButton.setFont(resourceMap.getFont("detailsNextButton.font")); // NOI18N
        detailsNextButton.setText(resourceMap.getString("detailsNextButton.text")); // NOI18N
        detailsNextButton.setName("detailsNextButton"); // NOI18N
        detailsNextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                detailsNextButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout detailsBottomPanelLayout = new javax.swing.GroupLayout(detailsBottomPanel);
        detailsBottomPanel.setLayout(detailsBottomPanelLayout);
        detailsBottomPanelLayout.setHorizontalGroup(
            detailsBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, detailsBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(detailsPreviousButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 274, Short.MAX_VALUE)
                .addComponent(detailsNextButton)
                .addContainerGap())
        );
        detailsBottomPanelLayout.setVerticalGroup(
            detailsBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(detailsPreviousButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(detailsNextButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        choice10DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice10DetailsPanel.setName("choice10DetailsPanel"); // NOI18N

        choice10DetailsTitle.setFont(resourceMap.getFont("choice10DetailsTitle.font")); // NOI18N
        choice10DetailsTitle.setText(resourceMap.getString("choice10DetailsTitle.text")); // NOI18N
        choice10DetailsTitle.setName("choice10DetailsTitle"); // NOI18N

        choice10DetailsSqFt.setFont(resourceMap.getFont("choice10DetailsSqFt.font")); // NOI18N
        choice10DetailsSqFt.setText(resourceMap.getString("choice10DetailsSqFt.text")); // NOI18N
        choice10DetailsSqFt.setName("choice10DetailsSqFt"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout choice10DetailsPanelLayout = new javax.swing.GroupLayout(choice10DetailsPanel);
        choice10DetailsPanel.setLayout(choice10DetailsPanelLayout);
        choice10DetailsPanelLayout.setHorizontalGroup(
            choice10DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice10DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice10DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice10DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice10DetailsPanelLayout.setVerticalGroup(
            choice10DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice10DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice10DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice10DetailsTitle)
                    .addComponent(choice10DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice11DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice11DetailsPanel.setName("choice11DetailsPanel"); // NOI18N

        choice11DetailsTitle.setFont(resourceMap.getFont("choice11DetailsTitle.font")); // NOI18N
        choice11DetailsTitle.setText(resourceMap.getString("choice11DetailsTitle.text")); // NOI18N
        choice11DetailsTitle.setName("choice11DetailsTitle"); // NOI18N

        choice11DetailsSqFt.setFont(resourceMap.getFont("choice11DetailsSqFt.font")); // NOI18N
        choice11DetailsSqFt.setName("choice11DetailsSqFt"); // NOI18N

        jLabel9.setFont(resourceMap.getFont("jLabel9.font")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        javax.swing.GroupLayout choice11DetailsPanelLayout = new javax.swing.GroupLayout(choice11DetailsPanel);
        choice11DetailsPanel.setLayout(choice11DetailsPanelLayout);
        choice11DetailsPanelLayout.setHorizontalGroup(
            choice11DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice11DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice11DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice11DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice11DetailsPanelLayout.setVerticalGroup(
            choice11DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice11DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice11DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice11DetailsTitle)
                    .addComponent(choice11DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice12DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice12DetailsPanel.setName("choice12DetailsPanel"); // NOI18N

        choice12DetailsTitle.setFont(resourceMap.getFont("choice12DetailsTitle.font")); // NOI18N
        choice12DetailsTitle.setText(resourceMap.getString("choice12DetailsTitle.text")); // NOI18N
        choice12DetailsTitle.setName("choice12DetailsTitle"); // NOI18N

        jLabel10.setFont(resourceMap.getFont("jLabel10.font")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        choice12DetailsSqFt.setFont(resourceMap.getFont("choice12DetailsSqFt.font")); // NOI18N
        choice12DetailsSqFt.setName("choice12DetailsSqFt"); // NOI18N

        javax.swing.GroupLayout choice12DetailsPanelLayout = new javax.swing.GroupLayout(choice12DetailsPanel);
        choice12DetailsPanel.setLayout(choice12DetailsPanelLayout);
        choice12DetailsPanelLayout.setHorizontalGroup(
            choice12DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice12DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice12DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice12DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice12DetailsPanelLayout.setVerticalGroup(
            choice12DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice12DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice12DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choice12DetailsTitle)
                    .addComponent(choice12DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice13DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice13DetailsPanel.setName("choice13DetailsPanel"); // NOI18N

        choice13DetailsTitle.setFont(resourceMap.getFont("choice13DetailsTitle.font")); // NOI18N
        choice13DetailsTitle.setText(resourceMap.getString("choice13DetailsTitle.text")); // NOI18N
        choice13DetailsTitle.setName("choice13DetailsTitle"); // NOI18N

        jLabel12.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        choice13DetailsSqFt.setFont(resourceMap.getFont("choice13DetailsSqFt.font")); // NOI18N
        choice13DetailsSqFt.setName("choice13DetailsSqFt"); // NOI18N

        javax.swing.GroupLayout choice13DetailsPanelLayout = new javax.swing.GroupLayout(choice13DetailsPanel);
        choice13DetailsPanel.setLayout(choice13DetailsPanelLayout);
        choice13DetailsPanelLayout.setHorizontalGroup(
            choice13DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice13DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice13DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice13DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice13DetailsPanelLayout.setVerticalGroup(
            choice13DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice13DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice13DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choice13DetailsTitle)
                    .addGroup(choice13DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(choice13DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice15DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice15DetailsPanel.setName("choice15DetailsPanel"); // NOI18N

        choice15DetailsTitle.setFont(resourceMap.getFont("choice15DetailsTitle.font")); // NOI18N
        choice15DetailsTitle.setText(resourceMap.getString("choice15DetailsTitle.text")); // NOI18N
        choice15DetailsTitle.setName("choice15DetailsTitle"); // NOI18N

        jLabel13.setFont(resourceMap.getFont("jLabel13.font")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        choice15DetailsSqFt.setFont(resourceMap.getFont("choice15DetailsSqFt.font")); // NOI18N
        choice15DetailsSqFt.setName("choice15DetailsSqFt"); // NOI18N

        javax.swing.GroupLayout choice15DetailsPanelLayout = new javax.swing.GroupLayout(choice15DetailsPanel);
        choice15DetailsPanel.setLayout(choice15DetailsPanelLayout);
        choice15DetailsPanelLayout.setHorizontalGroup(
            choice15DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice15DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice15DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice15DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice15DetailsPanelLayout.setVerticalGroup(
            choice15DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice15DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice15DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choice15DetailsTitle)
                    .addGroup(choice15DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(choice15DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel13)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice17DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice17DetailsPanel.setName("choice17DetailsPanel"); // NOI18N

        choice17DetailsTitle.setFont(resourceMap.getFont("choice17DetailsTitle.font")); // NOI18N
        choice17DetailsTitle.setText(resourceMap.getString("choice17DetailsTitle.text")); // NOI18N
        choice17DetailsTitle.setName("choice17DetailsTitle"); // NOI18N

        jLabel14.setFont(resourceMap.getFont("jLabel14.font")); // NOI18N
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        choice17DetailsSqFt.setFont(resourceMap.getFont("choice17DetailsSqFt.font")); // NOI18N
        choice17DetailsSqFt.setName("choice17DetailsSqFt"); // NOI18N

        javax.swing.GroupLayout choice17DetailsPanelLayout = new javax.swing.GroupLayout(choice17DetailsPanel);
        choice17DetailsPanel.setLayout(choice17DetailsPanelLayout);
        choice17DetailsPanelLayout.setHorizontalGroup(
            choice17DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice17DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice17DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice17DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice17DetailsPanelLayout.setVerticalGroup(
            choice17DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice17DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice17DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choice17DetailsTitle)
                    .addGroup(choice17DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(choice17DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel14)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice18DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice18DetailsPanel.setName("choice18DetailsPanel"); // NOI18N

        choice18DetailsTitle.setFont(resourceMap.getFont("choice18DetailsTitle.font")); // NOI18N
        choice18DetailsTitle.setText(resourceMap.getString("choice18DetailsTitle.text")); // NOI18N
        choice18DetailsTitle.setName("choice18DetailsTitle"); // NOI18N

        jLabel15.setFont(resourceMap.getFont("jLabel15.font")); // NOI18N
        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        choice18DetailsSqFt.setFont(resourceMap.getFont("choice18DetailsSqFt.font")); // NOI18N
        choice18DetailsSqFt.setName("choice18DetailsSqFt"); // NOI18N

        javax.swing.GroupLayout choice18DetailsPanelLayout = new javax.swing.GroupLayout(choice18DetailsPanel);
        choice18DetailsPanel.setLayout(choice18DetailsPanelLayout);
        choice18DetailsPanelLayout.setHorizontalGroup(
            choice18DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice18DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice18DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice18DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice18DetailsPanelLayout.setVerticalGroup(
            choice18DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice18DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice18DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choice18DetailsTitle)
                    .addGroup(choice18DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(choice18DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel15)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice23DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice23DetailsPanel.setName("choice23DetailsPanel"); // NOI18N

        choice23DetailsTitle.setFont(resourceMap.getFont("choice23DetailsTitle.font")); // NOI18N
        choice23DetailsTitle.setText(resourceMap.getString("choice23DetailsTitle.text")); // NOI18N
        choice23DetailsTitle.setName("choice23DetailsTitle"); // NOI18N

        jLabel16.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        choice23DetailsChutes.setFont(resourceMap.getFont("choice23DetailsChutes.font")); // NOI18N
        choice23DetailsChutes.setName("choice23DetailsChutes"); // NOI18N

        javax.swing.GroupLayout choice23DetailsPanelLayout = new javax.swing.GroupLayout(choice23DetailsPanel);
        choice23DetailsPanel.setLayout(choice23DetailsPanelLayout);
        choice23DetailsPanelLayout.setHorizontalGroup(
            choice23DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice23DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice23DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 274, Short.MAX_VALUE)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice23DetailsChutes, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice23DetailsPanelLayout.setVerticalGroup(
            choice23DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice23DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice23DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choice23DetailsTitle)
                    .addGroup(choice23DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(choice23DetailsChutes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel16)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        choice24DetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        choice24DetailsPanel.setName("choice24DetailsPanel"); // NOI18N

        choice24DetailsTitle.setFont(resourceMap.getFont("choice24DetailsTitle.font")); // NOI18N
        choice24DetailsTitle.setText(resourceMap.getString("choice24DetailsTitle.text")); // NOI18N
        choice24DetailsTitle.setName("choice24DetailsTitle"); // NOI18N

        jLabel17.setFont(resourceMap.getFont("jLabel17.font")); // NOI18N
        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        choice24DetailsSqFt.setFont(resourceMap.getFont("choice24DetailsSqFt.font")); // NOI18N
        choice24DetailsSqFt.setName("choice24DetailsSqFt"); // NOI18N

        javax.swing.GroupLayout choice24DetailsPanelLayout = new javax.swing.GroupLayout(choice24DetailsPanel);
        choice24DetailsPanel.setLayout(choice24DetailsPanelLayout);
        choice24DetailsPanelLayout.setHorizontalGroup(
            choice24DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice24DetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(choice24DetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choice24DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        choice24DetailsPanelLayout.setVerticalGroup(
            choice24DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choice24DetailsPanelLayout.createSequentialGroup()
                .addGroup(choice24DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choice24DetailsTitle)
                    .addGroup(choice24DetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(choice24DetailsSqFt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel17)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel15.setName("jPanel15"); // NOI18N

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 486, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );

        noAddDetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        noAddDetailsPanel.setName("noAddDetailsPanel"); // NOI18N

        jLabel19.setFont(resourceMap.getFont("jLabel19.font")); // NOI18N
        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        javax.swing.GroupLayout noAddDetailsPanelLayout = new javax.swing.GroupLayout(noAddDetailsPanel);
        noAddDetailsPanel.setLayout(noAddDetailsPanelLayout);
        noAddDetailsPanelLayout.setHorizontalGroup(
            noAddDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(noAddDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addContainerGap(54, Short.MAX_VALUE))
        );
        noAddDetailsPanelLayout.setVerticalGroup(
            noAddDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(noAddDetailsPanelLayout.createSequentialGroup()
                .addComponent(jLabel19)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout detailsJPanelLayout = new javax.swing.GroupLayout(detailsJPanel);
        detailsJPanel.setLayout(detailsJPanelLayout);
        detailsJPanelLayout.setHorizontalGroup(
            detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choice1DetailsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(detailsBottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(detailsMainText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(choice2DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                    .addComponent(choice3DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice7DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice8DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice10DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice11DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice12DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice13DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice15DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice17DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice18DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice23DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(choice24DetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(noAddDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        detailsJPanelLayout.setVerticalGroup(
            detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(detailsMainText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice1DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice2DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice3DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice7DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice8DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice10DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice11DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice12DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice13DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice15DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice17DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice18DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice23DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice24DetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noAddDetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(detailsBottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPanel.addTab(resourceMap.getString("detailsJPanel.TabConstraints.tabTitle"), detailsJPanel); // NOI18N

        exportJPanel.setName("exportJPanel"); // NOI18N
        exportJPanel.setLayout(new java.awt.CardLayout());

        cardMainPanel.setName("cardMainPanel"); // NOI18N

        cardMainTitleText.setEditable(false);
        cardMainTitleText.setFont(resourceMap.getFont("cardMainTitleText.font")); // NOI18N
        cardMainTitleText.setText(resourceMap.getString("cardMainTitleText.text")); // NOI18N
        cardMainTitleText.setName("cardMainTitleText"); // NOI18N

        cardMainBottomPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cardMainBottomPanel.setName("cardMainBottomPanel"); // NOI18N

        cardMainExportPreviousButton.setFont(resourceMap.getFont("cardMainExportPreviousButton.font")); // NOI18N
        cardMainExportPreviousButton.setText(resourceMap.getString("cardMainExportPreviousButton.text")); // NOI18N
        cardMainExportPreviousButton.setName("cardMainExportPreviousButton"); // NOI18N
        cardMainExportPreviousButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardMainExportPreviousButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout cardMainBottomPanelLayout = new javax.swing.GroupLayout(cardMainBottomPanel);
        cardMainBottomPanel.setLayout(cardMainBottomPanelLayout);
        cardMainBottomPanelLayout.setHorizontalGroup(
            cardMainBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMainBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardMainExportPreviousButton)
                .addContainerGap(366, Short.MAX_VALUE))
        );
        cardMainBottomPanelLayout.setVerticalGroup(
            cardMainBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardMainBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardMainExportPreviousButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        cardMainInvoiceButton.setFont(resourceMap.getFont("cardMainInvoiceButton.font")); // NOI18N
        cardMainInvoiceButton.setText(resourceMap.getString("cardMainInvoiceButton.text")); // NOI18N
        cardMainInvoiceButton.setName("cardMainInvoiceButton"); // NOI18N
        cardMainInvoiceButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardMainInvoiceButtonMouseClicked(evt);
            }
        });

        cardMainProposalButton.setFont(resourceMap.getFont("cardMainProposalButton.font")); // NOI18N
        cardMainProposalButton.setText(resourceMap.getString("cardMainProposalButton.text")); // NOI18N
        cardMainProposalButton.setName("cardMainProposalButton"); // NOI18N
        cardMainProposalButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardMainProposalButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout cardMainPanelLayout = new javax.swing.GroupLayout(cardMainPanel);
        cardMainPanel.setLayout(cardMainPanelLayout);
        cardMainPanelLayout.setHorizontalGroup(
            cardMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMainPanelLayout.createSequentialGroup()
                .addGroup(cardMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardMainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(cardMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cardMainBottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cardMainTitleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(cardMainPanelLayout.createSequentialGroup()
                        .addGap(196, 196, 196)
                        .addGroup(cardMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(cardMainInvoiceButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cardMainProposalButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        cardMainPanelLayout.setVerticalGroup(
            cardMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardMainTitleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(77, 77, 77)
                .addComponent(cardMainInvoiceButton)
                .addGap(54, 54, 54)
                .addComponent(cardMainProposalButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 301, Short.MAX_VALUE)
                .addComponent(cardMainBottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        exportJPanel.add(cardMainPanel, "card3");

        cardInvoicePanel.setName("cardInvoicePanel"); // NOI18N

        cardInvoiceTitleText.setEditable(false);
        cardInvoiceTitleText.setFont(resourceMap.getFont("cardInvoiceTitleText.font")); // NOI18N
        cardInvoiceTitleText.setText(resourceMap.getString("cardInvoiceTitleText.text")); // NOI18N
        cardInvoiceTitleText.setName("cardInvoiceTitleText"); // NOI18N

        cardInvoiceBottomPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cardInvoiceBottomPanel.setName("cardInvoiceBottomPanel"); // NOI18N

        cardInvoicePreviousButton.setFont(resourceMap.getFont("cardInvoicePreviousButton.font")); // NOI18N
        cardInvoicePreviousButton.setText(resourceMap.getString("cardInvoicePreviousButton.text")); // NOI18N
        cardInvoicePreviousButton.setName("cardInvoicePreviousButton"); // NOI18N
        cardInvoicePreviousButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardInvoicePreviousButtonMouseClicked(evt);
            }
        });

        jButton1.setFont(resourceMap.getFont("jButton1.font")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButton2.setFont(resourceMap.getFont("jButton2.font")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout cardInvoiceBottomPanelLayout = new javax.swing.GroupLayout(cardInvoiceBottomPanel);
        cardInvoiceBottomPanel.setLayout(cardInvoiceBottomPanelLayout);
        cardInvoiceBottomPanelLayout.setHorizontalGroup(
            cardInvoiceBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardInvoiceBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardInvoicePreviousButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        cardInvoiceBottomPanelLayout.setVerticalGroup(
            cardInvoiceBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardInvoiceBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardInvoiceBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cardInvoicePreviousButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        jTextField2.setEditable(false);
        jTextField2.setFont(resourceMap.getFont("jTextField2.font")); // NOI18N
        jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
        jTextField2.setName("jTextField2"); // NOI18N

        jLabel20.setFont(resourceMap.getFont("jLabel20.font")); // NOI18N
        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        cardInvoiceBillName.setText(resourceMap.getString("cardInvoiceBillName.text")); // NOI18N
        cardInvoiceBillName.setName("cardInvoiceBillName"); // NOI18N

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        cardInvoiceBillAddr1.setText(resourceMap.getString("cardInvoiceBillAddr1.text")); // NOI18N
        cardInvoiceBillAddr1.setName("cardInvoiceBillAddr1"); // NOI18N

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        cardInvoiceBillAddr2.setText(resourceMap.getString("cardInvoiceBillAddr2.text")); // NOI18N
        cardInvoiceBillAddr2.setName("cardInvoiceBillAddr2"); // NOI18N

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        cardInvoiceBillAddr3.setText(resourceMap.getString("cardInvoiceBillAddr3.text")); // NOI18N
        cardInvoiceBillAddr3.setName("cardInvoiceBillAddr3"); // NOI18N

        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        cardInvoiceBillPhone.setText(resourceMap.getString("cardInvoiceBillPhone.text")); // NOI18N
        cardInvoiceBillPhone.setName("cardInvoiceBillPhone"); // NOI18N

        jTextField3.setEditable(false);
        jTextField3.setFont(resourceMap.getFont("jTextField3.font")); // NOI18N
        jTextField3.setText(resourceMap.getString("jTextField3.text")); // NOI18N
        jTextField3.setName("jTextField3"); // NOI18N

        jLabel25.setText(resourceMap.getString("jLabel25.text")); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N

        cardInvoiceJobName.setText(resourceMap.getString("cardInvoiceJobName.text")); // NOI18N
        cardInvoiceJobName.setName("cardInvoiceJobName"); // NOI18N

        jLabel26.setText(resourceMap.getString("jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        cardInvoiceJobAddr1.setText(resourceMap.getString("cardInvoiceJobAddr1.text")); // NOI18N
        cardInvoiceJobAddr1.setName("cardInvoiceJobAddr1"); // NOI18N

        jLabel27.setText(resourceMap.getString("jLabel27.text")); // NOI18N
        jLabel27.setName("jLabel27"); // NOI18N

        cardInvoiceJobAddr2.setText(resourceMap.getString("cardInvoiceJobAddr2.text")); // NOI18N
        cardInvoiceJobAddr2.setName("cardInvoiceJobAddr2"); // NOI18N

        jLabel28.setText(resourceMap.getString("jLabel28.text")); // NOI18N
        jLabel28.setName("jLabel28"); // NOI18N

        cardInvoiceJobAddr3.setText(resourceMap.getString("cardInvoiceJobAddr3.text")); // NOI18N
        cardInvoiceJobAddr3.setName("cardInvoiceJobAddr3"); // NOI18N

        jLabel29.setText(resourceMap.getString("jLabel29.text")); // NOI18N
        jLabel29.setName("jLabel29"); // NOI18N

        cardInvoiceJobPhone.setText(resourceMap.getString("cardInvoiceJobPhone.text")); // NOI18N
        cardInvoiceJobPhone.setName("cardInvoiceJobPhone"); // NOI18N

        cardInvoiceGenerateButton.setFont(resourceMap.getFont("cardInvoiceGenerateButton.font")); // NOI18N
        cardInvoiceGenerateButton.setText(resourceMap.getString("cardInvoiceGenerateButton.text")); // NOI18N
        cardInvoiceGenerateButton.setName("cardInvoiceGenerateButton"); // NOI18N
        cardInvoiceGenerateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardInvoiceGenerateButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout cardInvoicePanelLayout = new javax.swing.GroupLayout(cardInvoicePanel);
        cardInvoicePanel.setLayout(cardInvoicePanelLayout);
        cardInvoicePanelLayout.setHorizontalGroup(
            cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                            .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cardInvoiceBottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                                    .addComponent(jLabel22)
                                    .addGap(18, 18, 18)
                                    .addComponent(cardInvoiceBillAddr2, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
                                .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                                    .addComponent(jLabel23)
                                    .addGap(18, 18, 18)
                                    .addComponent(cardInvoiceBillAddr3, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
                                .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                                    .addComponent(jLabel24)
                                    .addGap(18, 18, 18)
                                    .addComponent(cardInvoiceBillPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
                                .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                                    .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel21)
                                        .addComponent(jLabel20))
                                    .addGap(18, 18, 18)
                                    .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cardInvoiceBillAddr1, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                                        .addComponent(cardInvoiceBillName, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addContainerGap())
                        .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                            .addComponent(jLabel27)
                            .addGap(18, 18, 18)
                            .addComponent(cardInvoiceJobAddr2, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                            .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel26)
                                .addComponent(jLabel25))
                            .addGap(18, 18, 18)
                            .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(cardInvoiceJobAddr1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                                .addComponent(cardInvoiceJobName, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                                .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap())
                        .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                            .addComponent(jLabel28)
                            .addGap(18, 18, 18)
                            .addComponent(cardInvoiceJobAddr3, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                            .addComponent(jLabel29)
                            .addGap(18, 18, 18)
                            .addComponent(cardInvoiceJobPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardInvoicePanelLayout.createSequentialGroup()
                            .addComponent(cardInvoiceGenerateButton)
                            .addGap(160, 160, 160)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardInvoicePanelLayout.createSequentialGroup()
                        .addComponent(cardInvoiceTitleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(198, 198, 198))))
        );
        cardInvoicePanelLayout.setVerticalGroup(
            cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardInvoicePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardInvoiceTitleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(cardInvoiceBillName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel21)
                    .addComponent(cardInvoiceBillAddr1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(cardInvoiceBillAddr2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(cardInvoiceBillAddr3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(cardInvoiceBillPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cardInvoiceJobName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(cardInvoiceJobAddr1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(cardInvoiceJobAddr2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(cardInvoiceJobAddr3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(cardInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(cardInvoiceJobPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addComponent(cardInvoiceGenerateButton)
                .addGap(18, 18, 18)
                .addComponent(cardInvoiceBottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        exportJPanel.add(cardInvoicePanel, "card4");

        cardProposalPanel.setName("cardProposalPanel"); // NOI18N

        jTextField1.setEditable(false);
        jTextField1.setFont(resourceMap.getFont("jTextField1.font")); // NOI18N
        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        cardProposalBottomPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cardProposalBottomPanel.setName("cardProposalBottomPanel"); // NOI18N

        cardProposalPreviousButton.setFont(resourceMap.getFont("cardProposalPreviousButton.font")); // NOI18N
        cardProposalPreviousButton.setText(resourceMap.getString("cardProposalPreviousButton.text")); // NOI18N
        cardProposalPreviousButton.setName("cardProposalPreviousButton"); // NOI18N
        cardProposalPreviousButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardProposalPreviousButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout cardProposalBottomPanelLayout = new javax.swing.GroupLayout(cardProposalBottomPanel);
        cardProposalBottomPanel.setLayout(cardProposalBottomPanelLayout);
        cardProposalBottomPanelLayout.setHorizontalGroup(
            cardProposalBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProposalBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardProposalPreviousButton)
                .addContainerGap(366, Short.MAX_VALUE))
        );
        cardProposalBottomPanelLayout.setVerticalGroup(
            cardProposalBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardProposalBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardProposalPreviousButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout cardProposalPanelLayout = new javax.swing.GroupLayout(cardProposalPanel);
        cardProposalPanel.setLayout(cardProposalPanelLayout);
        cardProposalPanelLayout.setHorizontalGroup(
            cardProposalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProposalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardProposalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardProposalBottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        cardProposalPanelLayout.setVerticalGroup(
            cardProposalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProposalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 496, Short.MAX_VALUE)
                .addComponent(cardProposalBottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        exportJPanel.add(cardProposalPanel, "card2");

        tabbedPanel.addTab(resourceMap.getString("exportJPanel.TabConstraints.tabTitle"), exportJPanel); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPanel.getAccessibleContext().setAccessibleName(resourceMap.getString("jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(thermalcraft.ThermalCraftApp.class).getContext().getActionMap(ThermalCraftView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        optionMenu.setText(resourceMap.getString("optionMenu.text")); // NOI18N
        optionMenu.setName("optionMenu"); // NOI18N

        invoiceNumMenuItem.setAction(actionMap.get("showInvoiceNumberEditor")); // NOI18N
        invoiceNumMenuItem.setText(resourceMap.getString("invoiceNumMenuItem.text")); // NOI18N
        invoiceNumMenuItem.setName("invoiceNumMenuItem"); // NOI18N
        optionMenu.add(invoiceNumMenuItem);

        menuBar.add(optionMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 355, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

	// <editor-fold defaultstate="collapsed" desc="Event Methods">
	private void serviceNextButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serviceNextButtonMouseClicked
            // store the selected checkboxes
            mySelectedBoxes.clear();
            for (int i = 0; i < myCheckBoxList.size(); i++) {
                if (myCheckBoxList.get(i).isSelected()) {
                    mySelectedBoxes.add(i + 1);
                }
            }

            // verify that at least one option is selected
            if (0 == mySelectedBoxes.size()) {
				showMyDialog(theDefaultErrorTitle_c,
							 "Please select at least one checkbox");
                return;
            }

            // show the details panels if the checkboxes are checked
            boolean noAdditionalInfo = true;

            // hide all of the panels
            for (JPanel panel : myParamPanelMap.values()) {
                panel.setVisible(false);
            }

            // show the param panels that have been selected
            for (Integer i : myParamCheckBoxMap.keySet()) {
                if (myParamCheckBoxMap.get(i).isSelected()) {
                    myParamPanelMap.get(i).setVisible(true);
                    noAdditionalInfo = false;
                } // if they are hidden, clear the contents of the param text fields
                else {
                    for (JTextField field : myParamFieldsMap.get(i)) {
                        field.setText(null);
                    }
                }
            }

            // show the no additional details window if needed
            noAddDetailsPanel.setVisible(noAdditionalInfo);

            // move to the second tab
            tabbedPanel.setSelectedIndex(theDetailsIndex_c);

            // disable the first tab
            tabbedPanel.setEnabledAt(theServiceIndex_c, false);

            // enable the second tab
            tabbedPanel.setEnabledAt(theDetailsIndex_c, true);
	}//GEN-LAST:event_serviceNextButtonMouseClicked

	private void detailsPreviousButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_detailsPreviousButtonMouseClicked
            // move the the first tab
            tabbedPanel.setSelectedIndex(theServiceIndex_c);

            // disable the second tab
            tabbedPanel.setEnabledAt(theDetailsIndex_c, false);

            // enable the first tab
            tabbedPanel.setEnabledAt(theServiceIndex_c, true);
	}//GEN-LAST:event_detailsPreviousButtonMouseClicked

	private void detailsNextButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_detailsNextButtonMouseClicked
            // check that the shown panels have details filled in
            boolean showDialog = false;
            for (Integer num : mySelectedBoxes) {
                ArrayList<JTextField> fields = myParamFieldsMap.get(num);
                if (null != fields) {
                    for (JTextField field : fields) {
                        if (field.getText().equals("")) {
                            showDialog = true;
                        }
                    }
                }
            }

            // if there is a null value, show the dialog
            if (showDialog) {
				showMyDialog(theDefaultErrorTitle_c,
							 "Null values are not permitted");
                return;
            }

            // clear the array
            myParsedTextArray.clear();

            // populate the text to use with the documents
            for (Integer num : mySelectedBoxes) {
                replaceParam(num);
            }

            // move to the third tab
            tabbedPanel.setSelectedIndex(theExportIndex_c);

            // disable the second tab
            tabbedPanel.setEnabledAt(theDetailsIndex_c, false);

            // enable the third tab
            tabbedPanel.setEnabledAt(theExportIndex_c, true);
	}//GEN-LAST:event_detailsNextButtonMouseClicked

	private void serviceSelectAllButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serviceSelectAllButtonMouseClicked
            // check all of the boxes
            for (JCheckBox box : myCheckBoxList) {
                box.setSelected(true);
            }
	}//GEN-LAST:event_serviceSelectAllButtonMouseClicked

	private void detailsDeselectAllButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_detailsDeselectAllButtonMouseClicked
            // uncheck all of the boxes
            for (JCheckBox box : myCheckBoxList) {
                box.setSelected(false);
            }
	}//GEN-LAST:event_detailsDeselectAllButtonMouseClicked

	private void cardMainExportPreviousButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardMainExportPreviousButtonMouseClicked
            // move the the second tab
            tabbedPanel.setSelectedIndex(theDetailsIndex_c);

            // disable the third tab
            tabbedPanel.setEnabledAt(theExportIndex_c, false);

            // enable the second tab
            tabbedPanel.setEnabledAt(theDetailsIndex_c, true);
	}//GEN-LAST:event_cardMainExportPreviousButtonMouseClicked

	private void cardInvoicePreviousButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardInvoicePreviousButtonMouseClicked
            CardLayout cl = (CardLayout) (exportJPanel.getLayout());
            cl.previous(exportJPanel);
	}//GEN-LAST:event_cardInvoicePreviousButtonMouseClicked

	private void cardProposalPreviousButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardProposalPreviousButtonMouseClicked
            CardLayout cl = (CardLayout) (exportJPanel.getLayout());
            cl.first(exportJPanel);
	}//GEN-LAST:event_cardProposalPreviousButtonMouseClicked

	private void cardMainInvoiceButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardMainInvoiceButtonMouseClicked
            CardLayout cl = (CardLayout) (exportJPanel.getLayout());
            cl.next(exportJPanel);
	}//GEN-LAST:event_cardMainInvoiceButtonMouseClicked

	private void cardMainProposalButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardMainProposalButtonMouseClicked
            CardLayout cl = (CardLayout) (exportJPanel.getLayout());
            cl.last(exportJPanel);
	}//GEN-LAST:event_cardMainProposalButtonMouseClicked

	private void cardInvoiceGenerateButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardInvoiceGenerateButtonMouseClicked
		// verify that the addresses are filled in
		if(!verifyNonNullAddress(myInvoiceBillFields, "Please enter a valid \"Bill To\" address")) {
			return;
		}

		if(!verifyNonNullAddress(myInvoiceJobFields, "Please enter a valid \"Job Site\" address")) {
			return;
		}

		// set the file filters
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel 97-2003 Workbook (*.xls)", "xls");
		myFileChooser.setFileFilter(filter);
		int val = myFileChooser.showSaveDialog(exportJPanel);

		boolean successful = false;
		String newFile = null;
		try {
			if (JFileChooser.APPROVE_OPTION == val) {
				// the source file
				Workbook workbook = Workbook.getWorkbook(
						new File(theResourceDir_c + System.getProperty("file.separator") + theInvoiceTemplate_c));

				// get the new file
				newFile = ".." + System.getProperty("file.separator")
						+ myFileChooser.getCurrentDirectory()
						+ System.getProperty("file.separator")
						+ myFileChooser.getSelectedFile().getName()
						+ theExcelFileExtension_c;

				WritableWorkbook copy = Workbook.createWorkbook(new File(newFile), workbook);
				WritableSheet wSheet = copy.getSheet(0);
				workbook.close();

				// add the date
				writeDateTimeCell(wSheet, theInvoiceDateRow_c, theInvoiceHeaderCol_c, new Date());

				// add the invoice number
				Double invoiceNum = Double.parseDouble(readInvoiceNumberFromFile());
				writeNumberCell(wSheet, theInvoiceNumRow_c, theInvoiceHeaderCol_c, invoiceNum);

				// then write the incremented value back
				invoiceNum++;
				writeInvoiceNumberToFile(invoiceNum.toString());

				// add the PO

				// add the rep

				// Add the "Bill To" address
				writeAddress(wSheet, myInvoiceBillFields, theInvoiceAddrStartRow_c, theInvoiceAddrBillCol_c);

				// Add the "Job Site" address
				writeAddress(wSheet, myInvoiceJobFields, theInvoiceAddrStartRow_c, theInvoiceAddrJobCol_c);

				// add the text
				for (int i = 0; i < myParsedTextArray.size(); i++) {
					writeLabelCell(wSheet, theInvoiceStartRow_c + i, theInvoiceDescriptionCol_c, myParsedTextArray.get(i));
				}

				// then erase the unused cells
				for (int i = myParsedTextArray.size(); i < theInvoiceNumRows_c; i++) {
					writeLabelCell(wSheet, theInvoiceStartRow_c + i, theInvoiceDescriptionCol_c, null);
				}

				// save and close
				copy.write();
				copy.close();

				// everything completed normally
				successful = true;
			}
		} catch (IOException ieo) {
			System.err.println("IOException: " + ieo.getMessage() + "\n" + ieo.getStackTrace());
		} catch (BiffException be) {
			System.err.println("BiffException: " + be.getMessage() + "\n" + be.getStackTrace());
		} catch (WriteException we) {
			System.err.println("WriteException: " + we.getMessage() + "\n" + we.getStackTrace());
		}
		finally {
			if(successful) {
				showMyDialog("Invoice Generation", "Invoice was generated successfully!");
			}
			else {
				showMyDialog("Invoice Generation", "Invoice could not be generated!");
				File f = new File(newFile);
				if(!f.delete()) {
					showMyDialog("Invoice Generation", "Failed to delete: " + f.toString());
				}
			}
		}
	}//GEN-LAST:event_cardInvoiceGenerateButtonMouseClicked

        private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
            for(int i = 0; i < myInvoiceBillFields.size(); i++) {
                myInvoiceBillFields.get(i).setText(null);
                myInvoiceJobFields.get(i).setText(null);
            }
        }//GEN-LAST:event_jButton2MouseClicked

        private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
            for(int i = 0; i < myInvoiceBillFields.size(); i++) {
                myInvoiceJobFields.get(i).setText(myInvoiceBillFields.get(i).getText());
            }
        }//GEN-LAST:event_jButton1MouseClicked
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Custom Methods">
	    private void initLongText() {
        myLongTextMap = new HashMap<Integer, String>();

        myLongTextMap.put(1, "Bring entire attic to a total depth of <P>”, <P> Sq. Ft.  Add an additional R-<P>.");
        myLongTextMap.put(2, "Install R-13 batts into 2x4 framing (<P> Sq. Ft.).");
        myLongTextMap.put(3, "Final blower door test (CFM @-50 <P>).");
        myLongTextMap.put(4, "Build R-19 poly ISO box that allows stairs to close.");
        myLongTextMap.put(5, "Build R-19 poly ISO box on fan seal attic access panel.  Insulate attic hatch with insulation affixed with spray adhesive.");
        myLongTextMap.put(6, "Install dense pack cellulose into bottom of cantilever ensuring whole area is insulated.");
        myLongTextMap.put(7, "Hang R-19 vinyl faced batt along perimeter and secure with screws or ramset (<P> Lin. Ft.).");
        myLongTextMap.put(8, "Dam hot flue with rigid metal material leaving 3” to 6” clearance (<P> flues).");
        myLongTextMap.put(9, "Build a cardboard/foilray/wood dam around access.");
        myLongTextMap.put(10, "Dense pack walls through 0.5\" hole between bricks with cellulose to ensure no settling and to reach a min of R-13 (<P> Sq. Ft.).");
        myLongTextMap.put(11, "Dense pack walls through 2.5\" hole in drywall with cellulose to ensure no settling and to reach a min of R-13 (<P> Sq. Ft.).");
        myLongTextMap.put(12, "Install floor insulation securing with wire rods (<P> Sq. Ft.).");
        myLongTextMap.put(13, "Dense pack area above garage where living space is present with cellulose through 2.5\" holes (<P> Sq. Ft.).");
        myLongTextMap.put(14, "Install cover over recessed light and seal with foam or caulk.");
        myLongTextMap.put(15, "Wrap knee wall with R-13 vinyl faced batt (<P> Sq. Ft.).");
        myLongTextMap.put(16, "Seal all seams and joints on duct work with duct mastic.");
        myLongTextMap.put(17, "Install R-19 batts and secure with wire rods (<P> Sq. Ft.).");
        myLongTextMap.put(18, "Install R-30 batts and secure with wire rods (<P> Sq. Ft.).");
        myLongTextMap.put(19, "Install (R-13 or R19) batts over rim joist.");
        myLongTextMap.put(20, "Insulate attic hatch with 3 layers R-19 poly ISO foam board affixed with spray adhesive.");
        myLongTextMap.put(21, "Seal off chase where flue is present using metal and high temp foam or caulk.");
        myLongTextMap.put(22, "AI foam or caulk along sill plate and around rim joist.");
        myLongTextMap.put(23, "Install cardboard or Styrofoam baffles on rafters were soffit vent are present (<P> chutes).");
        myLongTextMap.put(24, "Install 6mil poly over ground and up the walls ensuring all seams and penetrations are sealed (<P> Sq. Ft.).");
    }

    private void initCheckBoxes() {
        myCheckBoxList = new ArrayList<JCheckBox>();

        myCheckBoxList.add(choice1);
        myCheckBoxList.add(choice2);
        myCheckBoxList.add(choice3);
        myCheckBoxList.add(choice4);
        myCheckBoxList.add(choice5);
        myCheckBoxList.add(choice6);
        myCheckBoxList.add(choice7);
        myCheckBoxList.add(choice8);
        myCheckBoxList.add(choice9);
        myCheckBoxList.add(choice10);
        myCheckBoxList.add(choice11);
        myCheckBoxList.add(choice12);
        myCheckBoxList.add(choice13);
        myCheckBoxList.add(choice14);
        myCheckBoxList.add(choice15);
        myCheckBoxList.add(choice16);
        myCheckBoxList.add(choice17);
        myCheckBoxList.add(choice18);
        myCheckBoxList.add(choice19);
        myCheckBoxList.add(choice20);
        myCheckBoxList.add(choice21);
        myCheckBoxList.add(choice22);
        myCheckBoxList.add(choice23);
        myCheckBoxList.add(choice24);
    }

    private void initParamPanels() {
        myParamPanelMap = new HashMap<Integer, JPanel>();

        myParamPanelMap.put(1, choice1DetailsPanel);
        myParamPanelMap.put(2, choice2DetailsPanel);
        myParamPanelMap.put(3, choice3DetailsPanel);
        myParamPanelMap.put(7, choice7DetailsPanel);
        myParamPanelMap.put(8, choice8DetailsPanel);
        myParamPanelMap.put(10, choice10DetailsPanel);
        myParamPanelMap.put(11, choice11DetailsPanel);
        myParamPanelMap.put(12, choice12DetailsPanel);
        myParamPanelMap.put(13, choice13DetailsPanel);
        myParamPanelMap.put(15, choice15DetailsPanel);
        myParamPanelMap.put(17, choice17DetailsPanel);
        myParamPanelMap.put(18, choice18DetailsPanel);
        myParamPanelMap.put(23, choice23DetailsPanel);
        myParamPanelMap.put(24, choice24DetailsPanel);
    }

    private void initParamCheckBoxes() {
        myParamCheckBoxMap = new HashMap<Integer, JCheckBox>();

        myParamCheckBoxMap.put(1, choice1);
        myParamCheckBoxMap.put(2, choice2);
        myParamCheckBoxMap.put(3, choice3);
        myParamCheckBoxMap.put(7, choice7);
        myParamCheckBoxMap.put(8, choice8);
        myParamCheckBoxMap.put(10, choice10);
        myParamCheckBoxMap.put(11, choice11);
        myParamCheckBoxMap.put(12, choice12);
        myParamCheckBoxMap.put(13, choice13);
        myParamCheckBoxMap.put(15, choice15);
        myParamCheckBoxMap.put(17, choice17);
        myParamCheckBoxMap.put(18, choice18);
        myParamCheckBoxMap.put(23, choice23);
        myParamCheckBoxMap.put(24, choice24);
    }

    private void initParamFields() {
        myParamFieldsMap = new HashMap<Integer, ArrayList<JTextField>>();

        // choice 1
        ArrayList<JTextField> one = new ArrayList<JTextField>();
        one.add(choice1DetailsDepth);
        one.add(choice1DetailsSqFt);
        one.add(choice1DetailsR);
        myParamFieldsMap.put(1, one);

        // choice 2
        ArrayList<JTextField> two = new ArrayList<JTextField>();
        two.add(choice2DetailsSqFt);
        myParamFieldsMap.put(2, two);

        // choice 3
        ArrayList<JTextField> three = new ArrayList<JTextField>();
        three.add(choice3DetailsCFM);
        myParamFieldsMap.put(3, three);

        // choice 7
        ArrayList<JTextField> seven = new ArrayList<JTextField>();
        seven.add(choice7DetailsLinFt);
        myParamFieldsMap.put(7, seven);

        // choice 8
        ArrayList<JTextField> eight = new ArrayList<JTextField>();
        eight.add(choice8DetailsFlues);
        myParamFieldsMap.put(8, eight);

        // choice 10
        ArrayList<JTextField> ten = new ArrayList<JTextField>();
        ten.add(choice10DetailsSqFt);
        myParamFieldsMap.put(10, ten);

        // choice 11
        ArrayList<JTextField> eleven = new ArrayList<JTextField>();
        eleven.add(choice11DetailsSqFt);
        myParamFieldsMap.put(11, eleven);

        // choice 12
        ArrayList<JTextField> twelve = new ArrayList<JTextField>();
        twelve.add(choice12DetailsSqFt);
        myParamFieldsMap.put(12, twelve);

        // choice 13
        ArrayList<JTextField> thirteen = new ArrayList<JTextField>();
        thirteen.add(choice13DetailsSqFt);
        myParamFieldsMap.put(13, thirteen);

        // choice 15
        ArrayList<JTextField> fifteen = new ArrayList<JTextField>();
        fifteen.add(choice15DetailsSqFt);
        myParamFieldsMap.put(15, fifteen);

        // choice 17
        ArrayList<JTextField> seventeen = new ArrayList<JTextField>();
        seventeen.add(choice17DetailsSqFt);
        myParamFieldsMap.put(17, seventeen);

        // choice 18
        ArrayList<JTextField> eighteen = new ArrayList<JTextField>();
        eighteen.add(choice18DetailsSqFt);
        myParamFieldsMap.put(18, eighteen);

        // choice 23
        ArrayList<JTextField> twentythree = new ArrayList<JTextField>();
        twentythree.add(choice23DetailsChutes);
        myParamFieldsMap.put(23, twentythree);

        // choice 24
        ArrayList<JTextField> twentyfour = new ArrayList<JTextField>();
        twentyfour.add(choice24DetailsSqFt);
        myParamFieldsMap.put(24, twentyfour);
    }

    private void initDetailsTitleText() {
        // use the resource map defined for the checkbox text
        org.jdesktop.application.ResourceMap resourceMap =
                org.jdesktop.application.Application.getInstance(thermalcraft.ThermalCraftApp.class).getContext().getResourceMap(ThermalCraftView.class);

        // set the text
        choice1DetailsTitle.setText(resourceMap.getString("choice1.text"));
        choice2DetailsTitle.setText(resourceMap.getString("choice2.text"));
        choice3DetailsTitle.setText(resourceMap.getString("choice3.text"));
        choice7DetailsTitle.setText(resourceMap.getString("choice7.text"));
        choice8DetailsTitle.setText(resourceMap.getString("choice8.text"));
        choice10DetailsTitle.setText(resourceMap.getString("choice10.text"));
        choice11DetailsTitle.setText(resourceMap.getString("choice11.text"));
        choice12DetailsTitle.setText(resourceMap.getString("choice12.text"));
        choice13DetailsTitle.setText(resourceMap.getString("choice13.text"));
        choice15DetailsTitle.setText(resourceMap.getString("choice15.text"));
        choice17DetailsTitle.setText(resourceMap.getString("choice17.text"));
        choice18DetailsTitle.setText(resourceMap.getString("choice18.text"));
        choice23DetailsTitle.setText(resourceMap.getString("choice23.text"));
        choice24DetailsTitle.setText(resourceMap.getString("choice24.text"));
    }

    private void initInvoiceBillToFields() {
        myInvoiceBillFields = new ArrayList<JTextField>();

        myInvoiceBillFields.add(cardInvoiceBillName);
        myInvoiceBillFields.add(cardInvoiceBillAddr1);
        myInvoiceBillFields.add(cardInvoiceBillAddr2);
        myInvoiceBillFields.add(cardInvoiceBillAddr3);
        myInvoiceBillFields.add(cardInvoiceBillPhone);
    }

    private void initInvoiceJobSiteFields() {
        myInvoiceJobFields = new ArrayList<JTextField>();

        myInvoiceJobFields.add(cardInvoiceJobName);
        myInvoiceJobFields.add(cardInvoiceJobAddr1);
        myInvoiceJobFields.add(cardInvoiceJobAddr2);
        myInvoiceJobFields.add(cardInvoiceJobAddr3);
        myInvoiceJobFields.add(cardInvoiceJobPhone);
    }

    public JTabbedPane getTabbedFrame() {
        return tabbedPanel;
    }

	private void replaceParam(Integer num) {
        // these fields have one replacable parameter
        if (myParamFieldsMap.containsKey(num)) {
            String str = myLongTextMap.get(num);

            // replace the parameters in order
            for (JTextField field : myParamFieldsMap.get(num)) {
                str = str.replaceFirst(theDelimiter, field.getText());
            }
            myParsedTextArray.add(str);
        } // these fields have no replacable parameters
        else {
            myParsedTextArray.add(myLongTextMap.get(num));
        }
    }

	private boolean verifyNonNullAddress(ArrayList<JTextField> fields, String errorString) {
		boolean allBlank = true;
		for(JTextField field : fields) {
			if(!field.getText().equals("")) {
				allBlank = false;
			}
		}

		if(allBlank) {
			showMyDialog(theDefaultErrorTitle_c, errorString);
		}

		return !allBlank;
	}

	private void showMyDialog(String title, String text) {
		myErrorDialog.setTitle(title);
		myErrorDialog.setLabelText(text);
		ThermalCraftApp.getApplication().show(myErrorDialog);
	}

	private void writeAddress(WritableSheet wSheet, ArrayList<JTextField> fields, int row, int col) {
		int i = 0, nullFields = 0;

		// write the non-null fields to the sheet
		for(JTextField field : fields) {
			if(!field.getText().equals("")) {
				writeLabelCell(wSheet, row + i, col, theTextOffset + field.getText());
				i++;
			}
			else {
				nullFields++;
			}
		}

		// then set the null-valued cells blank
		for(i = fields.size() - nullFields; i < fields.size(); i++) {
			writeLabelCell(wSheet, row + i, col, null);
		}
	}

	private void writeLabelCell(WritableSheet wSheet, int row, int col, String text) {
        Cell item = wSheet.getWritableCell(col, row);
        Label itemLabel = (Label) item;
        itemLabel.setString(text);
    }

	private void writeDateTimeCell(WritableSheet wSheet, int row, int col, Date date) {
		Cell item = wSheet.getWritableCell(col, row);
        DateTime itemDateTime = (DateTime) item;
        itemDateTime.setDate(date);
	}

	private void writeNumberCell(WritableSheet wSheet, int row, int col, Double num) {
        Cell item = wSheet.getWritableCell(col, row);
        Number numLabel = (Number) item;
        numLabel.setValue(num);
    }

	private String readStringFromFile(String filename) {
		String line = null;
		try {
			BufferedReader reader =
				new BufferedReader(
					new FileReader(filename));

			line = reader.readLine();
			reader.close();
		} catch (IOException iox) {
			showMyDialog("File I/O Error", "Failed to open file: " + filename);
		}

		return line;
	}

	private void writeStringToFile(String filename, String text, boolean truncate) {
		try {
			if(truncate) {
				RandomAccessFile randFile =
					new RandomAccessFile(new File(filename), "rw");
				randFile.setLength(0);
				randFile.close();
			}
			BufferedWriter writer =
				new BufferedWriter(
					new FileWriter(filename));

			writer.write(text, 0, text.length());
			writer.close();
		} catch (IOException iox) {
			showMyDialog("File I/O Error", "Failed to write to file: " + filename);
		}
	}

	private String readInvoiceNumberFromFile() {
		String filename = ".." + System.getProperty("file.separator")
						+ theResourceDir_c
						+ System.getProperty("file.separator")
						+ theInvoiceTextFile_c;
		return readStringFromFile(filename);
	}

	private void writeInvoiceNumberToFile(String invoiceNum) {
		String filename = ".." + System.getProperty("file.separator")
						+ theResourceDir_c
						+ System.getProperty("file.separator")
						+ theInvoiceTextFile_c;
		writeStringToFile(filename, invoiceNum.toString(), true);
	}

	public void setNewInvoiceNumber(String val) {
		writeInvoiceNumberToFile(val);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="NetBeans Members">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cardInvoiceBillAddr1;
    private javax.swing.JTextField cardInvoiceBillAddr2;
    private javax.swing.JTextField cardInvoiceBillAddr3;
    private javax.swing.JTextField cardInvoiceBillName;
    private javax.swing.JTextField cardInvoiceBillPhone;
    private javax.swing.JPanel cardInvoiceBottomPanel;
    private javax.swing.JButton cardInvoiceGenerateButton;
    private javax.swing.JTextField cardInvoiceJobAddr1;
    private javax.swing.JTextField cardInvoiceJobAddr2;
    private javax.swing.JTextField cardInvoiceJobAddr3;
    private javax.swing.JTextField cardInvoiceJobName;
    private javax.swing.JTextField cardInvoiceJobPhone;
    private javax.swing.JPanel cardInvoicePanel;
    private javax.swing.JButton cardInvoicePreviousButton;
    private javax.swing.JTextField cardInvoiceTitleText;
    private javax.swing.JPanel cardMainBottomPanel;
    private javax.swing.JButton cardMainExportPreviousButton;
    private javax.swing.JButton cardMainInvoiceButton;
    private javax.swing.JPanel cardMainPanel;
    private javax.swing.JButton cardMainProposalButton;
    private javax.swing.JTextField cardMainTitleText;
    private javax.swing.JPanel cardProposalBottomPanel;
    private javax.swing.JPanel cardProposalPanel;
    private javax.swing.JButton cardProposalPreviousButton;
    private javax.swing.JCheckBox choice1;
    private javax.swing.JCheckBox choice10;
    private javax.swing.JPanel choice10DetailsPanel;
    private javax.swing.JTextField choice10DetailsSqFt;
    private javax.swing.JLabel choice10DetailsTitle;
    private javax.swing.JCheckBox choice11;
    private javax.swing.JPanel choice11DetailsPanel;
    private javax.swing.JTextField choice11DetailsSqFt;
    private javax.swing.JLabel choice11DetailsTitle;
    private javax.swing.JCheckBox choice12;
    private javax.swing.JPanel choice12DetailsPanel;
    private javax.swing.JTextField choice12DetailsSqFt;
    private javax.swing.JLabel choice12DetailsTitle;
    private javax.swing.JCheckBox choice13;
    private javax.swing.JPanel choice13DetailsPanel;
    private javax.swing.JTextField choice13DetailsSqFt;
    private javax.swing.JLabel choice13DetailsTitle;
    private javax.swing.JCheckBox choice14;
    private javax.swing.JCheckBox choice15;
    private javax.swing.JPanel choice15DetailsPanel;
    private javax.swing.JTextField choice15DetailsSqFt;
    private javax.swing.JLabel choice15DetailsTitle;
    private javax.swing.JCheckBox choice16;
    private javax.swing.JCheckBox choice17;
    private javax.swing.JPanel choice17DetailsPanel;
    private javax.swing.JTextField choice17DetailsSqFt;
    private javax.swing.JLabel choice17DetailsTitle;
    private javax.swing.JCheckBox choice18;
    private javax.swing.JPanel choice18DetailsPanel;
    private javax.swing.JTextField choice18DetailsSqFt;
    private javax.swing.JLabel choice18DetailsTitle;
    private javax.swing.JCheckBox choice19;
    private javax.swing.JTextField choice1DetailsDepth;
    private javax.swing.JPanel choice1DetailsPanel;
    private javax.swing.JTextField choice1DetailsR;
    private javax.swing.JTextField choice1DetailsSqFt;
    private javax.swing.JLabel choice1DetailsTitle;
    private javax.swing.JCheckBox choice2;
    private javax.swing.JCheckBox choice20;
    private javax.swing.JCheckBox choice21;
    private javax.swing.JCheckBox choice22;
    private javax.swing.JCheckBox choice23;
    private javax.swing.JTextField choice23DetailsChutes;
    private javax.swing.JPanel choice23DetailsPanel;
    private javax.swing.JLabel choice23DetailsTitle;
    private javax.swing.JCheckBox choice24;
    private javax.swing.JPanel choice24DetailsPanel;
    private javax.swing.JTextField choice24DetailsSqFt;
    private javax.swing.JLabel choice24DetailsTitle;
    private javax.swing.JPanel choice2DetailsPanel;
    private javax.swing.JTextField choice2DetailsSqFt;
    private javax.swing.JLabel choice2DetailsTitle;
    private javax.swing.JCheckBox choice3;
    private javax.swing.JTextField choice3DetailsCFM;
    private javax.swing.JPanel choice3DetailsPanel;
    private javax.swing.JLabel choice3DetailsTitle;
    private javax.swing.JCheckBox choice4;
    private javax.swing.JCheckBox choice5;
    private javax.swing.JCheckBox choice6;
    private javax.swing.JCheckBox choice7;
    private javax.swing.JTextField choice7DetailsLinFt;
    private javax.swing.JPanel choice7DetailsPanel;
    private javax.swing.JLabel choice7DetailsTitle;
    private javax.swing.JCheckBox choice8;
    private javax.swing.JTextField choice8DetailsFlues;
    private javax.swing.JPanel choice8DetailsPanel;
    private javax.swing.JLabel choice8DetailsTitle;
    private javax.swing.JCheckBox choice9;
    private javax.swing.JPanel detailsBottomPanel;
    private javax.swing.JButton detailsDeselectAllButton;
    private javax.swing.JPanel detailsJPanel;
    private javax.swing.JTextField detailsMainText;
    private javax.swing.JButton detailsNextButton;
    private javax.swing.JButton detailsPreviousButton;
    private javax.swing.JPanel exportJPanel;
    private javax.swing.JMenuItem invoiceNumMenuItem;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel noAddDetailsPanel;
    private javax.swing.JMenu optionMenu;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel serviceBottomPanel;
    private javax.swing.JPanel serviceJPanel;
    private javax.swing.JTextField serviceMainText;
    private javax.swing.JButton serviceNextButton;
    private javax.swing.JButton serviceSelectAllButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTabbedPane tabbedPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Custom Members">
    /* BEGIN Custom Variables */

	// array of selected checkboxes
    private ArrayList<Integer> mySelectedBoxes;

    // array of all checkboxes
    private ArrayList<JCheckBox> myCheckBoxList;

    // array of parsed text
    private ArrayList<String> myParsedTextArray;

    // array of Invoice Bill To fields
    private ArrayList<JTextField> myInvoiceBillFields;

    // array of Invoice Job Site fields
    private ArrayList<JTextField> myInvoiceJobFields;

    // map of the parametrized text strings
    private HashMap<Integer, String> myLongTextMap;

    // map of panels that need details
    private HashMap<Integer, JPanel> myParamPanelMap;

    // map of checkboxes that need details
    private HashMap<Integer, JCheckBox> myParamCheckBoxMap;

    // multimap for the replacable param source
    private HashMap<Integer, ArrayList<JTextField>> myParamFieldsMap;

	// file chooser
    private JFileChooser myFileChooser;

	// error box
    private ThermalCraftDialog myErrorDialog;

	// number editor dialog
	private ThermalCraftNumberEditor myNumberEditor;

    // tab panel indexes
    private static final int theServiceIndex_c = 0;
    private static final int theDetailsIndex_c = 1;
    private static final int theExportIndex_c = 2;

    // invoice constants
    private static final int theInvoiceStartRow_c = 15;
    private static final int theInvoiceNumRows_c = 24;
    private static final int theInvoiceItemCol_c = 0;
    private static final int theInvoiceQuantityCol_c = 1;
    private static final int theInvoiceDescriptionCol_c = 2;
    private static final int theInvoiceRateCol_c = 7;
    private static final int theInvoiceAmountCol_c = 8;
    private static final int theInvoiceAddrStartRow_c = 8;
    private static final int theInvoiceAddrBillCol_c = 0;
    private static final int theInvoiceAddrJobCol_c = 5;
	private static final int theInvoiceHeaderCol_c = 8;
	private static final int theInvoiceDateRow_c = 2;
	private static final int theInvoiceNumRow_c = 3;
	private static final int theInvoicePORow_c = 4;
	private static final int theInvoiceRepRow_c = 5;

    // string contstants
    private static final String theDelimiter = "<P>";
    private static final String theTextOffset = "        ";

	// error dialog
	private static final String theDefaultErrorTitle_c = "Correct errors before continuing";

	// files and directories
	private static final String theResourceDir_c = "resources";
	private static final String theInvoiceTemplate_c = "Invoice.xls";
	private static final String theProposalTemplate_c = "Proposal.xls";
	private static final String theExcelFileExtension_c = ".xls";
	private static final String theInvoiceTextFile_c = "invoice.txt";
    /* END   Custom Variables */
	// </editor-fold>
}
