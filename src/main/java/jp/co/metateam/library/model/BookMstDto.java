package jp.co.metateam.library.model;

import java.security.Timestamp;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;



/**
 * 書籍マスタDTO
 */
@Getter
@Setter
public class BookMstDto {
    
    private Long id; 
    
    private String isbn;

    private String title;
    
    private Timestamp deletedAt;

    private BookMst bookMst;
    private Boolean deletedFlag;

public Boolean getDeletedFlag() {
    return deletedFlag;
}

public void setDeletedFlag(Boolean deletedFlag) {
    this.deletedFlag = deletedFlag;
}

}

