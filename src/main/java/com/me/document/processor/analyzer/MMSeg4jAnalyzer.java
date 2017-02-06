package com.me.document.processor.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.me.document.processor.common.DocumentAnalyzer;
import com.me.document.processor.common.Term;
import com.me.document.processor.config.Configuration;

public class MMSeg4jAnalyzer extends AbstractDocumentAnalyzer implements DocumentAnalyzer {

	private static final Log LOG = LogFactory.getLog(MMSeg4jAnalyzer.class);
	private final Analyzer analyzer;
	
	public MMSeg4jAnalyzer(Configuration configuration) {
		super(configuration);
		analyzer = new ComplexAnalyzer();
	}
	
	@Override
	public Map<String, Term> analyze(File file) {
		String doc = file.getAbsolutePath();
		LOG.info("Process document: file=" + doc);
		Map<String, Term> terms = new HashMap<String, Term>(0);
		BufferedReader br = null;
		try {
			br = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), charSet));
			String line = null;
			while((line = br.readLine()) != null) {
				StringReader reader = new StringReader(line);
				TokenStream ts = analyzer.tokenStream("", reader);
				ts.addAttribute(CharTermAttribute.class); 
				while (ts.incrementToken()) {  
					CharTermAttributeImpl attr = (CharTermAttributeImpl) ts.getAttribute(CharTermAttribute.class);  
					String word = attr.toString().trim();
					if(!word.isEmpty() && !super.isStopword(word)) {
						Term term = terms.get(word);
						if(term == null) {
							term = new Term(word);
						}
						term.incrFreq();
					} else {
						LOG.debug("Filter out stop word: file=" + file + ", word=" + word);
					}
				}
				ts.close();
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch (IOException e) {
				LOG.warn(e);
			}
			LOG.info("Done: file=" + file + ", termCount=" + terms.size());
		}
		return terms;
	}

	@Override
	public List<Term> analyzeKeepSeq(File file) {
		// TODO Auto-generated method stub
		return null;
	}

}
