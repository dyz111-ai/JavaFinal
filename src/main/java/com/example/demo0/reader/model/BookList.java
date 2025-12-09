package com.example.demo0.reader.model;

/**
 * 书单实体类，对应数据库 Booklist 表
 * 字段名使用首字母大写，匹配原项目格式
 */
public class BookList {
    private Integer BooklistId;
    private String ListCode;
    private String BooklistName;
    private String BooklistIntroduction;
    private Integer CreatorId;
    private java.sql.Timestamp FavoriteTime; // 用于收藏书单时的时间
    private String Notes; // 收藏备注

    /**
     * 无参构造函数，用于 Repository 映射
     */
    public BookList() {
    }

    // Getters and Setters
    public Integer getBooklistId() {
        return BooklistId;
    }

    public void setBooklistId(Integer booklistId) {
        this.BooklistId = booklistId;
    }

    public String getListCode() {
        return ListCode;
    }

    public void setListCode(String listCode) {
        this.ListCode = listCode;
    }

    public String getBooklistName() {
        return BooklistName;
    }

    public void setBooklistName(String booklistName) {
        this.BooklistName = booklistName;
    }

    public String getBooklistIntroduction() {
        return BooklistIntroduction;
    }

    public void setBooklistIntroduction(String booklistIntroduction) {
        this.BooklistIntroduction = booklistIntroduction;
    }

    public Integer getCreatorId() {
        return CreatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.CreatorId = creatorId;
    }

    public java.sql.Timestamp getFavoriteTime() {
        return FavoriteTime;
    }

    public void setFavoriteTime(java.sql.Timestamp favoriteTime) {
        this.FavoriteTime = favoriteTime;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        this.Notes = notes;
    }
}
