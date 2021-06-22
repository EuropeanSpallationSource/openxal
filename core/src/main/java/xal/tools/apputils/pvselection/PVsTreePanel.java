package xal.tools.apputils.pvselection;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class PVsTreePanel extends JPanel{
	private static final long serialVersionUID = 0L;

    private JTree tree;

    /** the tree cell render */
    private TreeCellRenderer render = null;
    private TreeCellRenderer editRender = null;
    private TreeCellRenderer controlRender = null;

    /** the tree cell  TreeSelectionListener */
    private TreeSelectionListener treeSelectionListener = null;
    private TreeSelectionListener editTreeSelectionListener = null;
    private TreeSelectionListener controlTreeSelectionListener = null;

    private ActionListener extTreeSelectionListener = null;

    private int renderMode;
    public static final int RENDER_MODE_EDIT    = 0;
    public static final int RENDER_MODE_CONTROL = 1;

    /** The constructor.
     *  @param pvNode - the root PV node, specifying all structure of PVs
     */
    public PVsTreePanel(PVTreeNode pvNode){

	if(pvNode == null){
	    pvNode = new PVTreeNode();
	}

	editRender = new EditModeTreeCellRenderer();
	controlRender = new ControlModeTreeCellRenderer();

        tree = new JTree((TreeNode) pvNode);
	tree.setRootVisible(false); 
        tree.setBackground(getBackground());

	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); 

	tree.setShowsRootHandles(true);

        render = controlRender;
	renderMode = RENDER_MODE_CONTROL;
	tree.setCellRenderer(render);

	//Action listener
	MouseListener ml = new MouseAdapter() {
                @Override
		public void mousePressed(MouseEvent e) {
		    int selRow = tree.getRowForLocation(e.getX(), e.getY());
		    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		    if(selRow != -1) {
			Object value = selPath.getLastPathComponent();
			if(value instanceof PVTreeNode){
			    PVTreeNode tn = (PVTreeNode) value;
			    if(extTreeSelectionListener != null){
				ActionEvent evnt = new ActionEvent(tn,1,"selected");
				extTreeSelectionListener.actionPerformed(evnt);
			    } 
			}

		    }
		}
	    };

	tree.addMouseListener(ml);

	//listeners
        editTreeSelectionListener =  new TreeSelectionListener(){
                @Override
		public void valueChanged(TreeSelectionEvent e){
		    TreePath path_new = e.getNewLeadSelectionPath();
		    if(path_new != null){
			PVTreeNode tn_new = (PVTreeNode) path_new.getLastPathComponent();
			if(tn_new.isPVName() || tn_new.isPVNamesAllowed()){
			    tn_new.setSelected(true);
			}
		    }
		    TreePath path_old = e.getOldLeadSelectionPath();
		    if(path_old != null){
			PVTreeNode tn_old = (PVTreeNode) path_old.getLastPathComponent(); 
			if(tn_old.isPVName() || tn_old.isPVNamesAllowed()){
			    tn_old.setSelected(false);
			}
		    }
		}
	    };

	controlTreeSelectionListener  =  new TreeSelectionListener(){
                @Override
		public void valueChanged(TreeSelectionEvent e){
		    TreePath path = e.getNewLeadSelectionPath();
		    if(path != null){
			PVTreeNode tn = (PVTreeNode) path.getLastPathComponent();
			if(tn.isPVName()){
			    if(tn.isSwitchedOn()){
				tn.setSwitchedOn(false);
			    }
			    else{
				tn.setSwitchedOn(true);
			    }
			}
		    }
		    tree.clearSelection();
		}
	    };

	treeSelectionListener = controlTreeSelectionListener;
	tree.addTreeSelectionListener(treeSelectionListener);

	JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(tree);

        setLayout(new BorderLayout());
        add(scrollPane,BorderLayout.CENTER);
	setAllFonts(getFont());
    }

    public JTree getJTree(){
	return tree;
    }

    public void setEditMode(){
	render = editRender;
        tree.removeTreeSelectionListener(treeSelectionListener); 
	treeSelectionListener = editTreeSelectionListener;
        tree.setCellRenderer(render);
	tree.addTreeSelectionListener(treeSelectionListener);
        renderMode = RENDER_MODE_EDIT;
    }

    public void setControlMode(){
	render = controlRender;
        tree.removeTreeSelectionListener(treeSelectionListener); 
	treeSelectionListener = controlTreeSelectionListener;
        tree.setCellRenderer(render);
 	tree.addTreeSelectionListener(treeSelectionListener);
        renderMode = RENDER_MODE_CONTROL;
    }

    public void setExtTreeSelectionListener(ActionListener extTreeSelectionListener){
	this.extTreeSelectionListener = extTreeSelectionListener;
    }

    public int getRenderMode(){
        return renderMode;
    }

    public void setAllFonts(Font fnt){
	tree.setFont(fnt);
    }
}

