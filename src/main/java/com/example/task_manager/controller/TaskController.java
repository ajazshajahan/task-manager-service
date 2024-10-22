package com.example.task_manager.controller;

import com.example.task_manager.dto.ProjectDTO;
import com.example.task_manager.dto.TaskDTO;
import com.example.task_manager.enums.ProjectStatus;
import com.example.task_manager.enums.TaskStatus;
import com.example.task_manager.response.AppResponse;
import com.example.task_manager.response.PaginatedResponse;
import com.example.task_manager.response.ProjectResponse;
import com.example.task_manager.response.TaskResponse;
import com.example.task_manager.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.example.task_manager.constant.AppConstant.FAILED;
import static com.example.task_manager.constant.AppConstant.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class TaskController {


    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/save/task")
    public ResponseEntity<AppResponse<Void>> saveTask(@RequestBody TaskDTO request){

        taskService.createTask(request);
        AppResponse<Void> appResponse = new AppResponse<>(null, "SUCCESS", "Project created Successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(appResponse);
    }

    @GetMapping("/get/task")
    public ResponseEntity<AppResponse<PaginatedResponse<TaskResponse>>> getTask(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "companyId", required = false) Long companyId,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "updatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime updatedAt,
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {

        log.info("Received request to fetch project data with id: {}, companyId: {}, title: {}, updatedAt: {}, status: {}, startDate: {}, endDate: {}, page: {}, size: {}, sortBy: {}, sortOrder: {}",
                id, companyId, title, updatedAt, status, startDate, endDate, page, size, sortBy, sortOrder);

        try {
            // Call the service method which now returns PaginatedResponse<ProjectResponse>
            PaginatedResponse<TaskResponse> response = taskService.getTask(id, companyId, title, updatedAt, status, startDate, endDate, page, size, sortBy, sortOrder);

            if (response.getData().isEmpty()) {
                log.warn("No projects found for filters: id: {}, companyId: {}, name: {}, updatedAt: {}, status: {}, startDate: {}, endDate: {}",
                        id, companyId, title, updatedAt, status, startDate, endDate);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        AppResponse.<PaginatedResponse<TaskResponse>>builder()
                                .status(FAILED)
                                .message("No projects found")
                                .build()
                );
            }

            log.info("Successfully fetched project data: {}", response);
            return ResponseEntity.ok(
                    AppResponse.<PaginatedResponse<TaskResponse>>builder()
                            .data(response)
                            .status(SUCCESS)
                            .message("Project data fetched successfully")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to fetch project data for id: {}, companyId: {}, name: {}, updatedAt: {}, status: {}, startDate: {}, endDate: {}, page: {}, size: {}, sortBy: {}, sortOrder: {}",
                    id, companyId, title, updatedAt, status, startDate, endDate, page, size, sortBy, sortOrder, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AppResponse.<PaginatedResponse<TaskResponse>>builder()
                            .status(FAILED)
                            .message("Failed to fetch project data: " + e.getMessage())
                            .build()
            );
        }
    }


    @PutMapping("/update/task")
    public ResponseEntity<AppResponse<TaskResponse>> updateTask(@RequestBody TaskDTO taskDTO) {

        AppResponse<TaskResponse> appResponse = new AppResponse<>();

        try {


            TaskResponse taskResponse = taskService.updateTask(taskDTO);


            if (taskResponse == null) {
                appResponse.setStatus("FAILED");
                appResponse.setMessage("Task not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(appResponse);
            }

            appResponse.setData(taskResponse);
            appResponse.setStatus("SUCCESS");
            appResponse.setMessage("Project found successfully");

            return ResponseEntity.ok(appResponse);
        } catch (Exception ex) {
            appResponse.setStatus("FAILED");
            appResponse.setMessage("An error occurred while fetching the project: " + ex.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(appResponse);
        }
    }



    @DeleteMapping("/delete/task")
    public ResponseEntity<String> deleteTask(@RequestParam(name = "id", required = false) Long id) {

        return taskService.deleteTask(id);
    }
}