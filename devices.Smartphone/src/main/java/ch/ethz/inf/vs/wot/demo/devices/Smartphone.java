package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.semantics.parser.ExecutionPlan;
import ch.ethz.inf.vs.semantics.parser.N3Utils;
import ch.ethz.inf.vs.semantics.parser.elements.N3Document;
import ch.ethz.inf.vs.semantics.parser.elements.N3Element;
import ch.ethz.inf.vs.semantics.parser.elements.RDFResource;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceManufacturer;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceModel;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceName;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceSerial;
import ch.ethz.inf.vs.wot.demo.utils.devices.*;
import ch.ethz.inf.vs.wot.demo.utils.restdesc.*;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The class ThermostatServer a sample thermostat
 */
public class Smartphone extends CoapServer implements ActionListener {

	private static final String DEMO_IP = DeviceServer.RD_ADDRESS;
	// since we register with the RD, we can use a random port
	private static int port = 0;
	private static final long NOTIFICATION_DELAY = 3; // seconds
	
	private static final Color transparent = new Color(0,0,0, Color.TRANSLUCENT);
	private static DevicePanel phone;
	private static JTextArea screen = new JTextArea(80, 25);
	private JPanel questions = new JPanel(new GridLayout(0, 1));
	private JButton saveButton = new JButton("Save");
	
