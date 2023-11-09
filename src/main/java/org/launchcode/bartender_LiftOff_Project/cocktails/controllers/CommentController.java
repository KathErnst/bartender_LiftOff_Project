package org.launchcode.bartender_LiftOff_Project.cocktails.controllers;

import org.launchcode.bartender_LiftOff_Project.cocktails.data.RecipeRepository;
import org.launchcode.bartender_LiftOff_Project.cocktails.models.Comment;
import org.launchcode.bartender_LiftOff_Project.cocktails.models.Recipe;
import org.launchcode.bartender_LiftOff_Project.controllers.AuthenticationController;
import org.launchcode.bartender_LiftOff_Project.data.CommentRepository;
import org.launchcode.bartender_LiftOff_Project.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private AuthenticationController authenticationController;

    @PostMapping("/cocktails/recipe/comment")
    public String processAddCommentForm(@RequestParam Integer recipeId,
                                        @RequestParam String commentContents,
                                        @ModelAttribute Comment newComment,
                                        HttpServletRequest request, Model model) {
        try {
            HttpSession session = request.getSession();
            User user = authenticationController.getUserFromSession(session);
            Optional<Recipe> result = recipeRepository.findById(recipeId);

            if (user == null) {
                model.addAttribute("errorMessage", "User not found");
                return "error";
            }

            newComment.setDateAdded(LocalDate.now());
            newComment.setUserName(user);
            newComment.setContents(commentContents);

            Recipe recipe = result.get();
            recipe.addComment(newComment);
            commentRepository.save(newComment);

            return "redirect:/cocktails/recipe?recipeId=" + recipeId;
        }

            catch (Exception e) {
                model.addAttribute("title", "Error");
                model.addAttribute("errorMessage", "There seems to be a problem");
                e.printStackTrace();
                return "error";
                }

    }

    @GetMapping("/cocktails/recipe/comment/edit")
//    public String renderEditCommentForm(@RequestParam Integer commentId,
//                                        @ModelAttribute Comment oldComment,
//                                        HttpServletRequest request, Model model) {
//        HttpSession session = request.getSession();
//        User user = authenticationController.getUserFromSession(session);
//        Optional<Comment> result = commentRepository.findById(commentId);
//
//        if (user == null) {
//            model.addAttribute("errorMessage", "User not found");
//            return "error";
//        }
// Get comment contents in a text box that can be edited
    public String renderEditCommentForm (@RequestParam Integer commentId,
                                         Model model) {
        Optional<Comment> oldComment = commentRepository.findById(commentId);
        String commentTextToEdit = oldComment.get().getContents();
        model.addAttribute(commentTextToEdit);
        return "/cocktails/editComment";
    }






}
