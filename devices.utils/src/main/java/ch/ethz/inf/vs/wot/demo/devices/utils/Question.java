package ch.ethz.inf.vs.wot.demo.devices.utils;

import org.apache.commons.lang3.StringEscapeUtils;

import ch.ethz.inf.vs.semantics.parser.elements.Formula;
import ch.ethz.inf.vs.semantics.parser.elements.Iri;
import ch.ethz.inf.vs.semantics.parser.elements.Literal;
import ch.ethz.inf.vs.semantics.parser.elements.N3Document;
import ch.ethz.inf.vs.semantics.parser.elements.N3Element;
import ch.ethz.inf.vs.semantics.parser.elements.Prefix;
import ch.ethz.inf.vs.semantics.parser.elements.RDFResource;
import ch.ethz.inf.vs.semantics.parser.elements.Verb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;

public class Question extends JPanel {
    
	private static final long serialVersionUID = 1557460449720441816L;
	
	private RDFResource triple;
	private JTextArea label = new JTextArea();
    private JTextField answerField = new JTextField();
    private String questionText;
    public ArrayList<Option> options;
    public boolean loading;
    public String id;

    public Question(String id, RDFResource t) {
        this.id = id;
        
        this.setLayout(new GridLayout(2,1));
        this.setBackground(Color.darkGray);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setPreferredSize(new Dimension(260, 100));
        
        label.setFont(new Font("Sans Serif", Font.BOLD, 12));
        label.setLineWrap(true);
        label.setEditable(false);
        label.setFocusable(false);
        this.add(label);
        
        answerField.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black), BorderFactory.createEmptyBorder(5, 5, 5, 5) ));
        this.add(answerField);

        update(t);
    }

    public String get(String field) {
        if ("answer".equals(field)) {
            return answerField.getText();
        } else if ("questionText".equals(field)) {
            return questionText;
        }
        throw new RuntimeException("Invalid field");
    }

    public String sendResponse() {
    	
    	if (answerField.getText().equals("")) return "";
    	
        N3Document input = new N3Document();
        input.addPrefix(new Prefix("in:", "<in#>"));
        input.addPrefix(new Prefix(":", "<ex#>"));
        Iri answerObject = new Iri(input, "in:" + UUID.randomUUID().toString().replace("-", ""));

        Iri replyType = input.importToDocument((Iri) triple.get(":replyType"));
        RDFResource t = new RDFResource(answerObject)
                .add(new Iri(input, ":name"), new Literal("\"" + StringEscapeUtils.escapeJava(answerField.getText()) + "\""))
                .add(new Verb("a"), new Iri(input, ":Answer"))
                .add(new Verb("a"), replyType);

        return sendResponse(t);
    }

    public String sendResponse(N3Element.Subject subject) {
        return sendResponse(new RDFResource(subject));
    }

    public String sendResponse(RDFResource t) {

        N3Document input = new N3Document();
        input.addPrefix(new Prefix(":", "<ex#>"));
        Iri question = input.importToDocument((Iri) triple.subject);
        t = input.importToDocument(t);
        t.add(new Iri(input, ":answers"), question);
        input.statements.add(t);

        N3Document goal = new N3Document();
        goal.addPrefix(new Prefix(":", "<ex#>"));
        question = goal.importToDocument(question);
        Iri answerObject = goal.importToDocument((Iri) t.subject);
        t = new RDFResource(question)
                .add(new Verb(":hasAnswer"), answerObject);
        Formula s = new Formula();
        s.add(t);
        t = new RDFResource(s).add(new Verb("=>"), new Formula());
        goal.statements.add(t);
        String query = goal + "\n########################\n" + input;
        return query;
    }

    public void update(RDFResource t) {
        this.triple = t;
        questionText = t.getReadableString(":text");
        label.setText(questionText);
        answerField.setText("");
        Object o = t.get(":options");

        options = new ArrayList<Option>();
        if (o instanceof Formula) {
            Formula f = (Formula) o;
            for (N3Element.Statement s : f) {
                if (s instanceof RDFResource) {
                    Option qoption = new Option(((RDFResource) s).subject, ((RDFResource) s).getReadableString(":name"));
                    options.add(qoption);
                }
            }
        }
        
        this.repaint();
    }
    
    static public class Option {
        public final N3Element.Subject subject;
        public final String name;

        public Option(N3Element.Subject subject, String name) {

            this.subject = subject;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
    	g.setColor(Color.white);
        g.fillRoundRect(10, 10, this.getWidth()-20, this.getHeight()-20, 20, 20);
    }
}
