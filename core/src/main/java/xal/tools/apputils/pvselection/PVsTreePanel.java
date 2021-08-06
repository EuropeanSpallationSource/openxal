package xal.tools.apputils.pvselection;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class PVsTreePanel extends JPanel {

    private static final long serialVersionUID = 0L;

    private JTree tree;

    /**
     * the tree cell render
     */
    private transient TreeCellRenderer render = null;
    private transient TreeCellRenderer editRender = null;
    private transient TreeCellRenderer controlRender = null;

    /**
     * the tree cell TreeSelectionListener
     */
    private transient TreeSelectionListener treeSelectionListener = null;
    private transient TreeSelectionListener editTreeSelectionListener = null;
    private transient TreeSelectionListener controlTreeSelectionListener = null;

    private transient ActionListener extTreeSelectionListener = null;

    private int renderMode;
    public static final int RENDER_MODE_EDIT = 0;
    public static final int RENDER_MODE_CONTROL = 1;

    /**
     * The constructor.
     *
     * @param pvNode - the root PV node, specifying all structure of PVs
     */
    public PVsTreePanel(PVTreeNode pvNode) {
        if (pvNode == null) {
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
                if (selRow != -1) {
                    Object value = selPath.getLastPathComponent();
                    if (value instanceof PVTreeNode) {
                        PVTreeNode tn = (PVTreeNode) value;
                        if (extTreeSelectionListener != null) {
                            ActionEvent evnt = new ActionEvent(tn, 1, "selected");
                            extTreeSelectionListener.actionPerformed(evnt);
                        }
                    }
                }
            }
        };

        tree.addMouseListener(ml);

        //listeners
        editTreeSelectionListener = e -> {
            TreePath pathNew = e.getNewLeadSelectionPath();
            if (pathNew != null) {
                PVTreeNode tnNew = (PVTreeNode) pathNew.getLastPathComponent();
                if (tnNew.isPVName() || tnNew.isPVNamesAllowed()) {
                    tnNew.setSelected(true);
                }
            }
            TreePath pathOld = e.getOldLeadSelectionPath();
            if (pathOld != null) {
                PVTreeNode tnOld = (PVTreeNode) pathOld.getLastPathComponent();
                if (tnOld.isPVName() || tnOld.isPVNamesAllowed()) {
                    tnOld.setSelected(false);
                }
            }
        };

        controlTreeSelectionListener = e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                PVTreeNode tn = (PVTreeNode) path.getLastPathComponent();
                if (tn.isPVName()) {
                    tn.setSwitchedOn(!tn.isSwitchedOn());
                }
            }
            tree.clearSelection();
        };

        treeSelectionListener = controlTreeSelectionListener;
        tree.addTreeSelectionListener(treeSelectionListener);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(tree);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        setAllFonts(getFont());
    }

    public JTree getJTree() {
        return tree;
    }

    public void setEditMode() {
        render = editRender;
        tree.removeTreeSelectionListener(treeSelectionListener);
        treeSelectionListener = editTreeSelectionListener;
        tree.setCellRenderer(render);
        tree.addTreeSelectionListener(treeSelectionListener);
        renderMode = RENDER_MODE_EDIT;
    }

    public void setControlMode() {
        render = controlRender;
        tree.removeTreeSelectionListener(treeSelectionListener);
        treeSelectionListener = controlTreeSelectionListener;
        tree.setCellRenderer(render);
        tree.addTreeSelectionListener(treeSelectionListener);
        renderMode = RENDER_MODE_CONTROL;
    }

    public void setExtTreeSelectionListener(ActionListener extTreeSelectionListener) {
        this.extTreeSelectionListener = extTreeSelectionListener;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public void setAllFonts(Font fnt) {
        tree.setFont(fnt);
    }
}

class EditModeTreeCellRenderer implements TreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        Font fnt = tree.getFont();
        JPanel treecell = new JPanel();
        treecell.setLayout(new FlowLayout(SwingConstants.LEFT, 0, 0));
        treecell.setFont(fnt);

        if (value instanceof PVTreeNode) {
            if (((PVTreeNode) value).isPVName()) {
                JLabel pvLabel = new JLabel("PV : ", SwingConstants.LEFT);
                pvLabel.setForeground(Color.blue);
                JLabel nameLabel = new JLabel(((PVTreeNode) value).getName(), SwingConstants.LEFT);
                treecell.add(pvLabel);
                treecell.add(nameLabel);
                if (selected) {
                    pvLabel.setBackground(treecell.getBackground().brighter());
                    treecell.setBackground(treecell.getBackground().brighter());
                    nameLabel.setBackground(treecell.getBackground().brighter());
                }
                if (((PVTreeNode) value).getColor() != null) {
                    nameLabel.setForeground(((PVTreeNode) value).getColor());
                }
                pvLabel.setFont(fnt);
                nameLabel.setFont(fnt);
            } else {
                JLabel nameLabel = new JLabel(((PVTreeNode) value).getName(), SwingConstants.LEFT);
                nameLabel.setFont(fnt);
                treecell.add(nameLabel);
                if (((PVTreeNode) value).isPVNamesAllowed() && selected) {
                    nameLabel.setForeground(Color.blue);
                    nameLabel.setBackground(treecell.getBackground().brighter());
                    treecell.setBackground(treecell.getBackground().brighter());
                }
            }
        }
        return treecell;
    }
}

class ControlModeTreeCellRenderer implements TreeCellRenderer {

    private static final EmptyBorder EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        Font fnt = tree.getFont();
        Color bkgColor = tree.getBackground();
        JPanel treecell = new JPanel();
        treecell.setLayout(new FlowLayout(SwingConstants.LEFT, 0, 0));
        treecell.setFont(fnt);
        treecell.setBackground(bkgColor);

        if (value instanceof PVTreeNode) {
            String name = ((PVTreeNode) value).getName();
            if (((PVTreeNode) value).isPVName()) {
                if (((PVTreeNode) value).isCheckBoxVisible()) {
                    JCheckBox chckB;
                    if (name != null) {
                        chckB = new JCheckBox(((PVTreeNode) value).getName());
                    } else {
                        chckB = new JCheckBox("");
                    }
                    chckB.setBackground(bkgColor);
                    chckB.setBorder(EMPTY_BORDER);
                    chckB.setFont(fnt);
                    chckB.setSelected(((PVTreeNode) value).isSwitchedOn());
                    treecell.add(chckB);
                    if (((PVTreeNode) value).getColor() != null) {
                        chckB.setForeground(((PVTreeNode) value).getColor());
                    }
                } else {
                    JLabel nameLabel;
                    if (name != null) {
                        nameLabel = new JLabel(name, SwingConstants.LEFT);
                    } else {
                        nameLabel = new JLabel("", SwingConstants.LEFT);
                    }
                    nameLabel.setForeground(((PVTreeNode) value).getColor());
                    nameLabel.setFont(fnt);
                    treecell.add(nameLabel);
                }
            } else {
                JLabel nameLabel = new JLabel(((PVTreeNode) value).getName(), SwingConstants.LEFT);
                nameLabel.setFont(fnt);
                treecell.add(nameLabel);
            }
        }
        return treecell;
    }
}
