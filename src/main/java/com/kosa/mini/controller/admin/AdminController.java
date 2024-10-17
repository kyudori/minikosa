package com.kosa.mini.controller.admin;

import com.kosa.mini.domain.member.Member;
import com.kosa.mini.domain.member.UserSessionDTO;
import com.kosa.mini.domain.store.StoreDTO;
import com.kosa.mini.service.store.StoreService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final StoreService storeService;

    @Autowired
    public AdminController(StoreService storeService) {
        this.storeService = storeService;
    }

    //맛집 등록 페이지
    @GetMapping("create/store")
    public String createStoreView(Model model, HttpSession session) {
        // 현재 로그인된 사용자 정보 가져오기
        UserSessionDTO loggedInUser = (UserSessionDTO) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getRoleId().equals(2)) { // roleId 2가 ADMIN
            return "redirect:/access-denied";
        }

        model.addAttribute("storeDTO", new StoreDTO());
        return "admin_create_store";
    }

    @PostMapping("create/store")
    public String createStore(@Valid StoreDTO storeDTO,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session) {
        UserSessionDTO loggedInUser = (UserSessionDTO) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getRoleId().equals(2)) {
            return "redirect:/access-denied";
        }

        if (bindingResult.hasErrors()) {
            return "admin_create_store";
        }

        try {
            storeService.createStore(storeDTO, storeDTO.getMenuDTOs());
            model.addAttribute("successMessage", "가게와 메뉴가 성공적으로 등록되었습니다.");
            return "redirect:/content";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin_create_store";
        }
    }

    // 사장 등록 페이지
    @GetMapping("/register/owner")
    public String registerOwnerView(HttpSession session) {
        // 관리자 권한 체크
        UserSessionDTO loggedInUser = (UserSessionDTO) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getRoleId().equals(2)) {
            return "redirect:/access-denied";
        }
        return "admin_register_owner";
    }

    // 사장님 할당 API
    @PostMapping("/assign/owner")
    @ResponseBody
    public ResponseEntity<String> assignOwnerToStore(@RequestParam Long storeId, @RequestParam Long ownerId, HttpSession session) {
        // 관리자 권한 체크
        UserSessionDTO loggedInUser = (UserSessionDTO) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getRoleId().equals(2)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
        }

        try {
            storeService.assignOwnerToStore(storeId, ownerId);
            return ResponseEntity.ok("사장님이 가게에 성공적으로 매칭되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("매칭 중 오류가 발생했습니다.");
        }
    }

    // 사용자 검색 API
    @GetMapping("/search/users")
    @ResponseBody
    public ResponseEntity<List<Member>> searchUsers(@RequestParam String email) {
        List<Member> users = storeService.searchMembersByEmail(email);
        return ResponseEntity.ok(users);
    }

    // 가게 검색 API
    @GetMapping("/search/stores")
    @ResponseBody
    public ResponseEntity<List<StoreDTO>> searchStores(@RequestParam String storeName) {
        List<StoreDTO> stores = storeService.searchStoresByName(storeName);
        return ResponseEntity.ok(stores);
    }
}

