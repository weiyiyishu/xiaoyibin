package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 借阅记录实体（对应表 borrow_records）
 */
@TableName("borrow_records")
public class BorrowRecord {

    /** 借阅中 */
    public static final String STATUS_BORROWING = "borrowing";
    /** 已归还 */
    public static final String STATUS_RETURNED = "returned";

    /** 借书期限（天） */
    public static final int BORROW_DAYS = 30;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    private LocalDateTime borrowAt;

    private LocalDateTime dueAt;

    private LocalDateTime returnAt;

    /** borrowing=借阅中 / returned=已归还 */
    private String status;

    // ---- 以下字段不入库，由 Service 关联图书 / 用户信息填充 ----

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String bookAuthor;

    @TableField(exist = false)
    private String coverUrl;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String displayName;

    @TableField(exist = false)
    private boolean overdue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public LocalDateTime getBorrowAt() {
        return borrowAt;
    }

    public void setBorrowAt(LocalDateTime borrowAt) {
        this.borrowAt = borrowAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public LocalDateTime getReturnAt() {
        return returnAt;
    }

    public void setReturnAt(LocalDateTime returnAt) {
        this.returnAt = returnAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }
}
