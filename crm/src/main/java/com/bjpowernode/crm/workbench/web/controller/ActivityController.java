package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.commons.contants.Contacts;
import com.bjpowernode.crm.commons.domain.ReturnObject;
import com.bjpowernode.crm.commons.utils.DateUtils;
import com.bjpowernode.crm.commons.utils.HSSFUtils;
import com.bjpowernode.crm.commons.utils.UUIDUtils;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.ActivityRemark;
import com.bjpowernode.crm.workbench.mapper.ActivityRemarkMapper;
import com.bjpowernode.crm.workbench.service.ActivityRemarkService;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@Controller
public class ActivityController {
    @Resource
    private UserService userService;
    @Resource
    private ActivityService activityService;
    @Resource
    private ActivityRemarkService activityRemarkService;

    @RequestMapping("/workbench/activity/index.do")
    public String index(HttpServletRequest request){
        List<User> users = userService.queryAllUsers();

        request.setAttribute("userList",users);

        return "workbench/activity/index";
    }
    @RequestMapping("/workbench/activity/saveCreateActivity.do")
    public @ResponseBody
    Object saveCreateActivity(Activity activity, HttpSession session){
        User user= (User) session.getAttribute(Contacts.SESSION_USER);
        //????????????
        activity.setId(UUIDUtils.getUUID());
        activity.setCreateTime(DateUtils.formateDateTime(new Date()));
        activity.setCreateBy(user.getId());


        ReturnObject returnObject=new ReturnObject();
        try {
            //??????service
            int ret = activityService.saveCreateActivity(activity);
            if(ret>0){
                returnObject.setCode(Contacts.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Contacts.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("???????????????????????????");
            }
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Contacts.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("???????????????????????????");
        }

        return returnObject;



    }
    @RequestMapping("/workbench/activity/queryActivityByConditionForPage.do")
    public @ResponseBody Object queryActivityByConditionForPage(String name,String owner,String startDate,String endDate,
    int pageNo,int pageSize){
        //????????????
        Map<String,Object> map=new HashMap<>();
        map.put("name",name);
        map.put("owner",owner);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("beginNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        //??????service??????
        List<Activity> activityList = activityService.queryActivityByConditionForPage(map);
        int totalRows = activityService.queryCountOfActivityByCondition(map);
        //???????????????????????????????????????
        Map<String,Object> retMap=new HashMap<>();
        retMap.put("activityList",activityList);
        retMap.put("totalRows",totalRows);
        return retMap;
    }
    @RequestMapping("/workbench/activity/deleteActivityByIds.do")
    public @ResponseBody Object deleteActivityIds(String[] id){
        ReturnObject returnObject=new ReturnObject();
        //??????service??????????????????
        try {
            int ret = activityService.deleteActivityByIds(id);
            if(ret>0){
                returnObject.setCode(Contacts.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Contacts.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("????????????????????????");
            }
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Contacts.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("????????????????????????");
        }
        return returnObject;


    }
    @RequestMapping("/workbench/activity/queryActivityById.do")
    public @ResponseBody Object queryActivityById(String id){
        //??????service???????????????????????????
        Activity activity = activityService.queryActivityById(id);

        return activity;
    }
    @RequestMapping("/workbench/activity/saveEditActivity.do")
    public @ResponseBody Object saveEditActivity(Activity activity,HttpSession session){
        //????????????
        User user=(User)session.getAttribute(Contacts.SESSION_USER);
        activity.setEditTime(DateUtils.formateDateTime(new Date()));
        activity.setEditBy(user.getId());

        ReturnObject returnObject = new ReturnObject();
        //??????service???????????????????????????????????????
        try {
            int ret=activityService.saveEditActivity(activity);
            if (ret>0){
                returnObject.setCode(Contacts.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Contacts.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("???????????????????????????");
            }
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Contacts.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("???????????????????????????");
        }
        return returnObject;
    }
    @RequestMapping("/workbench/activity/fileDownload.do")
    public void fileDownload(HttpServletResponse response) throws IOException {
        //??????????????????
        response.setContentType("application/octet-stream;charset=UTF-8");
        //???????????????
        OutputStream out=response.getOutputStream();


        //???????????????????????????????????????????????????????????????????????????????????????????????????
        response.addHeader("Content-Disposition","attachment;filename=mystu.xls");




        //??????excel?????????inputstream??????????????????????????????outputstream???
        InputStream is=new FileInputStream("D:\\idea\\other\\file\\stu.xml");
        byte[] buff=new byte[256];

        int len=0;
        while ((len=is.read(buff))!=-1){
            out.write(buff,0,len);
        }
        is.close();
        out.flush();


    }
    @RequestMapping("/workbench/activity/exportAllActivitys.do")
    public void exportAllActivitys(HttpServletResponse response) throws IOException {
        //??????service???????????????????????????????????????
        List<Activity> activityList=activityService.queryAllActivitys();
        //??????exel??????????????????ac????????????excel?????????
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("??????????????????");
        HSSFRow row=sheet.createRow(0);
        HSSFCell cell=row.createCell(0);
        cell.setCellValue("ID");
        cell=row.createCell(1);
        cell.setCellValue("?????????");
        cell=row.createCell(2);
        cell.setCellValue("??????");
        cell=row.createCell(3);
        cell.setCellValue("????????????");
        cell=row.createCell(4);
        cell.setCellValue("????????????");
        cell=row.createCell(5);
        cell.setCellValue("??????");
        cell=row.createCell(6);
        cell.setCellValue("??????");
        cell=row.createCell(7);
        cell.setCellValue("????????????");
        cell=row.createCell(8);
        cell.setCellValue("?????????");
        cell=row.createCell(9);
        cell.setCellValue("????????????");
        cell=row.createCell(10);
        cell.setCellValue("?????????");



        if (activityList!=null&&activityList.size()>0) {
            //??????activityList?????????HSSFRow?????????????????????????????????
            Activity activity = null;
            for (int i = 0; i < activityList.size(); i++) {
                activity = activityList.get(i);
                //??????????????????activity???????????????
                row = sheet.createRow(i + 1);


                cell = row.createCell(0);
                cell.setCellValue(activity.getId());
                cell = row.createCell(1);
                cell.setCellValue(activity.getOwner());
                cell = row.createCell(2);
                cell.setCellValue(activity.getName());
                cell = row.createCell(3);
                cell.setCellValue(activity.getStartDate());
                cell = row.createCell(4);
                cell.setCellValue(activity.getEndDate());
                cell = row.createCell(5);
                cell.setCellValue(activity.getCost());
                cell = row.createCell(6);
                cell.setCellValue(activity.getDescription());
                cell = row.createCell(7);
                cell.setCellValue(activity.getCreateTime());
                cell = row.createCell(8);
                cell.setCellValue(activity.getCreateBy());
                cell = row.createCell(9);
                cell.setCellValue(activity.getEditTime());
                cell = row.createCell(10);
                cell.setCellValue(activity.getEditBy());


            }
        }

        //??????web????????????excel??????
//        OutputStream os=new FileOutputStream("D:\\idea\\other\\file\\activityList.xls");
//        wb.write(os);

        wb.close();

        //????????????????????????????????????
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.addHeader("Content-Disposition","attachment;filename=activityList.xls");
        OutputStream out=response.getOutputStream();
//        InputStream is=new FileInputStream("D:\\idea\\other\\file\\activityList.xls");
//        byte[] buff=new byte[256];
//        int len=0;
//        while ((len=is.read(buff))!=-1){
//            out.write(buff,0,len);
//        }
//        is.close();

        wb.write(out);
        wb.close();
        out.flush();
    }
    @RequestMapping("/workbench/activity/importActivity.do")
    public @ResponseBody Object importActivity(MultipartFile activityFile,String username,HttpSession session){
        User user = (User) session.getAttribute(Contacts.SESSION_USER);
        ReturnObject returnObject=new ReturnObject();
        try{
            //???excel???????????????????????????
//            String originalFilename = activityFile.getOriginalFilename();
//            File file = new File("D:\\idea\\other\\file\\", originalFilename);
//            activityFile.transferTo(file);

            //??????excel???????????????????????????????????????????????????activityList
//            InputStream is=new FileInputStream("D:\\idea\\other\\file\\"+originalFilename);
            InputStream is=activityFile.getInputStream();



            HSSFWorkbook wb=new HSSFWorkbook(is);
            HSSFSheet sheet = wb.getSheetAt(0);

            HSSFRow row=null;
            HSSFCell cell=null;
            Activity activity=null;
            List<Activity> activityList=new ArrayList<>();
            for(int i=1;i<=sheet.getLastRowNum();i++){
                row=sheet.getRow(i);
                activity = new Activity();
                activity.setId(UUIDUtils.getUUID());
                activity.setOwner(user.getId());
                activity.setCreateTime(DateUtils.formateDateTime(new Date()));
                activity.setCreateBy(user.getId());


                for (int j=0;j<row.getLastCellNum();j++){
                    cell=row.getCell(j);

                    String cellValue= HSSFUtils.getCellValueForStr(cell);
                    if(j==0){
                        activity.setName(cellValue);
                    }else if(j==1){
                        activity.setStartDate(cellValue);
                    }else if(j==2){
                        activity.setEndDate(cellValue);
                    }else if(j==3){
                        activity.setCost(cellValue);
                    }else if(j==4){
                        activity.setDescription(cellValue);
                    }

                }
                //??????????????????????????????
                activityList.add(activity);
            }
            //??????serviece?????????
            int ret=activityService.saveCreateActivityByList(activityList);

            returnObject.setCode(Contacts.RETURN_OBJECT_CODE_SUCCESS);
            returnObject.setReData(ret);
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Contacts.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("????????????????????????????????????");
        }
        return returnObject;
    }
    @RequestMapping("/workbench/activity/detailActivity.do")
    public String detailActivity(String id, HttpServletRequest request){
        //??????service????????????????????????
        Activity activity=activityService.queryActivityForDetailById(id);
        List<ActivityRemark> remarkList=activityRemarkService.queryActivityRemarkForDetailByActivityId(id);
        //??????????????????request???
        request.setAttribute("activity",activity);
        request.setAttribute("remarkList",remarkList);
        //??????????????????????????????
        return "workbench/activity/detail";
    }


}
