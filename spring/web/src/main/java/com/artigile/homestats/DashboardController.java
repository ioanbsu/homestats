package com.artigile.homestats;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by ibahdanau on 2/24/16.
 */
@Controller
public class DashboardController {
    @RequestMapping("/dashboard")
    public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name,
                           Model model) {
        model.addAttribute("name", name);
        return "dashboard";
    }
}
