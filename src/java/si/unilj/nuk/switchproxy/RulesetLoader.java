/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import com.sun.net.httpserver.HttpContext;
import java.io.FileOutputStream;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Serializer;
import nu.xom.Text;

/**
 *
 * @author mitja
 */
public class RulesetLoader {
	public static void LoadFromXml(Vector<UrlMatchRule> ruleSet, ServletContext context) throws Exception {
		ruleSet.clear();
		
		Builder parse = new Builder(false);
		Document doc = parse.build(context.getResourceAsStream("WEB-INF/ruleset.xml"));


		Nodes n = doc.query("/ruleset/rule");
		for(int i = 0; i < n.size(); i++) {
			String urlPattern = n.get(i).query("./url-pattern").get(0).getValue();
			String clientScript = n.get(i).query("./client-script").get(0).getValue();

			ruleSet.add(new UrlMatchRule(urlPattern, clientScript));
		}
	}
	
	public static void SaveToXml(Vector<UrlMatchRule> ruleSet, ServletContext context) throws Exception {
		Element xRuleset = new Element("ruleset");
		Document doc = new Document(xRuleset);
		
			for(UrlMatchRule r : ruleSet) {
			Element xRule = new Element("rule");
			xRuleset.appendChild(xRule);
			
				Element xUrlPattern = new Element("url-pattern");
				xRule.appendChild(xUrlPattern);
				
					xUrlPattern.appendChild(r.getUrlPattern().toString());
					
				Element xClientScript = new Element("client-script");
				xRule.appendChild(xClientScript);
				
					xClientScript.appendChild(new Text(r.getClientScript()));
					xClientScript.addAttribute(new Attribute("xml:space", XMLConstants.XML_NS_URI, "preserve"));
			}
			
		Serializer serializer = new Serializer(
			new FileOutputStream(context.getRealPath("WEB-INF/ruleset.xml"), false),
			"UTF-8");
		serializer.setIndent(3);
		serializer.setLineSeparator("\r\n");
		serializer.write(doc);
		serializer.flush();		
	}
}
