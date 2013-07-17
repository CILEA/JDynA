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
package it.cilea.osd.jdyna.web.controller;

import it.cilea.osd.common.controller.BaseFormController;
import it.cilea.osd.jdyna.model.AnagraficaObject;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.model.Property;
import it.cilea.osd.jdyna.service.IPersistenceDynaService;
import it.cilea.osd.jdyna.util.AnagraficaUtils;
import it.cilea.osd.jdyna.util.ImportPropertyAnagraficaUtil;
import it.cilea.osd.jdyna.utils.FileUploadConfiguration;
import it.cilea.osd.jdyna.web.IPropertyHolder;
import it.cilea.osd.jdyna.web.Tab;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

public class ImportAnagraficaObject<P extends Property<TP>,TP extends PropertiesDefinition, H extends IPropertyHolder, A extends Tab<H>, B extends ImportPropertyAnagraficaUtil> extends BaseFormController {
	
	
	private Class<B> beanPropertyClass;
	private Class<AnagraficaObject<P, TP>> modelClass;
	
	private IPersistenceDynaService applicationService;
	private AnagraficaUtils anagraficaUtils;
	private String path;
	
	/** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());
    
	/** Performa l'import da file xml di configurazioni di oggetti;
	 *  Sull'upload del file la configurazione dell'oggetto viene caricato come contesto di spring
	 *  e salvate su db.
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object object, BindException errors)
			throws RuntimeException, IOException {
					
			
			FileUploadConfiguration bean = (FileUploadConfiguration)object;
			MultipartFile file = (CommonsMultipartFile)bean.getFile();		
			File a = null;

			//creo il file temporaneo che sara' caricato come contesto di spring per caricare la configurazione degli oggetti
			a = File.createTempFile("jdyna", ".xml", new File(path));
			file.transferTo(a);
			
			ApplicationContext context = null;			
			try {			
				context = new FileSystemXmlApplicationContext(new String [] {"file:"+a.getAbsolutePath()}); 				
			}
			catch(XmlBeanDefinitionStoreException exc) {
				//cancello il file dalla directory temporanea
				logger.warn("Error during the configuration import from file: "+ file.getOriginalFilename(), exc);
				a.delete();
				saveMessage(request, getText(
						"action.file.nosuccess.upload", new Object[]{exc.getMessage()}, request
								.getLocale()));		
				return new ModelAndView(getErrorView());
			}
			//cancello il file dalla directory temporanea
			a.delete();
			String[] tpDefinitions = context.getBeanDefinitionNames(); //getBeanNamesForType(tpClass);
			AnagraficaObject<P, TP> anagraficaObject = null;

			String idStringAnagraficaObject = request.getParameter("id");
				Integer pkey = Integer.parseInt(idStringAnagraficaObject);
				anagraficaObject = applicationService.get(modelClass, pkey);
			
			//la variabile i conta le tipologie caricate con successo
			int i = 0;
			//la variabile j conta le tipologie non caricate
			int j = 0;

			for (String tpNameDefinition : tpDefinitions) {
				try {										
					ImportPropertyAnagraficaUtil importBean = (ImportPropertyAnagraficaUtil) context.getBean(tpNameDefinition);
					anagraficaUtils.importProprieta(anagraficaObject, importBean);					
				}
				catch(Exception ecc) {
					saveMessage(request, getText(
							"action.file.nosuccess.metadato.upload", new Object[]{ecc.getMessage()}, request
									.getLocale()));					
					j++;
					i--;
				}
				i++;
			}
			//pulisco l'anagrafica
			anagraficaObject.pulisciAnagrafica();
			applicationService.saveOrUpdate(modelClass, anagraficaObject);
			
			saveMessage(request, getText("action.file.success.upload",
				new Object[] {
						new String("Totale Oggetti Caricati:" + (i + j) + "" + "[" + i
								+ " caricate con successo/" + j
								+ " fallito caricamento]") }, request
						.getLocale()));
		
		return new ModelAndView(getDetailsView());
	}



	public void setApplicationService(IPersistenceDynaService applicationService) {
		this.applicationService = applicationService;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}	

	public AnagraficaUtils getAnagraficaUtils() {
		return anagraficaUtils;
	}

	public void setAnagraficaUtils(AnagraficaUtils anagraficaUtils) {
		this.anagraficaUtils = anagraficaUtils;
	}


	public Class<B> getBeanPropertyClass() {
		return beanPropertyClass;
	}


	public void setBeanPropertyClass(Class<B> beanPropertyClass) {
		this.beanPropertyClass = beanPropertyClass;
	}



	public Class<AnagraficaObject<P, TP>> getModelClass() {
		return modelClass;
	}



	public void setModelClass(Class<AnagraficaObject<P, TP>> modelClass) {
		this.modelClass = modelClass;
	}
}
