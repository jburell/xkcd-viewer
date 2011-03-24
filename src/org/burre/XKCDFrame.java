package org.burre;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.htmlcleaner.*;

@SuppressWarnings("serial")
public class XKCDFrame extends JFrame {
	public XKCDFrame() {
		setTitle("XKCD");
		getContentPane().setBackground(Color.WHITE);
		java.net.URL iconURL = XKCDFrame.class.getResource("xkcd_icon.png");
		if (iconURL != null) {
			setIconImage(new ImageIcon(iconURL).getImage());
		}
		
		HtmlCleaner cleaner = new HtmlCleaner();
		CleanerProperties props = cleaner.getProperties();
    props.setAllowHtmlInsideAttributes(true);
    props.setAllowMultiWordAttributes(true);
    props.setRecognizeUnicodeChars(true);
    props.setOmitComments(true);
    
		try {
	    URL url = new URL("http://www.xkcd.com/");
	    URLConnection conn = url.openConnection();
	    conn.setConnectTimeout(10000); // 10 sec timout

	    TagNode tag = cleaner.clean(new InputStreamReader(conn.getInputStream()));
			XPather xpath = new XPather("body/div/div[2]/div/div[2]/div/div/img");
			
			Object[] nodes = xpath.evaluateAgainstNode(tag);
			TagNode node = null;
			
			if(nodes != null && nodes.length > 0 && nodes[0] instanceof TagNode){
				node = (TagNode)nodes[0];
			}else{
				throw new ClassCastException("Could not cast to TagNode");
			}
			
			String imgUrl = node.getAttributeByName("src");
			
			BufferedImage imageRef = null;
			if(imgUrl != null && imgUrl.length() > 0){
				imageRef = ImageIO.read(new URL(imgUrl));
			}else{
				java.net.URL noImageURL = XKCDFrame.class.getResource("noImage.jpg");
				if (noImageURL != null) {
					imageRef = ImageIO.read(noImageURL);
				}
			}
			
			final BufferedImage img = imageRef;		
			JPanel panel = new JPanel() {
				@Override
				public void paint(Graphics g) {
					if (img != null) {
						setSize(img.getWidth(), img.getHeight());
						g.drawImage(img, 0, 0, null);
					}
				}
			};

			String desc = node.getAttributeByName("title");
			int pos = 0;
			int lower = 50;
			int upper = 100;
			String resultStr = "<html>";
			do{
				if(pos + upper > desc.length()){
					upper = desc.length() - pos;
				}
				
				String subStr = desc.substring(pos + lower, pos + upper);
				int delimIdx = subStr.indexOf(" ");
				if(delimIdx != -1){
					resultStr = resultStr.concat(desc.substring(pos, pos + lower + delimIdx));
					resultStr = resultStr.concat("<br/>");
					pos += lower + delimIdx + 1;
				}else{
					pos += lower + delimIdx;
				}
				
				if(pos + lower > desc.length()){
					upper = desc.length() - pos;
				}
			}while(pos + lower < desc.length());			
			resultStr = resultStr.concat(desc.substring(pos, desc.length()) + "</html>");
			
			String newDesc = new String(resultStr);			
			panel.setToolTipText(newDesc);
			getContentPane().add(panel);
			setSize(img.getWidth(), img.getHeight());
			setResizable(false);
			invalidate();
			setLocationRelativeTo(null);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {	new XKCDFrame().setVisible(true);	}
		});
	}
}
