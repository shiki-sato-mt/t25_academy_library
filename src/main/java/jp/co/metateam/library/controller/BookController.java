package jp.co.metateam.library.controller;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.ISBN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;
// 書籍編集で入力
import java.util.Objects;



/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
        model.addAttribute("bookMstList", bookMstList);

        return "book/index";
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }

        return "book/add";
    }

    // @GetMapping("/book/edit")
    // public String edit(Model model) {
    //     if (!model.containsAttribute("bookMstDto")) {
    //         model.addAttribute("bookMstDto", new BookMstDto());
    //     }

    //     return "book/edit";
    // }

 


    //書籍登録バリデーション
    @PostMapping("/book/add")
    public String register(@ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra) {
       try {
           boolean hasError = false;
     
           if (bookMstDto.getTitle() == null || bookMstDto.getTitle().trim().isEmpty()) {
               result.rejectValue("title", "error.title.required", "書籍名は必須です");
               hasError = true;
           }
           
           if (bookMstDto.getTitle() != null && bookMstDto.getTitle().length() > 255) {
               result.rejectValue("title", "error.title.length", "書籍名は255文字以内で入力してください");
               hasError = true;
           }
       
           if (bookMstDto.getIsbn() == null || bookMstDto.getIsbn().trim().isEmpty()) {
               result.rejectValue("isbn", "error.isbn.required", "ISBNは必須です");
               hasError = true;
           }
           
           if (bookMstDto.getIsbn() != null && !bookMstDto.getIsbn().isEmpty() && bookMstDto.getIsbn().length() != 13) {
               result.rejectValue("isbn", "error.isbn.length", "ISBNは13桁で入力してください");
               hasError = true;
           }
           
           if (bookMstDto.getIsbn() != null && !bookMstDto.getIsbn().isEmpty() && !bookMstDto.getIsbn().matches("^[0-9]+$")) {
               result.rejectValue("isbn", "error.isbn.hankaku", "ISBNは半角で入力してください");
               hasError = true;
           }
           if (bookMstService.existsByIsbn(bookMstDto.getIsbn())) {
            result.rejectValue("isbn", "error.isbn.duplicate", "このISBNは既に登録されています");
            hasError = true;
        }
           
           if (hasError) {
               throw new Exception("バリデーションエラー");
           }
           
           bookMstService.save(bookMstDto);
           return "redirect:/book/index";
       } catch (Exception e) {
           log.error("書籍登録エラー: {}", e.getMessage());
     
           ra.addFlashAttribute("bookMstDto", bookMstDto);
           ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);
           return "book/add";
       }
    }




    // 書籍編集
    @GetMapping("/book/edit/{id}")
    public String edit(@PathVariable Long id, Model model,RedirectAttributes redirectAttributes) {
    BookMstDto bookMstDto = bookMstService.findById(id);
    if (bookMstDto == null) {
        // IDが見つからない場合は一覧へリダイレクト
        redirectAttributes.addFlashAttribute("errorMessage", "データが存在しません");
        return "redirect:/book/index";
    }
    model.addAttribute("bookMstDto", bookMstDto);
    return "book/edit"; // 編集画面へ

    
}

// 書籍編集バリデーション
@PostMapping("/book/edit")

    public String update(@ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra) {
       try {
           boolean hasError = false;

        //    long NyuryokusareteitaId = bookMstDto.getId();
        //    BookMstDto Sagaitaiyatu = bookMstService.findById(NyuryokusareteitaId);
        //    String hikakusitaiyasuTitle = Sagaitaiyatu.getTitle();
     
           // 書籍名の変更があるかのバリデーション

           BookMstDto original = bookMstService.findById(bookMstDto.getId());
           if (original == null) {
            ra.addFlashAttribute("errorMessage", "該当する書籍が削除されているため、編集できません。");
            return "redirect:/book/index";
           }

           boolean isTitleChanged = !Objects.equals(original.getTitle(), bookMstDto.getTitle());
        boolean isIsbnChanged = !Objects.equals(original.getIsbn(), bookMstDto.getIsbn());

           if (!isTitleChanged && !isIsbnChanged) {
               ra.addFlashAttribute("infoMessage", "変更点がありません");
               return "redirect:/book/edit/" + bookMstDto.getId();
           }

                   if (isTitleChanged) {
            if (bookMstDto.getTitle() == null || bookMstDto.getTitle().trim().isEmpty()) {
                result.rejectValue("title", "error.title.required", "書籍名は必須です");
                hasError = true;
            } else if (bookMstDto.getTitle().length() > 255) {
                result.rejectValue("title", "error.title.length", "書籍名は255文字以内で入力してください");
                hasError = true;
            }
        }


        //    ISBNの変更があるかどうかのバリデーション
        //    if (bookMstService.findById(bookMstDto.getId()).getIsbn().equals (bookMstDto.getIsbn())){
            // hasError = true;
        //    }

        if (isIsbnChanged) {
            String isbn = bookMstDto.getIsbn();
        
            if (isbn == null || isbn.trim().isEmpty()) {
                result.rejectValue("isbn", "error.isbn.required", "ISBNは必須です");
                hasError = true;
            } else {
                if (isbn.length() != 13) {
                    result.rejectValue("isbn", "error.isbn.length", "ISBNは13桁で入力してください");
                    hasError = true;
                }
        
                if (!isbn.matches("^[0-9]+$")) {
                    result.rejectValue("isbn", "error.isbn.hankaku", "ISBNは半角で入力してください");
                    hasError = true;
                }
        
                if (bookMstService.existsByIsbnAndNotId(isbn, bookMstDto.getId())) {
                    result.rejectValue("isbn", "error.isbn.duplicate", "このISBNは既に登録されています");
                    hasError = true;
                }
            }
        }
        

           if (hasError) {
               throw new Exception("バリデーションエラー");
           }
           
           
           bookMstService.update(bookMstDto);
           // 書籍編集が成功したら
ra.addFlashAttribute("successMessage", "書籍の変更が完了しました");
           return "redirect:/book/index";
       } catch (Exception e) {
           log.error("書籍編集エラー: {}", e.getMessage());
     
           ra.addFlashAttribute("bookMstDto", bookMstDto);
           ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);
           return "book/edit";
       }
    }

    
    

}