class EditModeTreeCellRenderer implements TreeCellRenderer{

    public EditModeTreeCellRenderer(){}
 
    @Override
    public Component getTreeCellRendererComponent(JTree tree,
						  Object value,
						  boolean selected,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus)
    {
	Font fnt = tree.getFont();
	JPanel treecell = new JPanel();
	treecell.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	treecell.setFont(fnt);

        if(value instanceof PVTreeNode){
	    if(((PVTreeNode) value).isPVName()){
                JLabel pvLabel   = new JLabel("PV : ",JLabel.LEFT);
		pvLabel.setForeground(Color.blue);
                JLabel nameLabel = new JLabel(((PVTreeNode) value).getName(),JLabel.LEFT);
                treecell.add(pvLabel);
                treecell.add(nameLabel);
		if(selected){
		    pvLabel.setBackground(treecell.getBackground().brighter());
		    treecell.setBackground(treecell.getBackground().brighter());
		    nameLabel.setBackground(treecell.getBackground().brighter());
		}
		if(((PVTreeNode) value).getColor() != null){
		    nameLabel.setForeground(((PVTreeNode) value).getColor());
		}
                pvLabel.setFont(fnt);
		nameLabel.setFont(fnt);
	    }
	    else{
		JLabel nameLabel = new JLabel(((PVTreeNode) value).getName(),JLabel.LEFT);
		nameLabel.setFont(fnt);
		treecell.add(nameLabel);
		if(((PVTreeNode) value).isPVNamesAllowed()){
		    if(selected){
                        nameLabel.setForeground(Color.blue);
			nameLabel.setBackground(treecell.getBackground().brighter());
			treecell.setBackground(treecell.getBackground().brighter());            
		    }
		}
	    }
	}
	return treecell;     
    }
} 


class ControlModeTreeCellRenderer implements TreeCellRenderer{

    private static final EmptyBorder EMPTY_BORDER = new EmptyBorder(0,0,0,0);

    public ControlModeTreeCellRenderer(){}
 
    @Override
    public Component getTreeCellRendererComponent(JTree tree,
						  Object value,
						  boolean selected,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus)
    {
	Font fnt = tree.getFont();
	Color bkgColor = tree.getBackground();
	JPanel treecell = new JPanel();
	treecell.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	treecell.setFont(fnt);
	treecell.setBackground(bkgColor);

        if(value instanceof PVTreeNode){
	    String name = ((PVTreeNode)value).getName();
	    if(((PVTreeNode) value).isPVName()){
		if(((PVTreeNode) value).isCheckBoxVisible()){
		    JCheckBox chckB = null;
		    if(name != null){
			chckB = new JCheckBox(((PVTreeNode)value).getName());
		    }
		    else{
			chckB = new JCheckBox("");
		    }
		    chckB.setBackground(bkgColor);
		    chckB.setBorder(EMPTY_BORDER);
		    chckB.setFont(fnt);
		    if(((PVTreeNode) value).isSwitchedOn()){
			chckB.setSelected(true);
		    }
		    else{
			chckB.setSelected(false);
		    }
		    treecell.add(chckB);
		    if(((PVTreeNode) value).getColor() != null){
			chckB.setForeground(((PVTreeNode) value).getColor());
		    }
		}
		else{
		    JLabel nameLabel = null;
		    if(name != null){
			nameLabel = new JLabel(name,JLabel.LEFT);
		    }
		    else{
                       nameLabel = new JLabel("",JLabel.LEFT);
		    }
                    nameLabel.setForeground(((PVTreeNode) value).getColor());
		    nameLabel.setFont(fnt);
                    treecell.add(nameLabel);
		}
	    }
	    else{
		JLabel nameLabel = new JLabel(((PVTreeNode) value).getName(),JLabel.LEFT);
		nameLabel.setFont(fnt);
		treecell.add(nameLabel);
	    }
	}
	return treecell;     
    }
} 
