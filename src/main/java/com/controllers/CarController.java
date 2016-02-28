package com.controllers;

import com.dao.ResultCars;
import com.helpers.EncoderId;
import com.helpers.FileUploadForm;
import com.dao.CarDAO;
import com.dao.DealerDao;
import com.helpers.SearchOptions;
import com.modelClass.Car;
import com.servise.ChangeImgSize;
import com.servise.StandartMasege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.helpers.ViewHalper;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by Эдуард on 30.11.15.
 */
@Controller
public class CarController {
    @Autowired
    CarDAO carDAO;
    @Autowired
    DealerDao dealerDao;
    @Autowired
    StandartMasege standartMasege;
    @Autowired
    ChangeImgSize changeImgSize;
    @RequestMapping(value = "/getPhoto", method = RequestMethod.GET)
    public void getPhoto(HttpServletRequest req, HttpServletResponse response){
        String path_of_photo=(String)req.getParameter("pathPhoto");
        Integer imgHeight=0;
        Integer imgWidth =0;
        try {

        if(req.getParameter("Height")!=null||req.getParameter("Width")!=null){
            imgHeight=new Integer(req.getParameter("Height"));
            imgWidth=new Integer(req.getParameter("Width"));

        }
        }
        catch (ClassCastException e){
            System.out.println("exeption in car contpoller param");
        }
        finally {


        if(req.getParameter("pathPhoto")!=null&&!req.getParameter("pathPhoto").isEmpty()){
//            path_of_photo=req.getParameter("path_of_photo");


            try {

//                FileInputStream fileInputStream=null;

                try {
                    File file=new File(path_of_photo);
                    BufferedImage bufferedImage =  ImageIO.read(file);
//
                    if(imgHeight!=0||imgWidth!=0){
                        bufferedImage = changeImgSize.resizeImage(bufferedImage, imgWidth, imgHeight);
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage,"jpg",baos);
                    baos.flush();
                    byte[] imageInByte = baos.toByteArray();
                    baos.close();

                    response.setContentType("image/jpg");
                    response.getOutputStream().write(imageInByte);
                    response.getOutputStream().flush();
                    response.getOutputStream().close();
                }

                catch(FileNotFoundException fe){
                    fe.printStackTrace();
                }
                finally {
//                    fileInputStream.close();
                }

            }
            catch (IOException r){
                System.out.println("Ошибка потока getPhoto");
            }
        }
        }
    }

    @RequestMapping(value = "/getModelByBrand",method = RequestMethod.GET, headers="Accept=application/json")
    public @ResponseBody List<String> getModelForm(@ModelAttribute("model")String model){
//        System.out.println(model);
//        CarDAO carDAO=new CarDAO();
        return carDAO.getModelByBrand(model);

    }


    @RequestMapping(value = "/ascending_price")
    public ModelAndView sortedCarsAp(HttpSession session){
        ResultCars result;
        SearchOptions options=(SearchOptions)session.getAttribute("options");
        Integer page=(Integer)session.getAttribute("page");
        if (options!=null){
            if (page==null){page=1;}
            if(options.getPrise()!=1){
            options.setPrise(1);
            result= carDAO.getCarsByParameters(options,page);
            session.setAttribute("cars",result.getCars());
            session.setAttribute("page",result.getPage());
            }
        }
        else {
            List<Car>cars = (List<Car>)session.getAttribute("cars");
            if(cars!=null){ Collections.sort(cars,(Car c1,Car c2)->c1.getPrise().compareTo(c2.getPrise()));
                session.setAttribute("cars",cars);
        }
        }
        ModelAndView modelAndView = new ModelAndView("index");
        return modelAndView;
    }

    @RequestMapping(value = "/by_prices_descending")
    public ModelAndView sortedCarsDown(HttpSession session){
        ResultCars result;
        SearchOptions options=(SearchOptions)session.getAttribute("options");
        Integer page=(Integer)session.getAttribute("page");
        if (options!=null){
            if (page==null){page=1;}
            if(options.getPrise()!=2){
                options.setPrise(2);
                result= carDAO.getCarsByParameters(options,page);
                session.setAttribute("cars",result.getCars());
                session.setAttribute("page",result.getPage());
            }
        }
        else {
        List<Car>cars = (List<Car>)session.getAttribute("cars");
        if(cars!=null){        Collections.sort(cars,(Car c1,Car c2)->-(c1.getPrise().compareTo(c2.getPrise())));
            session.setAttribute("cars",cars);
        }
        }

        ModelAndView modelAndView = new ModelAndView("index");
        return modelAndView;
    }
    @RequestMapping(value = "/addCar")
    public ModelAndView getLoginForm(){
        ModelAndView modelAndView = new ModelAndView("pageForCar");
        return modelAndView;
    }