	private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);
	private static ScheduledFuture<?> notifyHandle = null;
	
	private static ArrayList<Question> list = new ArrayList<Question>();
    private static String resourcesDirectory;
    private static String reasonerQueryInterface;
    private static String reasonerMashupInterface;
	
	public static void setColor(Color set) {
		screen.setBackground(set);
	}
	
	public static void setText(String text) {
		screen.setText(text);
	}
	
	public static void notifyText(String text) {
		if (notifyHandle != null) {
			notifyHandle.cancel(false);
		}
		
		screen.setText(text);
		screen.repaint();
		
		notifyHandle = tasks.schedule(new Runnable() {
			@Override
			public void run() {
				screen.setText(null);
				screen.repaint();
			}
		}, NOTIFICATION_DELAY, TimeUnit.SECONDS);
	}

	public static void main(String[] args) {
		
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		
		Smartphone server = new Smartphone(port);
		server.start();

		System.out.printf(Smartphone.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	public Smartphone(int... ports) {
		super(ports);
		// add resources to the server
		add(new CoapResource("dev").add(
			new DeviceManufacturer(),
			new DeviceModel(),
			new DeviceSerial(),
			new DeviceName()));
		
		// GUI
		phone = new DevicePanel(getClass().getResourceAsStream("smartphone_640.png"), 330, 640);  
        screen.setBounds(21, 66, 288, 510);
        screen.setBorder(BorderFactory.createLineBorder(transparent, 10));
		screen.setBackground(Color.darkGray);
		screen.setForeground(Color.green);
		screen.setFont(new Font("Sans Serif", Font.BOLD, 12));
		screen.setEditable(false);
		phone.add(screen);
		JButton queryButton = new JButton("Query");
		queryButton.setBounds(125, 590, 80, 26);
		queryButton.setBorderPainted(false);
		queryButton.setFocusable(false);
		queryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		queryButton.setBackground(transparent);
		queryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeFetch();
			}
		});
		phone.add(queryButton);
		
		questions.setBackground(Color.darkGray);
		questions.setVisible(false);
		
		saveButton.addActionListener(this);
		
		JScrollPane scrollFrame = new JScrollPane(questions);
		questions.setAutoscrolls(true);
		scrollFrame.setPreferredSize(new Dimension(288, 510));
		scrollFrame.setBounds(21, 66, 288, 510);
		scrollFrame.setBorder(BorderFactory.createEmptyBorder());
		
		phone.add(scrollFrame);
		
		new DeviceFrame(phone).setVisible(true);
        
		executeFetch();
	}

    private static void findResourceDirectory() {
        if (resourcesDirectory != null) {
            return;
        }
        CoapClient c = new CoapClient();
        c.setURI("coap://" + DEMO_IP + ":5683");

        notifyText("Looking for RD");
        
        Set<WebLink> resources = c.discover("rt=core.rd-lookup");
        if (resources != null) {
            if (resources.size() > 0) {
                WebLink w = resources.iterator().next();
                String uri = "coap://" + DEMO_IP + ":5683" + w.getURI();
                resourcesDirectory = uri;
                notifyText("Found RD at " + uri);
            }
        }
    }

    private static void findReasonerQueryInterface() {
        if (resourcesDirectory == null || reasonerQueryInterface != null) {
            return;
        }

        CoapClient client = new CoapClient();
        client.setURI(resourcesDirectory + "/res?rt=sr-query");
        CoapResponse response = client.get();

        Set<WebLink> resources = Collections.emptySet();
        if (response.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT)
            resources = LinkFormat.parse(response.getResponseText());
        if (resources.size() > 0) {
            WebLink w = resources.iterator().next();
            reasonerQueryInterface = w.getURI();
            notifyText("Found QI at " + reasonerQueryInterface);
        }
    }

    private static void findReasonerMashupInterface() {
        if (resourcesDirectory == null || reasonerMashupInterface != null) {
            return;
        }

        CoapClient client = new CoapClient();
        client.setURI(resourcesDirectory + "/res?rt=sr-mashup");
        CoapResponse response = client.get();

        Set<WebLink> resources = Collections.emptySet();
        if (response.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT)
            resources = LinkFormat.parse(response.getResponseText());
        if (resources.size() > 0) {
            WebLink w = resources.iterator().next();
            reasonerMashupInterface = w.getURI();
            notifyText("Found MI at " + reasonerMashupInterface);
        }
    }

    public void executeMashup(final String query) {
    	
    	tasks.execute(new Runnable() {
			@Override
			public void run() {
				findResourceDirectory();
		        findReasonerMashupInterface();
		        
		        if (reasonerMashupInterface != null) {
		            CoapClient client = new CoapClient();
		            client.setURI(reasonerMashupInterface);
		            CoapResponse response = client.post(query, MediaTypeRegistry.TEXT_PLAIN);
		            String strplan = response.getResponseText();
		            
		            ExecutionPlan plan = new ExecutionPlan(strplan, new CoapRequest.Factory());
		            plan.execute(new ExecutionPlan.RequestCallback() {
		                @Override
		                public void onComplete(Object r) {
		                	executeFetch();
		                }
		            });
	
		        }
			}
		});
    }

    public void executeFetch() {

    	tasks.execute(new Runnable() {
			@Override
			public void run() {
				
		        findResourceDirectory();
		        findReasonerQueryInterface();
		        
		        if (reasonerQueryInterface != null) {
		            String query = "@prefix : <ex#>. \n" +
		                    "@prefix log: <http://www.w3.org/2000/10/swap/log#>.\n" +
		                    "@prefix e: <http://eulersharp.sourceforge.net/2003/03swap/log-rules#>.\n" +
		                    " \n" +
		                    "{  \n" +
		                    "\t?x a :openquestion.\n" +
		                    "\t?x :replyType ?t.\n" +
		                    "\t?x :text ?text.\n" +
		                    "\t (?S ?SP) e:findall ( { ?X :name ?L }\n" +
		                    "\t    { ?X a ?t. ?X :name ?L}\n" +
		                    "\t    ?ITEMS\n" +
		                    "\t  ) .\n" +
		                    "  ?ITEMS log:conjunction ?F.\n" +
		                    "}\n" +
		                    "=>\n" +
		                    "{  \n" +
		                    "\t?x :options ?F.\n" +
		                    "\t?x :replyType ?t.\n" +
		                    "\t?x :text ?text.\n" +
		                    "}.\n";
		
		            CoapClient client = new CoapClient();
		            client.setURI(reasonerQueryInterface);
		            CoapResponse response = client.post(query, MediaTypeRegistry.TEXT_PLAIN);
		            
		            if (response != null) {
		                String str = response.getResponseText();
		                N3Document resp = N3Utils.parseN3Document(str);
		                List<Question> oldlist = new ArrayList<Question>();
		                oldlist.addAll(list);
		                list.clear();
		                for (N3Element.Statement s : resp.statements) {
		                    if (s instanceof RDFResource) {
		                    	RDFResource t = (RDFResource) s;
		                        Question foundQuestion = null;
		                        String k = t.subject.toString();
		                        for (Question q : oldlist) {
		                            if (q.id.equals(k)) {
		                                foundQuestion = q;
		                                break;
		                            }
		                        }
		                        if (foundQuestion == null) {
		                            list.add(new Question(k, t));
		                        } else {
		                            foundQuestion.update(t);
		                            list.add(foundQuestion);
		                        }
		                    }
		                }
		            }
		        }
		        
				displayList();
		    }
		});
    }
    
    public void displayList() {
    	
    	questions.removeAll();
    	
    	for (Question q : list) {    		
    		questions.add(q);
    	}
    	
    	// Make button smaller than grid space
    	JPanel frame = new JPanel();
    	frame.setBackground(Color.darkGray);
    	frame.add(saveButton);
    	
    	questions.add(frame);
    	
    	screen.setVisible(false);
    	questions.setVisible(true);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		
		for (Question q : list) {
			executeMashup(q.sendResponse());
    	}
	}
}