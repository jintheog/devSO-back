package com.example.devso.controller;

import com.example.devso.security.CustomUserDetails;
import com.example.devso.dto.request.RecruitRequest;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.EnumResponse;
import com.example.devso.dto.response.RecruitResponse;
import com.example.devso.entity.recruit.*;
import com.example.devso.service.RecruitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Recruit", description = "팀원 모집 API")
@RestController
@RequestMapping("/api/recruits")
@RequiredArgsConstructor
public class RecruitController {
    private final RecruitService recruitService;

    @Operation(summary = "모집글 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<RecruitResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecruitRequest request
    ){
        System.out.println(request.toString());
        RecruitResponse response = recruitService.create(userDetails.getId(),  request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "모집글 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecruitResponse>>> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long  userId = userDetails !=null ? userDetails.getId() : null;
        List<RecruitResponse> list = recruitService.findAll(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(list));
    }

    @Operation(summary = "모집글 상세조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecruitResponse>> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        System.out.println("로그인 유저 정보: " + userDetails);
        Long userId = userDetails.getId() != null ? userDetails.getId() : null;
        RecruitResponse response = recruitService.findById(id, userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "북마크")
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<Boolean>> toggleBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean bookmarked = recruitService.toggleBookmark(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(bookmarked));
    }

    @Operation(summary = "모집글 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecruitResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecruitRequest request
    ) {
        RecruitResponse response = recruitService.update(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "모집글 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recruitService.delete(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "모집글 상태 변경")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RecruitStatus>> toggleStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecruitStatus newStatus = recruitService.toggleStatus(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(newStatus));
    }

    //
//    @PostMapping("/{id}/comments")
//    public RecruitComment addComment(@PathVariable Long id, @RequestBody String content, @AuthenticationPrincipal CustomUserDetails userDetails) {
//        return recruitService.addComment(userDetails, id, content);
//    }

// enum (팀원 모집 게시글 생성 시 select option들)
    @Operation(summary = "포지션 enum 조회")
    @GetMapping("/enum/position")
    public ResponseEntity<List<EnumResponse>> getPositions() {
        List<EnumResponse> positions = Arrays.stream(RecruitPosition.values())
                .map(pos -> new EnumResponse(pos.getValue(), pos.getLabel(), pos.name()))
                .toList();
        return ResponseEntity.ok(positions);
    }

    @Operation(summary = "모집 타입 enum 조회")
    @GetMapping("/enum/type")
    public ResponseEntity<List<EnumResponse>> getTypes() {
        List<EnumResponse> types = Arrays.stream(RecruitType.values())
                .map(type -> new EnumResponse(type.getValue(), type.getLabel(), type.name()))
                .toList();
        return ResponseEntity.ok(types);
    }

    @Operation(summary = "진행 방식 enum 조회")
    @GetMapping("/enum/progress-type")
    public ResponseEntity<List<EnumResponse>> getProgress() {
        List<EnumResponse> progress = Arrays.stream(RecruitProgressType.values())
                .map(p -> new EnumResponse(p.getValue(), p.getLabel(), p.name()))
                .toList();
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "기술 스택 enum 조회")
    @GetMapping("/enum/tech-stacks")
    public ResponseEntity<List<EnumResponse>> getTechStacks() {
        List<EnumResponse> stacks = Arrays.stream(TechStack.values())
                .map(stack -> new EnumResponse(stack.getValue(), stack.getLabel(), stack.name()))
                .toList();
        return ResponseEntity.ok(stacks);
    }

    @Operation(summary = "연락 방법 enum 조회")
    @GetMapping("/enum/contact")
    public ResponseEntity<List<EnumResponse>> getContactTypes() {
        List<EnumResponse> contactTypes = Arrays.stream(RecruitContactMethod.values())
                .map(c -> new EnumResponse(c.getValue(), c.getLabel(), c.name()))
                .toList();
        return ResponseEntity.ok(contactTypes);
    }

    @Operation(summary = "진행 기간 enum 조회")
    @GetMapping("/enum/duration")
    public ResponseEntity<List<EnumResponse>> getDurationTypes() {
        List<EnumResponse> durationTypes = Arrays.stream(RecruitDuration.values())
                .map(d -> new EnumResponse(d.getValue(), d.getLabel(), d.name()))
                .toList();
        return ResponseEntity.ok(durationTypes);
    }

    @Operation(summary = "모집 인원 enum 조회")
    @GetMapping("/enum/memberCount")
    public ResponseEntity<List<EnumResponse>> getMemberCount() {
        List<EnumResponse> memberCounts = Arrays.stream(RecruitCountOption.values())
                .map(m -> new EnumResponse(m.getValue(), m.getLabel(), m.name()))
                .toList();
        return ResponseEntity.ok(memberCounts);
    }
}
