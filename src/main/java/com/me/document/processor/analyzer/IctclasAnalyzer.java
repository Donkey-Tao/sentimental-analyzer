package com.me.document.processor.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kevin.zhang.NLPIR;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.me.document.processor.common.DocumentAnalyzer;
import com.me.document.processor.common.Term;
import com.me.document.processor.config.Configuration;

public class IctclasAnalyzer extends AbstractDocumentAnalyzer implements DocumentAnalyzer {
	private static final Log LOG = LogFactory.getLog(IctclasAnalyzer.class);
	private final NLPIR analyzer;
	
	public IctclasAnalyzer(Configuration configuration) {
		super(configuration);
		analyzer = new NLPIR();
		try {
			boolean initialized = NLPIR.NLPIR_Init(".".getBytes(charSet), 1);
			if(!initialized) {
				throw new RuntimeException("Fail to initialize!");
			}
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	@Override
	public Map<String, Term> analyze(File file) {
		String doc = file.getAbsolutePath();
		LOG.info("Process document: file=" + doc);
		Map<String, Term> terms = new HashMap<String, Term>(0);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
			String line = null;
			while((line = br.readLine()) != null) {
				line = line.trim();
				if(!line.isEmpty()) {
					byte nativeBytes[] = analyzer.NLPIR_ParagraphProcess(line.getBytes(charSet), 1);
					String content = new String(nativeBytes, 0, nativeBytes.length, charSet);
					String[] rawWords = content.split("\\s+");
					for(String rawWord : rawWords) {
						String[] words = rawWord.split("/");
						if(words.length == 2) {
							String word = words[0];
							String lexicalCategory = words[1];
							Term term = terms.get(word);
							if(term == null) {
								term = new Term(word);
								// TODO set lexical category
								term.setLexicalCategory(lexicalCategory);
								terms.put(word, term);
							}
							term.incrFreq();
							LOG.debug("Got word: word=" + rawWord);
						}
					}
				}
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
		}
		return terms;
	}

	@Override
	public List<Term> analyzeKeepSeq(File file) {
		// TODO Auto-generated method stub
		return null;
	}

}
