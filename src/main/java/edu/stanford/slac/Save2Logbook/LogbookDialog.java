package edu.stanford.slac.Save2Logbook;

/**
 * @author Koosh
 */


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

/**
 * The LogbookDialog allows the user to use a GUI to add files, set the author, comments, 
 * severity etc, and displays the files that are being added 
 * 
 */

public class LogbookDialog extends JPanel implements ActionListener, ListSelectionListener
{
	SpringLayout layout = new SpringLayout();
	private static JFrame frame = new JFrame();	
	private Calendar cal = GregorianCalendar.getInstance();
	private static LogbookEntry save = new LogbookEntry();
	JButton addFileButtonOne;
	JFileChooser fc;
	JTextArea t;
	Container contentPane = frame.getContentPane();
	JPanel topPanel = new JPanel();
	JList list;
	JScrollPane listScrollPane;
	JSplitPane splitPane;
	JScrollPane allScrollPane;
	Vector<String> fileNames = new Vector<String>();
	Vector<String> filePaths = new Vector<String>();
	MyListener authorListener = new MyListener();
	MyListener textListener = new MyListener();
	MyListener titleListener = new MyListener();
	JLabel picture, all;
	Vector<BufferedImage> biVector = new Vector<BufferedImage>();
	WriteLogbookXML xml = new WriteLogbookXML();

	
	//constructor without a frame
	public LogbookDialog(final LogbookEntry save) 
	{
        super(new BorderLayout());	   
		this.save = save;
		runG();
	}
	
