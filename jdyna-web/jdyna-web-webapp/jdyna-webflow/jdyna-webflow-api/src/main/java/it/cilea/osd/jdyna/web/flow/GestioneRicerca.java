/*
 * JDynA, Dynamic Metadata Management for Java Domain Object
 * 
 *  Copyright (c) 2008, CILEA and third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by CILEA.
 * 
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU
 *  Lesser General Public License v3 or any later version, as published 
 *  by the Free Software Foundation, Inc. <http://fsf.org/>.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 *  for more details.
 * 
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */
package it.cilea.osd.jdyna.web.flow;


import it.cilea.osd.common.util.displaytag.DisplayTagData;
import it.cilea.osd.jdyna.model.AnagraficaSupport;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.service.IPersistenceDynaService;
import it.cilea.osd.jdyna.service.ISearchDynaService;
import it.cilea.osd.jdyna.service.PersistenceDynaService;
import it.cilea.osd.jdyna.service.SearchService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class GestioneRicerca extends FormAction {
   
	/** The logger */
	private final static Log log = LogFactory.getLog(GestioneRicerca.class);
	
	
	private IPersistenceDynaService applicationService;

	private ISearchDynaService searchService;
	
	protected Map<String, Class<? extends AnagraficaSupport>> indexedClass;

	
	public GestioneRicerca(IPersistenceDynaService applicationService, ISearchDynaService searchService) {
		this.applicationService = applicationService;
		this.searchService = searchService;
	}
	
	public Event ricercaSemplice(RequestContext requestContext) throws ParseException, InstantiationException, IllegalAccessException {
		
		String classe = (String) requestContext.getFlowScope().get("classe");
		String query = (String) requestContext.getFlowScope().get("query");
		String sortParam = (String) requestContext.getRequestParameters().get("sort");
		
				
		Map<String, Object> results = new HashMap<String, Object>();
		
		if(query.isEmpty() || query.equals("*")) {			
			return error();
		}
		
		for (String index : indexedClass.keySet())
		{
			if (classe.equals("ovunque") || classe.equals(index)) {
				String sort = "score";
				String dir = "asc"; 				
				
				if (sortParam != null && sortParam.startsWith(index+"_"))
				{
					// alla lunghezza del nome indice va sommato 1 perche' e' stato aggiunto _
					sort = sortParam.substring(index.length() + 1);
					dir = (String) requestContext.getRequestParameters().get("dir");					
				}
				
				int maxResults = 20;
				String paramPage = (String) requestContext.getRequestParameters().get("page");
				int page= paramPage != null ? Integer.parseInt(paramPage) : 1;
				Integer numTotale = searchService.count("default", query, indexedClass.get(index));
				if (numTotale == 0) continue;
				List result = searchService.search("default", query, indexedClass.get(index),sort, dir, page, maxResults);
				DisplayTagData displayResult = new DisplayTagData(numTotale.longValue(),result,sortParam,dir,page,maxResults);
				results.put(index, displayResult);
				List<? extends PropertiesDefinition> listTipologieShowInColumn = applicationService.getValoriDaMostrare(getTPfromClass(indexedClass.get(index)));
				requestContext.getFlowScope().put(index+"showInColumnList", listTipologieShowInColumn);
			}
			
		}
		

		
		requestContext.getFlowScope().put("results", results);
		requestContext.getFlowScope().put("clazz",classe);
		
		return success();
	}



	private Class<PropertiesDefinition> getTPfromClass(
			Class<? extends AnagraficaSupport> class1) throws InstantiationException, IllegalAccessException {
		return class1.newInstance().getClassPropertiesDefinition();
	}


	public void setIndexedClass(
			Map<String, Class<? extends AnagraficaSupport>> indexedClass) {
		this.indexedClass = indexedClass;
	}


	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setApplicationService(PersistenceDynaService applicationService) {
		this.applicationService = applicationService;
	}

}