    @RequestMapping(value = "*/update_car")
    public ModelAndView updateCarsDateProvide(@ModelAttribute("car")String id){
//        CarDAO carDAO=new CarDAO();
        carDAO.updateDataProvideDyId(new Long(EncoderId.decodeID(id)));
        ModelAndView modelAndView=new ModelAndView("my_account");
        return ViewHalper.addingDealerAndCarsInView(modelAndView);
    }
//@ModelAttribute("make")String make,@ModelAttribute("model")String model,
// @ModelAttribute("price_from")String price_from,@ModelAttribute("price_to")String price_to,
// @ModelAttribute("year_from")String year_from,@ModelAttribute("year_to")String year_to,
// @ModelAttribute("engine")String engine,@ModelAttribute("gearbox")String gearbox,
// @ModelAttribute("region")String region,
    @RequestMapping(value = "/lookForCars",method = RequestMethod.POST)
    public ModelAndView lookForCars(
            @ModelAttribute("make")String make,@ModelAttribute("model")String model,
            @ModelAttribute("price_from")String price_from,@ModelAttribute("price_to")String price_to,
            @ModelAttribute("year_from")String year_from,@ModelAttribute("year_to")String year_to,
            @ModelAttribute("engine")String engine,@ModelAttribute("gearbox")String gearbox,
            @ModelAttribute("region")String region,
            @ModelAttribute("page")String page,HttpSession session,ModelMap firstSearchOptions){
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addAllObjects(firstSearchOptions);
        if(page.isEmpty()){
            page="1";
        }
        SearchOptions options=new SearchOptions(make,model,price_from,price_to,year_from,year_to,engine,gearbox,region,0);
        ResultCars cars =carDAO.getCarsByParameters(options,new Integer(page));
        session.setAttribute("options",options);
        session.setAttribute("cars",cars.getCars());
        session.setAttribute("page",cars.getPage());
        session.setAttribute("pages", cars.getPages());
        return modelAndView;
    }
    @RequestMapping(value = "/replacing_the_page_number", method = RequestMethod.GET)
    public ModelAndView replacePage(@RequestParam("page") Integer page,HttpSession session){
        SearchOptions options=(SearchOptions)session.getAttribute("options");
        ResultCars result=carDAO.getCarsByParameters(options, page);
        session.setAttribute("page",  result.getPage());
        session.setAttribute("cars",  result.getCars());
        session.setAttribute("pages", result.getPages());
        ModelAndView modelAndView = new ModelAndView("index");
        return modelAndView;
    }
    @RequestMapping(value = {"*/addCarWithPhoto","/addCarWithPhoto"},method = RequestMethod.POST)
    public ModelAndView uploadCarsFile(@ModelAttribute("uploadForm") FileUploadForm uploadForm,Model map,
                                          @ModelAttribute("make")String make,@ModelAttribute("model")String model,
                                          @ModelAttribute("prise")String prise,@ModelAttribute("year_prov")String year_prov,
                                          @ModelAttribute("engine")String engine,@ModelAttribute("gearbox")String gearbox,
                                          @ModelAttribute("mileage")String mileage,@ModelAttribute("comment")String comment,
                                          @ModelAttribute("engine_capacity")String engine_capacity,@ModelAttribute("id_car")String idCar,
                                          @ModelAttribute("region")String region,@ModelAttribute("equipment")String equipment,HttpServletRequest request) {
        String nullMsg="Data not available.";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String idDealer = auth.getName();
        boolean update=false;
        List<String>photoPath;
        if (!idDealer.isEmpty()) {
            Car car = new Car();
//            CarDAO carDAO=new CarDAO();


            if(!idCar.isEmpty()){
              car=carDAO.getCarById(idCar);
                update=true;
                int deletePhotoNumber=0;
                List<String> paths=new ArrayList();
                photoPath=car.getPhotoPath();
                nextStep:for (String path:photoPath){

                  if(request.getParameter("photo"+deletePhotoNumber)==null)
                  {   deletePhotoNumber++;
                      continue nextStep;}
                  if(deletePhotoNumber==new Integer(request.getParameter("photo"+deletePhotoNumber))){
                      try {
                          File file = new File(path);
                          if(file.delete())//return true if file deleted
                          {paths.add(path);
                              }
                      } catch (Exception e) {
                          continue;
                      }
                  }
                  deletePhotoNumber++;
              }
                paths.forEach(p -> photoPath.remove(p));
                car.setPhotoPath(photoPath);
                 }
            else {
                car.setIdDealer(idDealer);
            }


            if (!make.isEmpty()) {
                car.setBrand(make);

            if (!model.isEmpty()) {
                car.setModel(model);

            if (!prise.isEmpty()) {
                car.setPrise(new Integer(prise));

            if (!year_prov.isEmpty()) {
                car.setYearMade(new Integer(year_prov));

            if (!engine.isEmpty()) {
                car.setEnginesType(engine);

            if (!gearbox.isEmpty()) {
                car.setTransmission(gearbox);

                if (!comment.isEmpty())car.setDescription(comment);
                if (!equipment.isEmpty())car.setEquipment(equipment);
                if(!region.isEmpty())car.setRegion(region);
                if(!engine_capacity.isEmpty()){car.setEngineCapacity(engine_capacity);}
                else {car.setEngineCapacity(nullMsg);}
                if(!mileage.isEmpty()){
                    car.setMileage(new Integer(mileage));
                }
                else car.setMileage(0);
             if(!update){
                if (carDAO.setCar(car, uploadForm.getFiles()) != -1L) {
                    ModelAndView modelAndView=new ModelAndView("my_account");
                    modelAndView.addObject("msg", standartMasege.getMessage(1));
//                    DealerDao dealerDao = new DealerDao();
                    dealerDao.updateCountOfCar(car.getIdDealer());
                    return ViewHalper.addingDealerAndCarsInView(modelAndView);
                }
             }
                if (update){
                    if (carDAO.updateCarById(car,uploadForm.getFiles())){
                        ModelAndView modelAndView=new ModelAndView("my_account");
                        modelAndView.addObject("msg", standartMasege.getMessage(1));
                        return ViewHalper.addingDealerAndCarsInView(modelAndView);
                    }
                }

            }}}}}}

        }

        ModelAndView modelAndView=new ModelAndView("pageForCar");

        modelAndView.addObject("msg", standartMasege.getMessage(2));
        return modelAndView;

    }
    @RequestMapping(value = "*/change_car",method = RequestMethod.GET)
    public ModelAndView responseCar(@ModelAttribute("car")String carId){
//        CarDAO carDAO = new CarDAO();
        Car car=carDAO.getCarById(EncoderId.decodeID(carId));
        ModelAndView modelAndView=new ModelAndView("pageForCar");
        modelAndView.addObject("car",car);
        return modelAndView;
    }
    @RequestMapping(value = "*/delete_car",method = RequestMethod.GET)
    public ModelAndView deleteCar(@ModelAttribute("car")String car){
//        CarDAO carDAO = new CarDAO();
        ModelAndView modelAndView=new ModelAndView("my_account");
        if(carDAO.deleteCarById(EncoderId.decodeID(car))){
            modelAndView.addObject("msg",standartMasege.getMessage(20));
            return ViewHalper.addingDealerAndCarsInView(modelAndView);
        }
        else {
            modelAndView.addObject("msg",standartMasege.getMessage(21));
            return ViewHalper.addingDealerAndCarsInView(modelAndView);
        }

    }
    @RequestMapping(value = "*/change_car",method = RequestMethod.POST)
    public ModelAndView changeCar(@ModelAttribute("car")String car){
//        CarDAO carDAO = new CarDAO();
        Car car1=carDAO.getCarById(EncoderId.decodeID(car));
        ModelAndView modelAndView=new ModelAndView("pageForCar");
        modelAndView.addObject("car",car);
        return modelAndView;
    }

}
