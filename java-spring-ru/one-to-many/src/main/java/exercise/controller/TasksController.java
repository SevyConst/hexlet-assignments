package exercise.controller;

import java.util.List;

import exercise.dto.TaskCreateDTO;
import exercise.dto.TaskDTO;
import exercise.dto.TaskUpdateDTO;
import exercise.mapper.TaskMapper;
import exercise.model.Task;
import exercise.model.User;
import exercise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import exercise.exception.ResourceNotFoundException;
import exercise.repository.TaskRepository;

@RestController
@RequestMapping("/tasks")
public class TasksController {
    // BEGIN
    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskMapper taskMapper;

    @GetMapping
    List<TaskDTO> index() {
        return taskRepository.findAll().stream().map(taskMapper::map).toList();
    }

    @GetMapping(path = "/{id}")
    TaskDTO show(@PathVariable long id) {
        return taskRepository.findById(id).
                map(taskMapper::map).
                orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TaskDTO create(@RequestBody TaskCreateDTO taskCreateDTO) {
        long assigneeId = taskCreateDTO.getAssigneeId();
        User user = userRepository.findById(assigneeId).
                orElseThrow(() -> new ResourceNotFoundException("User with id " + assigneeId + " not found"));
        Task task = taskMapper.map(taskCreateDTO);
        user.addTask(task);
        userRepository.save(user);
        return taskMapper.map(taskRepository.findById(task.getId()).get());
    }

    @PutMapping(path = "/{id}")
    TaskDTO update(@PathVariable long id, @RequestBody TaskUpdateDTO taskUpdateDTO) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        long assigneeId = taskUpdateDTO.getAssigneeId();
        User user = userRepository.findById(assigneeId).
                orElseThrow(() -> new ResourceNotFoundException("User with id " + assigneeId + " not found"));
        taskMapper.update(taskUpdateDTO, task);
        user.addTask(task);
        userRepository.save(user);
        return taskMapper.map(taskRepository.findById(task.getId()).get());
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        User user = userRepository.findById(task.getAssignee().getId()).get();
        user.removeTask(task);
        userRepository.save(user);
    }

    // END
}
