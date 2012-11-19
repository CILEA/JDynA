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
package it.cilea.osd.jdyna.controller;

import it.cilea.osd.common.controller.BaseFormController;
import it.cilea.osd.jdyna.model.ADecoratorTypeDefinition;
import it.cilea.osd.jdyna.model.ANestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ATypeNestedObject;
import it.cilea.osd.jdyna.web.ITabService;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class FormNestedObjectDefinitionController<PD extends ANestedPropertiesDefinition, TP extends ATypeNestedObject<PD>, ATN extends ADecoratorTypeDefinition<TP, PD>> extends BaseFormController
{

    private final String TYPO_ADDTEXT = "text";
    private final String TYPO_ADDDATE = "date";
    private final String TYPO_ADDLINK = "link";
    private final String TYPO_ADDFILE = "file";
    private final String TYPO_ADDPOINTERRP = "pointerrp";
    private final String TYPO_ADDPOINTEROU = "pointerou";
    private final String TYPO_ADDPOINTERPJ = "pointerpj";

    private String addTextView;
    private String addDateView;
    private String addLinkView;
    private String addFileView;
    private String addPointerRPView;
    private String addPointerOUView;
    private String addPointerPJView;

    private ITabService applicationService;

    private Class<TP> targetClass;
    private Class<ATN> decoratorClass;

    private String specificPartPath;

    
    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        String paramBoxId = request.getParameter("boxId");
        String paramTabId = request.getParameter("tabId");
        
        map.put("specificPartPath", getSpecificPartPath());
        map.put("tabId", paramTabId);
        map.put("boxId", paramBoxId);
        return map;
    }
    
    @Override
    protected Object formBackingObject(HttpServletRequest request)
            throws Exception
    {
        ATN decorator = null;
        TP object = null;
        String paramId = request.getParameter("pDId");
        if (paramId == null || paramId.isEmpty())
        {
            decorator = (ATN) (super
                    .formBackingObject(request));
            object = targetClass.newInstance();
            decorator.setReal(object);
        }
        else
        {
            Integer id = Integer.parseInt(paramId);
            decorator = (ATN) applicationService
                    .findContainableByDecorable(getCommandClass(), id);
        }
        return decorator;
    }

    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception
    {
        String boxId = request.getParameter("boxId");
        String tabId = request.getParameter("tabId");
        ATN object = (ATN) command;
        getApplicationService().saveOrUpdate(decoratorClass,
                object);

        Map<String, String> maprequest = request.getParameterMap();

        if (maprequest.containsKey(TYPO_ADDTEXT))
        {
            return new ModelAndView(addTextView.trim() + "?renderingparent="
                    + object.getId() + "&boxId=" + boxId
                    + "&tabId=" + tabId);
        }
        if (maprequest.containsKey(TYPO_ADDDATE))
        {
            return new ModelAndView(addDateView.trim() + "?renderingparent="
                    + object.getId() + "&boxId=" + boxId
                    + "&tabId=" + tabId);
        }
        if (maprequest.containsKey(TYPO_ADDLINK))
        {
            return new ModelAndView(addLinkView.trim() + "?renderingparent="
                    + object.getId() + "&boxId=" + boxId
                    + "&tabId=" + tabId);
        }
        if (maprequest.containsKey(TYPO_ADDFILE))
        {
            return new ModelAndView(addFileView.trim() + "?renderingparent="
                    + object.getId() + "&boxId=" + boxId
                    + "&tabId=" + tabId);
        }
        if (maprequest.containsKey(TYPO_ADDPOINTERRP))
        {
            return new ModelAndView(addPointerRPView.trim() + "?renderingparent="
                    + object.getId() + "&boxId=" + boxId
                    + "&tabId=" + tabId);
        }
        if (maprequest.containsKey(TYPO_ADDPOINTERPJ))
        {
            return new ModelAndView(addPointerPJView.trim() + "?renderingparent="
                    + object.getId() + "&boxId=" + boxId
                    + "&tabId=" + tabId);
        }
        if (maprequest.containsKey(TYPO_ADDPOINTEROU))
        {
            return new ModelAndView(addPointerOUView.trim() + "?renderingparent="
                    + object.getId() + "&boxId=" + boxId
                    + "&tabId=" + tabId);
        }

        return new ModelAndView(getSuccessView() + "?id=" + boxId + "&tabId="
                + tabId);

    }
   
    public Class getTargetClass()
    {
        return targetClass;
    }

    public void setTargetClass(Class targetClass)
    {
        this.targetClass = targetClass;
    }

    public String getSpecificPartPath()
    {
        return specificPartPath;
    }

    public void setSpecificPartPath(String specificPartPath)
    {
        this.specificPartPath = specificPartPath;
    }

    public String getAddTextView()
    {
        return addTextView;
    }

    public void setAddTextView(String addTextView)
    {
        this.addTextView = addTextView;
    }

    public String getAddDateView()
    {
        return addDateView;
    }

    public void setAddDateView(String addDateView)
    {
        this.addDateView = addDateView;
    }

    public String getAddLinkView()
    {
        return addLinkView;
    }

    public void setAddLinkView(String addLinkView)
    {
        this.addLinkView = addLinkView;
    }

    public String getAddFileView()
    {
        return addFileView;
    }

    public void setAddFileView(String addFileView)
    {
        this.addFileView = addFileView;
    }

    public ITabService getApplicationService()
    {
        return applicationService;
    }

    public void setApplicationService(ITabService applicationService)
    {
        this.applicationService = applicationService;
    }

    public Class<ATN> getDecoratorClass()
    {
        return decoratorClass;
    }

    public void setDecoratorClass(Class<ATN> decoratorClass)
    {
        this.decoratorClass = decoratorClass;
    }

    public String getAddPointerRPView()
    {
        return addPointerRPView;
    }

    public void setAddPointerRPView(String addPointerRPView)
    {
        this.addPointerRPView = addPointerRPView;
    }

    public String getAddPointerOUView()
    {
        return addPointerOUView;
    }

    public void setAddPointerOUView(String addPointerOUView)
    {
        this.addPointerOUView = addPointerOUView;
    }

    public String getAddPointerPJView()
    {
        return addPointerPJView;
    }

    public void setAddPointerPJView(String addPointerPJView)
    {
        this.addPointerPJView = addPointerPJView;
    }

 
}
