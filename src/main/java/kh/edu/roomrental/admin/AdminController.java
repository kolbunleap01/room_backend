package kh.edu.roomrental.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kh.edu.roomrental.rentalrequest.RentalRequestDtos;
import kh.edu.roomrental.room.RoomDtos;
import kh.edu.roomrental.room.RoomStatus;
import kh.edu.roomrental.user.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public AdminDtos.DashboardResponse dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/users")
    public Page<AdminDtos.UserSummary> users(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.users(pageable);
    }

    @PatchMapping("/users/{userId}/status")
    public AdminDtos.UserSummary setUserStatus(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable UUID userId,
                                               @Valid @RequestBody AdminDtos.UserStatusRequest request,
                                               HttpServletRequest httpRequest) {
        return adminService.setUserEnabled(principal.getId(), userId, request.enabled(),
                httpRequest.getRemoteAddr());
    }

    @PostMapping("/users/{userId}/activate")
    public AdminDtos.UserSummary activateUser(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable UUID userId, HttpServletRequest httpRequest) {
        return adminService.setUserEnabled(principal.getId(), userId, true, httpRequest.getRemoteAddr());
    }

    @PostMapping("/users/{userId}/deactivate")
    public AdminDtos.UserSummary deactivateUser(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable UUID userId, HttpServletRequest httpRequest) {
        return adminService.setUserEnabled(principal.getId(), userId, false, httpRequest.getRemoteAddr());
    }

    @GetMapping("/rooms")
    public Page<RoomDtos.RoomSummary> rooms(@RequestParam(required = false) RoomStatus status,
                                            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                            Pageable pageable) {
        return adminService.rooms(status, pageable);
    }

    @PatchMapping("/rooms/{roomId}")
    public RoomDtos.RoomResponse updateRoom(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable UUID roomId,
                                            @Valid @RequestBody RoomDtos.UpdateRoomRequest request,
                                            HttpServletRequest httpRequest) {
        return adminService.updateRoom(principal.getId(), roomId, request, httpRequest.getRemoteAddr());
    }

    @DeleteMapping("/rooms/{roomId}")
    public org.springframework.http.ResponseEntity<Void> deactivateRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId,
            HttpServletRequest request) {
        adminService.deactivateRoom(principal.getId(), roomId, request.getRemoteAddr());
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomId}/deactivate")
    public org.springframework.http.ResponseEntity<Void> deactivateRoomAlias(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId, HttpServletRequest request) {
        adminService.deactivateRoom(principal.getId(), roomId, request.getRemoteAddr());
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @DeleteMapping("/rooms/{roomId}/permanent")
    public org.springframework.http.ResponseEntity<Void> deleteRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId, HttpServletRequest request) {
        adminService.deleteRoom(principal.getId(), roomId, request.getRemoteAddr());
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @DeleteMapping("/rooms/{roomId}/delete")
    public org.springframework.http.ResponseEntity<Void> deleteRoomAlias(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId, HttpServletRequest request) {
        adminService.deleteRoom(principal.getId(), roomId, request.getRemoteAddr());
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @GetMapping("/rental-requests")
    public Page<RentalRequestDtos.Response> rentalRequests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.rentalRequests(pageable);
    }

    @PostMapping("/rental-requests/{requestId}/approve")
    public RentalRequestDtos.Response approveRentalRequest(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable UUID requestId,
                                                           @Valid @RequestBody(required = false)
                                                           RentalRequestDtos.DecisionRequest request,
                                                           HttpServletRequest httpRequest) {
        return adminService.decideRentalRequest(principal.getId(), requestId, true, request,
                httpRequest.getRemoteAddr());
    }

    @PostMapping("/rental-requests/{requestId}/reject")
    public RentalRequestDtos.Response rejectRentalRequest(@AuthenticationPrincipal UserPrincipal principal,
                                                          @PathVariable UUID requestId,
                                                          @Valid @RequestBody(required = false)
                                                          RentalRequestDtos.DecisionRequest request,
                                                          HttpServletRequest httpRequest) {
        return adminService.decideRentalRequest(principal.getId(), requestId, false, request,
                httpRequest.getRemoteAddr());
    }

    @PostMapping("/rental-requests/{requestId}/cancel")
    public org.springframework.http.ResponseEntity<Void> cancelRentalRequest(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID requestId,
            HttpServletRequest request) {
        adminService.cancelRentalRequest(principal.getId(), requestId, request.getRemoteAddr());
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    public Page<AdminDtos.AuditResponse> auditLogs(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.audits(pageable);
    }

    @GetMapping("/security-events")
    public Page<AdminDtos.SecurityEventResponse> securityEvents(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.securityEvents(pageable);
    }
}
