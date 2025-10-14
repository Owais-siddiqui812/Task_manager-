package com.example.taskmanager.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TaskController {
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public TaskController(TaskRepository taskRepo, UserRepository userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    private Long currentUserId(HttpSession session) {
        Object id = session.getAttribute("userId");
        return id == null ? null : (Long) id;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        Long uid = currentUserId(session);
        if (uid == null) return "redirect:/login";
        List<Task> tasks = taskRepo.findByUserId(uid);
        model.addAttribute("tasks", tasks);
        return "index";
    }

    @PostMapping("/add")
    public String add(@RequestParam String task, HttpSession session) {
        Long uid = currentUserId(session);
        if (uid == null) return "redirect:/login";
        User u = userRepo.findById(uid).orElse(null);
        if (u == null) return "redirect:/login";

        Task t = new Task();
        t.setTitle(task);
        t.setUser(u);
        taskRepo.save(t);
        return "redirect:/";
    }

    @GetMapping("/delete/{taskId}")
    public String delete(@PathVariable Long taskId, HttpSession session) {
        Long uid = currentUserId(session);
        if (uid == null) return "redirect:/login";
        var opt = taskRepo.findById(taskId);
        if (opt.isPresent() && opt.get().getUser().getId().equals(uid)) {
            taskRepo.delete(opt.get());
        }
        return "redirect:/";
    }

    @GetMapping("/completed/{taskId}")
    public String completed(@PathVariable Long taskId, HttpSession session) {
        Long uid = currentUserId(session);
        if (uid == null) return "redirect:/login";
        var opt = taskRepo.findById(taskId);
        if (opt.isPresent() && opt.get().getUser().getId().equals(uid)) {
            Task t = opt.get();
            t.setCompleted(true);
            taskRepo.save(t);
        }
        return "redirect:/";
    }

    @PostMapping("/clear")
    public String clear(HttpSession session) {
        Long uid = currentUserId(session);
        if (uid == null) return "redirect:/login";
        taskRepo.deleteByUserId(uid);
        return "redirect:/";
    }
}
