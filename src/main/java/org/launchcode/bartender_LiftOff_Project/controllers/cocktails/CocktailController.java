package org.launchcode.bartender_LiftOff_Project.controllers.cocktails;

import org.launchcode.bartender_LiftOff_Project.data.cocktails.CocktailRepository;
import org.launchcode.bartender_LiftOff_Project.models.cocktails.Cocktail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.Errors;

import javax.validation.Valid;

@Controller
@RequestMapping("cocktails")
public class CocktailController {

    @Autowired
    private CocktailRepository cocktailRepository;


    @GetMapping
    public String displayCocktails(@RequestParam(required = false) Integer ingredientId, Model model) {
        if (ingredientId == null) {
            model.addAttribute("title", "Recently Added Cocktails");
            model.addAttribute("cocktails", cocktailRepository.findAll());
        }

        return "cocktails/index";
    }
    @GetMapping("create")
    public String displayCreateCocktailForm(Model model) {
        model.addAttribute("title", "Create New Cocktail Recipe");
        model.addAttribute(new Cocktail());

        return "cocktails/create";
    }

    @PostMapping("create")
    public String processCreateCocktailForm(@ModelAttribute @Valid Cocktail newCocktail, Errors error, Model model) {
        if (error.hasErrors()) {
            model.addAttribute("title", "Create New Cocktail Recipe");
            return "cocktails/create";
        }

        cocktailRepository.save(newCocktail);
        return "redirect:";
    }

}
