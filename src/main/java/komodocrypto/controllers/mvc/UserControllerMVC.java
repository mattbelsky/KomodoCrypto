//package komodocrypto.controllers.mvc;
//
//import komodocrypto.exceptions.custom_exceptions.UserException;
//import komodocrypto.model.arbitrage.ArbitrageModel;
//import komodocrypto.model.user.User;
//import komodocrypto.services.arbitrage.ArbitrageTradingService;
//import komodocrypto.services.users.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.validation.Valid;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//
//@Controller
//@RequestMapping("/komodo")
//public class UserControllerMVC {
//
//    @Autowired
//    ArbitrageTradingService arbitrageTradingService;
//
//    @Autowired
//    UserService userService;
//
//    @RequestMapping(value ={"/", "/home"}, method = RequestMethod.GET)
//    public String home(Model model){
///*        ArbitrageModel am = arbitrageTradingService.getArbitrageOps();
//            am.getCurrencyPair() will replace the attribute value on the addAttribute method
//
// */
//        ArbitrageModel am = new ArbitrageModel();
//        am.setCurrencyPair("BTC/ETH");
//        am.setHighBid(BigDecimal.valueOf(0.856740));
//        am.setLowAsk(BigDecimal.valueOf(0.754602));
//        am.setDifference(BigDecimal.valueOf(0.102138));
//
//        model.addAttribute("currencyPair", am.getCurrencyPair());
//        model.addAttribute("highBid", am.getHighBid());
//        model.addAttribute("lowAsk", am.getLowAsk());
//        model.addAttribute("difference", am.getDifference());
//        return "home";
//    }
//    @RequestMapping(value={"/user"}, method = RequestMethod.GET)
//    public ModelAndView user(){
//        ModelAndView modelAndView = new ModelAndView();
//        ArrayList<ArbitrageModel> arbitrage = arbitrageTradingService.getArbitrageData();
//        modelAndView.addObject("arbitrage", arbitrage);
//        /*will need to work with ouath to get username*/
//        User user = arbitrageTradingService.createTempUser();
//        modelAndView.addObject("userWelcome", "Welcome " + user.getFirst_name());
//        modelAndView.setViewName("user_dashboard");
//        return modelAndView;
//    }
//
//    @RequestMapping(value = "/registration", method = RequestMethod.GET)
//    public ModelAndView registration() {
//        ModelAndView modelAndView = new ModelAndView();
//        User user = new User();
//        modelAndView.addObject("user", user);
//        modelAndView.setViewName("registration");
//
//        return modelAndView;
//    }
//
//    @RequestMapping(value = "/profile", method = RequestMethod.GET)
//    public ModelAndView profile() {
//        ModelAndView modelAndView = new ModelAndView();
//        //for when oath is added use:
//        //User user = new User();
//        User user = arbitrageTradingService.createTempUser();
//        modelAndView.addObject("user", user);
//        modelAndView.addObject("userWelcome", "Welcome " + user.getFirst_name());
//        modelAndView.setViewName("profile");
//
//        return modelAndView;
//    }
//
//    @RequestMapping(value = "/registration", method = RequestMethod.POST)
//    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) throws UserException {
//        ModelAndView modelAndView = new ModelAndView();
//
//        User userExists = userService.getUserByID(user.getUser_id());
//        if (userExists != null) {
//            bindingResult
//                    .rejectValue("name", "error.user",
//                            "There is already a user registered with that name");
//        }
//
//        if (bindingResult.hasErrors()) {
//            modelAndView.setViewName("registration");
//        } else {
//            userService.createUser(user);
//            modelAndView.addObject("successMessage", "User has been registered successfully");
//            modelAndView.addObject("user", new User());
//            modelAndView.setViewName("registration");
//        }
//        return modelAndView;
//    }
//
//}
