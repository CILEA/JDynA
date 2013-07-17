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
import it.cilea.osd.jdyna.model.ADecoratorPropertiesDefinition;
import it.cilea.osd.jdyna.model.AType;
import it.cilea.osd.jdyna.model.AWidget;
import it.cilea.osd.jdyna.model.Containable;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.web.IPropertyHolder;
import it.cilea.osd.jdyna.web.ITabService;
import it.cilea.osd.jdyna.web.Tab;
import it.cilea.osd.jdyna.web.TypedBox;
import it.cilea.osd.jdyna.web.Utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class FormDecoratorPropertiesDefinitionController<W extends AWidget, TP extends PropertiesDefinition, DTP extends ADecoratorPropertiesDefinition<TP>, H extends IPropertyHolder<Containable>, T extends Tab<H>>
        extends BaseFormController
{

    private ITabService applicationService;

    private Class<TP> targetModel;

    private Class<W> renderingModel;
    
    private Class<H> boxModel;
    
    private Class<AType<TP>> typoModel;

    private String specificPartPath;

    public FormDecoratorPropertiesDefinitionController(Class<TP> targetModel,
            Class<W> renderingModel, Class<H> boxModel)
    {
        this.targetModel = targetModel;
        this.renderingModel = renderingModel;
        this.boxModel = boxModel;
    }

    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String paramBoxId = request.getParameter("boxId");
        String paramTabId = request.getParameter("tabId");
        map.put("tabId", paramTabId);
        map.put("boxId", paramBoxId);
        map.put("specificPartPath", Utils.getAdminSpecificPath(request, null));
        return map;
    }

    @Override
    protected DTP formBackingObject(HttpServletRequest request)
            throws Exception
    {
        DTP decorator = null;
        TP object = null;
        String paramId = request.getParameter("pDId");
        if (paramId == null || paramId.isEmpty())
        {
            object = targetModel.newInstance();
            W widget = renderingModel.newInstance();
            object.setRendering(widget);
            decorator = (DTP) object.getDecoratorClass().newInstance();
            decorator.setReal(object);
        }
        else
        {
            Integer id = Integer.parseInt(paramId);
            object = applicationService.get(targetModel, id);
            decorator = (DTP) applicationService.findContainableByDecorable(
                    object.getDecoratorClass(), id);
        }
        return decorator;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception
    {

        String boxId = request.getParameter("boxId");
        String tabId = request.getParameter("tabId");
        
        DTP object = (DTP) command;
                
        applicationService.saveOrUpdate(object.getDecoratorClass(), object);
                
        if(boxId!=null && !boxId.isEmpty()) {
            H box = getApplicationService().get(boxModel, Integer.parseInt(boxId));
            if(!(box.getMask().contains(object))) {
                box.getMask().add(object);
                getApplicationService().saveOrUpdate(boxModel, box);
            }            
            getApplicationService().saveOrUpdate(boxModel, box);
            if(TypedBox.class.isAssignableFrom(boxModel)) {
                TypedBox tbox = (TypedBox)box;
                AType<TP> typo = tbox.getTypeDef();                
                if(!(typo.getMask().contains(object.getReal()))) {
                    typo.getMask().add(object.getReal());
                    getApplicationService().saveOrUpdate(typoModel, typo);
                }
                getApplicationService().saveOrUpdate(typoModel, typo);
            }
            
        }        
        return new ModelAndView(getSuccessView()+"?id="+boxId+"&tabId="+tabId+"&path="+Utils.getAdminSpecificPath(request, null));
    }

    public void setApplicationService(ITabService applicationService)
    {
        this.applicationService = applicationService;
    }

    public ITabService getApplicationService()
    {
        return applicationService;
    }

    public Class<TP> getTargetModel()
    {
        return targetModel;
    }

    public String getSpecificPartPath()
    {
        return specificPartPath;
    }

    public void setSpecificPartPath(String specificPartPath)
    {
        this.specificPartPath = specificPartPath;
    }

    public Class<W> getRenderingModel()
    {
        return renderingModel;
    }

    public Class<H> getBoxModel()
    {
        return boxModel;
    }

    public void setTypoModel(Class<AType<TP>> typoModel)
    {
        this.typoModel = typoModel;
    }

    public Class<AType<TP>> getTypoModel()
    {
        return typoModel;
    }
  
}