	//constructor with a frame
	public LogbookDialog(final LogbookEntry save, JFrame f)
	{
		super(new BorderLayout());	   
		this.save = save;		
		frame.setLocation(f.getLocation().x, f.getLocation().y);
		runG();
	}
	private void runG()
	{
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));				
		
		//sets label names
		JLabel severityLabel = new JLabel("Severity:");
		JLabel locationLabel = new JLabel("Location:");
		JLabel keywordLabel = new JLabel("Keyword:");
		JLabel timeLabel = new JLabel("Time:");
		JLabel dateLabel = new JLabel("Date:");
		JLabel authorLabel = new JLabel("Author:");
		JLabel titleLabel = new JLabel("Title:");
		JLabel textLabel = new JLabel("Text:");		
	
		//if the logbook has text already set in it, it is retained in the GUI
		//constructors for the various fields
		JTextField timeField = new JTextField(5);
		timeField.setEditable(false);
		SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");		
		String time = dateFormatter.format(cal.getTime());	
		timeField.setText(time);
		
		JTextField dateField = new JTextField(7);
		SimpleDateFormat timeFormatter = new SimpleDateFormat("dd/M/yyyy");
		String date = timeFormatter.format(cal.getTime());		
		dateField.setEditable(false);
		dateField.setText(date);		
		
		JTextArea authorArea = new JTextArea();
		authorArea.getDocument().addDocumentListener(authorListener);		
	    authorArea.getDocument().putProperty("name", "author Area");
	    authorArea.setEditable(true);
		JScrollPane authorScrollPane = new JScrollPane(authorArea);
		authorArea.setText(save.getAuthor());
        authorScrollPane.setPreferredSize(new Dimension(550, 25));
		
		JTextArea titleArea = new JTextArea();
		titleArea.getDocument().addDocumentListener(titleListener);		
	    titleArea.getDocument().putProperty("name", "title Area");
	    titleArea.setEditable(true);
		JScrollPane titleScrollPane = new JScrollPane(titleArea);
		titleArea.setText(save.getTitle());
        titleScrollPane.setPreferredSize(new Dimension(550, 25));
        
		
		JTextArea textArea = new JTextArea();
		textArea.getDocument().addDocumentListener(textListener);		
	    textArea.getDocument().putProperty("name", "Text Area");
	    textArea.setEditable(true);
		JScrollPane textScrollPane = new JScrollPane(textArea);
		textArea.setText(save.getComments());
        textScrollPane.setPreferredSize(new Dimension(550, 200));
        
        //checks to see which severity value, if any is already set
        int j = 0;
        for(int i = 0; i < SeverityEnum.values().length; i++)
        {
        	if (SeverityEnum.values()[i].toString() == save.getSeverity())
        	{
        		j = i;
        		break;
        	}
        }        
        JComboBox severity = new JComboBox(SeverityEnum.values());
		severity.setSelectedIndex(j);
		severity.setVisible(true);	
		severity.addActionListener(new ActionListener()
		{
			//checks for a change in severity
			public void actionPerformed(ActionEvent event)
			{				
				JComboBox cb = (JComboBox)event.getSource();
				SeverityEnum e = (SeverityEnum)cb.getSelectedItem();
				save.setSeverity(e);
			}
		});
		
		//checks to see which location value, if any is already set
		j = 0;
		for(int i = 0; i < LocationEnum.values().length; i++)
	        {
	        	if (LocationEnum.values()[i].toString() == save.getLocation())
	        	{
	        		j = i;
	        		break;
	        	}
	        }     
		JComboBox location = new JComboBox(LocationEnum.values());
		location.setSelectedIndex(j);
		location.setVisible(true);
		location.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{			
				//checks for a change in location
				JComboBox cb = (JComboBox)event.getSource();
				LocationEnum e = (LocationEnum)cb.getSelectedItem();
				save.setLocation(e);
			}
		});		
		
		//checks to see which keyword value, if any is already set
		j = 0;
		for(int i = 0; i < KeywordEnum.values().length; i++)
	        {
	        	if (KeywordEnum.values()[i].toString() == save.getKeyword())
	        	{
	        		j = i;
	        		break;
	        	}
	        }  
		JComboBox keyword = new JComboBox(KeywordEnum.values());
		keyword.setSelectedIndex(j);
		keyword.setVisible(true);
		keyword.addActionListener(new ActionListener()
		{
			//checks for a change in the keyword
			public void actionPerformed(ActionEvent event)
			{				
				JComboBox cb = (JComboBox)event.getSource();
				KeywordEnum e = (KeywordEnum)cb.getSelectedItem();
				save.setKeyword(e);
			}
		});			
	
		//file chooser constructor
		fc = new JFileChooser();      
	    addFileButtonOne = new JButton("Add File...");
	    
	    //list of the names
	    list = new JList(fileNames);
	    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        listScrollPane = new JScrollPane(list);  
        
        JScrollPane pictureScrollPane = new JScrollPane();       
        
        //pane for adding pictures/viewing them at the same time
	    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);    
		splitPane.setLeftComponent(listScrollPane);
	    splitPane.setRightComponent(pictureScrollPane);
	    splitPane.setOneTouchExpandable(true);
	    
	    Dimension minimumSize = new Dimension(50, 50);
        listScrollPane.setMinimumSize(minimumSize);
        pictureScrollPane.setMinimumSize(minimumSize);
	    
	    splitPane.setDividerLocation(50);
        splitPane.setDividerSize(10);
	    splitPane.setPreferredSize(new Dimension(550, 405));       
	    
	    all = new JLabel();
	    allScrollPane = new JScrollPane(all);
	    allScrollPane.setPreferredSize(new Dimension(325, 700));	    
	    
	    JButton saveEntryButton = new JButton("Save Entry");
	    saveEntryButton.addActionListener(new ActionListener()
        {
	    	//save entry exists the program
        	public void actionPerformed(ActionEvent e)
        	{      		
        		for (int j = 0; j < filePaths.size(); j++)
        		{
        			save.getFilePaths().add(filePaths.get(j));
        		}    
        		save.setAuthor(authorListener.getText());
                save.setTitle(titleListener.getText());
                save.setComments(textListener.getText());
        		xml.writeToLogbook(save);
        		//System.exit(0);  
        		return;
        	}
        });
	    
	    //add file
        addFileButtonOne.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
				if (e.getSource() == addFileButtonOne)
				{
					int returnVal = fc.showOpenDialog(LogbookDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						//finds the file they chose
						File file = fc.getSelectedFile();						
						System.out.println(file.getPath());
						fileNames.addElement(file.getName());
						filePaths.addElement(file.getPath());
						//adds the file they chose
						list = new JList(fileNames);
						list.addListSelectionListener(new ListSelectionListener ()
				        {	        
							//checks to see which of the files they have added is being selected
					        public void valueChanged(ListSelectionEvent e) {
					            if (e.getValueIsAdjusting())
					                return;
					
					            JList theList = (JList)e.getSource();					            
					            if (theList.isSelectionEmpty())
					            {
					            	picture.setIcon(null);
					            } else 
					            {
					            	//finds the index which the user selected
					                int index = theList.getSelectedIndex();	
					                ImageIcon newImage = new ImageIcon((filePaths.elementAt(index)));
					                picture.setIcon(newImage);					                
					                picture.setPreferredSize(new Dimension(newImage.getIconWidth(),
					                                                   newImage.getIconHeight() ));
					                //refreshes the picture
					                picture.revalidate();
					            }
					        }
				        });
						//if image is added
				        listScrollPane = new JScrollPane(list);   
						splitPane.setLeftComponent(listScrollPane);						
						ImageIcon firstImage = new ImageIcon(file.getPath());	
						Image img = firstImage.getImage();
						
						//copies the picture into a buffered image
						int width = 300;
						int height = 200;
						
						//recreates the image with a new size and displays it on the right hand side in a smaller version
						BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); 
						Graphics g = bi.createGraphics();	
			
						g.drawImage(img, 0, 0, width, height, null);
				
						
						//the bivector holds all the buffered images
						biVector.addElement(bi);
						
						picture = new JLabel(firstImage);
						picture.setPreferredSize(new Dimension(firstImage.getIconWidth(),
							 firstImage.getIconHeight()));
						JScrollPane pictureScrollPane = new JScrollPane(picture);			 
						splitPane.setRightComponent(pictureScrollPane);	
						Dimension minimumSize = new Dimension(150, 50);
					    listScrollPane.setMinimumSize(minimumSize);
					    pictureScrollPane.setMinimumSize(minimumSize);
					     
					    if (!biVector.isEmpty())
					    {	
					    	//if the bivector is not empty, it adds the images to a boxlayout
					    	//on the right hand side
					    	topPanel = new JPanel();
					    	BoxLayout box = new BoxLayout(topPanel, BoxLayout.Y_AXIS);
					    	topPanel.setLayout(box);
					    	
					    	for (int i = 0; i < biVector.size() ; i++)
					    	{
					    		ImageIcon allIcon = new ImageIcon(biVector.get(i));
					    		all = new JLabel(allIcon);
					    		topPanel.add(all);
					    		topPanel.add(Box.createHorizontalGlue());
					    		topPanel.add(Box.createVerticalGlue());					    		
					    	}
					    	allScrollPane.setViewportView(topPanel);
					    }
		            } 				
				}
        	}
        });
      
		//add's all the labels etc to the frame
		add(severityLabel);
		add(severity);
		add(locationLabel);
		add(location);
		add(keywordLabel);
		add(keyword);
		add(timeLabel);
		add(timeField);
		add(dateLabel);
		add(dateField);		
		add(authorLabel);
		add(authorScrollPane);
		add(titleLabel);
		add(titleScrollPane);
		add(textLabel);
		add(textScrollPane);		
		add(dateLabel);
		add(addFileButtonOne);
	    add(splitPane);    
	    add(allScrollPane);
	    add(saveEntryButton);
	    
		
		// Used SpringLayout to organize all the buttons/labels/fields/picture boxes
		layout.putConstraint(SpringLayout.NORTH, severityLabel, 5,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, severity, 60,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, locationLabel, 5,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, locationLabel, 150,
				SpringLayout.WEST, contentPane);				
		layout.putConstraint(SpringLayout.WEST, location, 210,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, keywordLabel, 5,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, keywordLabel, 300,
				SpringLayout.WEST, contentPane);		
		layout.putConstraint(SpringLayout.WEST, keyword, 360,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, timeLabel, -10,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, timeLabel, 480,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, timeField, -13,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, timeField, 525,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, dateLabel, 12,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, dateLabel, 480,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, dateField, 9,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, dateField, 525,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, authorLabel, 35,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.NORTH, authorScrollPane, 35,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, authorScrollPane, 60,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, titleLabel, 65,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.NORTH, titleScrollPane, 65,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, titleScrollPane, 60,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, textLabel, 95,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.NORTH, textScrollPane, 95,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, textScrollPane, 60,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, splitPane, 330,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, splitPane, 60,
				SpringLayout.WEST, contentPane);
		
		layout.putConstraint(SpringLayout.NORTH, addFileButtonOne, 300,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, addFileButtonOne, 60,
				SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, saveEntryButton, -2,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, saveEntryButton, 738,
				SpringLayout.WEST, contentPane);
		
		
		layout.putConstraint(SpringLayout.NORTH, allScrollPane, 35,
				SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, allScrollPane, 625,
				SpringLayout.WEST, contentPane);
				
		setLayout(layout);			
		setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		
	}
	
	public void actionPerformed(ActionEvent e)
	{	}
	public void valueChanged(ListSelectionEvent e) 
	{}

    private static void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        

        //Create and set up the content pane.
        JComponent newContentPane = new LogbookDialog(save);
        //newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setBounds(100, 100, 1000, 800);
        frame.setVisible(true);
    }
    class MyListener implements DocumentListener
    {
    	//the problem arose where text would be lost if the user did not hit enter
    	//after finished to write the text
    	//MyListener checks for every single key that is typed and stores it in "text"
    	//at the end when the save entry button is pressed it is set to the logbook and saved
    	String text;
    	
    	  public void changedUpdate(DocumentEvent documentEvent) 
    	  {
    		  printInfo(documentEvent);
    	  }

    	  public void insertUpdate(DocumentEvent documentEvent) 
    	  {
    		  printInfo(documentEvent);
    	  }
    	  public void removeUpdate(DocumentEvent documentEvent)
    	  {
    		  printInfo(documentEvent);
    	  }
    	  public String getText()
    	  {
    		  return text;
    	  }

    	  public void printInfo(DocumentEvent documentEvent) 
    	  { 
    	    try
    	    {
    	    	//gets the text
    	    	text = documentEvent.getDocument()
    	    		.getText(0, documentEvent.getDocument().getLength());
    	    }
    	    catch(BadLocationException b)
    	    {
    	    	System.err.println("Bad Location");
    	    }    	   
    	  }
    }; 
    
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {   
            	
                createAndShowGUI();
            }
        });
    }
}



