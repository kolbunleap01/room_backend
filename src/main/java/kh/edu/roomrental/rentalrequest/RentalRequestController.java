package kh.edu.roomrental.rentalrequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kh.edu.roomrental.user.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
public class RentalRequestController {
    private final RentalRequestService rentalRequestService;

    public RentalRequestController(RentalRequestService rentalRequestService) {
        this.rentalRequestService = rentalRequestService;
    }

    @PostMapping("/rooms/{roomId}/rental-requests")
    public ResponseEntity<RentalRequestDtos.Response> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId,
            @Valid @RequestBody RentalRequestDtos.CreateRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(201).body(rentalRequestService.create(principal.getId(), roomId, request,
                httpRequest.getRemoteAddr()));
    }

    @GetMapping("/me/rental-requests")
    public Page<RentalRequestDtos.Response> mine(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return rentalRequestService.mine(principal.getId(), pageable);
    }

    @GetMapping("/me/room-requests")
    public Page<RentalRequestDtos.Response> forOwner(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return rentalRequestService.forOwner(principal.getId(), pageable);
    }

    @GetMapping("/rental-requests")
    public Page<RentalRequestDtos.Response> aliasList(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "sent") String direction,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return "received".equalsIgnoreCase(direction)
                ? rentalRequestService.forOwner(principal.getId(), pageable)
                : rentalRequestService.mine(principal.getId(), pageable);
    }

    @PostMapping("/rental-requests")
    public ResponseEntity<RentalRequestDtos.Response> aliasCreate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RentalRequestDtos.AliasCreateRequest request,
            HttpServletRequest httpRequest) {
        LocalDate moveInDate = request.moveInDate() == null ? LocalDate.now() : request.moveInDate();
        RentalRequestDtos.CreateRequest createRequest = new RentalRequestDtos.CreateRequest(moveInDate, request.message());
        return ResponseEntity.status(201).body(rentalRequestService.create(principal.getId(), request.roomId(),
                createRequest, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/rental-requests/{requestId}/approve")
    public RentalRequestDtos.Response approve(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable UUID requestId,
                                              @Valid @RequestBody(required = false) RentalRequestDtos.DecisionRequest request,
                                              HttpServletRequest httpRequest) {
        return rentalRequestService.decide(principal.getId(), requestId, true, request, httpRequest.getRemoteAddr());
    }

    @PostMapping("/rental-requests/{requestId}/reject")
    public RentalRequestDtos.Response reject(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable UUID requestId,
                                             @Valid @RequestBody(required = false) RentalRequestDtos.DecisionRequest request,
                                             HttpServletRequest httpRequest) {
        return rentalRequestService.decide(principal.getId(), requestId, false, request, httpRequest.getRemoteAddr());
    }

    @DeleteMapping("/rental-requests/{requestId}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable UUID requestId,
                                       HttpServletRequest httpRequest) {
        rentalRequestService.cancel(principal.getId(), requestId, httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.PatchMapping("/rental-requests/{requestId}")
    public RentalRequestDtos.Response aliasUpdateStatus(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID requestId,
            @Valid @RequestBody RentalRequestDtos.StatusUpdateRequest request,
            HttpServletRequest httpRequest) {
        String status = request.status().trim().toLowerCase(java.util.Locale.ROOT);
        return switch (status) {
            case "approved" -> rentalRequestService.decide(principal.getId(), requestId, true,
                    new RentalRequestDtos.DecisionRequest(request.note()), httpRequest.getRemoteAddr());
            case "rejected" -> rentalRequestService.decide(principal.getId(), requestId, false,
                    new RentalRequestDtos.DecisionRequest(request.note()), httpRequest.getRemoteAddr());
            case "cancelled", "canceled" -> rentalRequestService.cancel(principal.getId(), requestId,
                    httpRequest.getRemoteAddr());
            default -> throw new kh.edu.roomrental.common.exception.ConflictException("Unsupported request status");
        };
    }
}
