package org.launchcode.bartender_LiftOff_Project.cocktails.controllers;

import net.bytebuddy.asm.Advice;
import org.launchcode.bartender_LiftOff_Project.cocktails.data.CocktailRepository;
import org.launchcode.bartender_LiftOff_Project.cocktails.data.RecipeRepository;
import org.launchcode.bartender_LiftOff_Project.cocktails.data.IngredientRepository;
import org.launchcode.bartender_LiftOff_Project.cocktails.models.Cocktail;
import org.launchcode.bartender_LiftOff_Project.cocktails.models.Ingredient;
import org.launchcode.bartender_LiftOff_Project.cocktails.models.Recipe;
import org.launchcode.bartender_LiftOff_Project.controllers.AuthenticationController;
import org.launchcode.bartender_LiftOff_Project.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.Errors;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Controller
@SessionAttributes("recipe")
@RequestMapping("cocktails")
public class CocktailController {

    @Autowired
    private CocktailRepository cocktailRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private AuthenticationController authenticationController;

    @GetMapping
    public String displayCocktails(Model model, HttpServletRequest request) {
        model.addAttribute("title", "Cocktail Recipes");

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        model.addAttribute("recipes", recipeRepository.findRecipesCreatedAfterOrderByDateAddedDesc(startDate));

        HttpSession session = request.getSession();
        User user = authenticationController.getUserFromSession(session);

        if (user != null) {
            List<Recipe> userRecipes = user.getCreatedRecipes();
            model.addAttribute("userRecipes", userRecipes);
        }

        return "cocktails/index";
    }

    @GetMapping("create")
    public String displayCreateCocktailForm(Model model, HttpServletRequest request) {
        model.addAttribute("title", "Create New Cocktail Recipe");
        HttpSession session = request.getSession();
        User user = authenticationController.getUserFromSession(session);

        Recipe recipe = new Recipe();
        recipe.setCocktail(new Cocktail());
        recipe.setAuthor(user);

        model.addAttribute("recipe", recipe);

        return "cocktails/create";
    }

    @PostMapping(value = {"create", "edit"}, params = {"addIngredient"})
    public String addIngredient(Model model, Recipe recipe, HttpServletRequest request){
        String path = request.getServletPath();

        if(null!=recipe){
            if(null==recipe.getIngredients()){
                recipe.getIngredients().add(new Ingredient());
            } else {
                recipe.getIngredients().add(new Ingredient());
            }
        }

        if (path.endsWith("edit")) {
            model.addAttribute("title", "Edit " + recipe.getAuthor().getUsername() + "'s " + recipe.getCocktail().getName());
            return "cocktails/edit";
        }
        else {
            model.addAttribute("title", "Create New Cocktail Recipe");
            return "cocktails/create";
        }
    }

    @PostMapping(value = {"create", "edit"}, params = {"removeIngredient"})
    public String removeIngredient(Model model, Recipe recipe, HttpServletRequest request) {
        String path = request.getServletPath();
        recipe.getIngredients().remove(Integer.parseInt(request.getParameter("removeIngredient")));

        if (path.endsWith("edit")) {
            model.addAttribute("title", "Edit " + recipe.getAuthor().getUsername() + "'s " + recipe.getCocktail().getName());
            return "cocktails/edit";
        }
        else {
            model.addAttribute("title", "Create New Cocktail Recipe");
            return "cocktails/create";
        }
    }

    @PostMapping("create")
    public String processCreateCocktailForm(Model model, @ModelAttribute @Valid Recipe recipe, Errors errors, SessionStatus status){
        List<Ingredient> ingredientList = recipe.getIngredients();

        if (errors.hasErrors()) {
            model.addAttribute("title", "Create New Cocktail Recipe");
            return "cocktails/create";
        }
        else {
            //Check if cocktail already exists; if so, add recipe to list. Otherwise, create new cocktail & add recipe
            Optional<Cocktail> existingCocktail = cocktailRepository.findByNameIgnoreCase(recipe.getCocktail().getName());
            if (existingCocktail.isPresent()) {
                recipe.setCocktail(existingCocktail.get());
            }

            //checking for duplicate ingredients; if found, replace with existing
            for (int i = 0; i < ingredientList.size(); i++) {
                Ingredient ingredient = ingredientList.get(i);
                Optional<Ingredient> existingIngredient = ingredientRepository.findByNameIgnoreCase(ingredient.getName());
                if (existingIngredient.isPresent()) {
                    ingredientList.remove(ingredient);
                    ingredientList.add(i, existingIngredient.get());
                }
            }

            recipeRepository.save(recipe);

            status.setComplete();
            return "redirect:recipe?recipeId=" + recipe.getId();
        }
    }

    @GetMapping("recipe")
    public String displayCocktailRecipe(@RequestParam Integer recipeId, Model model) {
        Optional<Recipe> result = recipeRepository.findById(recipeId);

        if (result.isEmpty()) {
            model.addAttribute("title", "Invalid ID");
            model.addAttribute("errorMessage", "Recipe not found");
            return "error";
        } else {
            Recipe recipe = result.get();
            model.addAttribute("title", recipe.getCocktail().getName() + " Recipe");
            model.addAttribute("recipe", recipe);
            model.addAttribute("ingredients", recipe.getIngredients());
        }

        return "cocktails/recipe";
    }

    @GetMapping("edit")
    public String displayEditRecipeForm(@RequestParam Integer recipeId, Model model) {
        Optional<Recipe> result = recipeRepository.findById(recipeId);

        if (result.isEmpty()) {
            model.addAttribute("errorMessage", "Recipe not found");
            return "error";
        }
        else {
            Recipe recipe = result.get();
            model.addAttribute("title", "Edit " + recipe.getAuthor().getUsername() + "'s " + recipe.getCocktail().getName());
            model.addAttribute("recipe", recipe);
        }
        return "cocktails/edit";
    }
}
