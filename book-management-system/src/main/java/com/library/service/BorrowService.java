package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.ServiceException;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BorrowService {

    private final BorrowRecordMapper borrowMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    public BorrowService(BorrowRecordMapper borrowMapper,
                         BookMapper bookMapper,
                         UserMapper userMapper) {
        this.borrowMapper = borrowMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    /**
     * 借书：校验图书/库存/重复借阅，库存 -1 并写入借阅记录（同一事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long borrowBook(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            throw new ServiceException("参数不完整");
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new ServiceException("图书不存在");
        }

        // 同一本书未归还前不能重复借阅
        Long activeCount = borrowMapper.selectCount(new QueryWrapper<BorrowRecord>()
                .eq("user_id", userId)
                .eq("book_id", bookId)
                .eq("status", BorrowRecord.STATUS_BORROWING));
        if (activeCount != null && activeCount > 0) {
            throw new ServiceException("你已借阅《" + book.getTitle() + "》，请先归还再借");
        }

        // 原子扣减库存：只有 stock>0 时才能成功，避免并发借出
        int rows = bookMapper.update(null, new UpdateWrapper<Book>()
                .eq("id", bookId)
                .gt("stock", 0)
                .setSql("stock = stock - 1"));
        if (rows == 0) {
            throw new ServiceException("《" + book.getTitle() + "》库存不足，无法借阅");
        }

        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowAt(now);
        record.setDueAt(now.plusDays(BorrowRecord.BORROW_DAYS));
        record.setStatus(BorrowRecord.STATUS_BORROWING);
        borrowMapper.insert(record);
        return record.getId();
    }

    /**
     * 还书：校验记录归属与状态，库存 +1，记录置为已归还（同一事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public void returnBook(Long userId, Long recordId) {
        if (recordId == null) {
            throw new ServiceException("借阅记录不存在");
        }
        BorrowRecord record = borrowMapper.selectById(recordId);
        if (record == null) {
            throw new ServiceException("借阅记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new ServiceException("只能归还自己借阅的图书");
        }
        if (!BorrowRecord.STATUS_BORROWING.equals(record.getStatus())) {
            throw new ServiceException("该记录已归还，无需重复操作");
        }

        LocalDateTime now = LocalDateTime.now();
        int rows = borrowMapper.update(null, new UpdateWrapper<BorrowRecord>()
                .eq("id", recordId)
                .eq("status", BorrowRecord.STATUS_BORROWING)
                .set("status", BorrowRecord.STATUS_RETURNED)
                .set("return_at", now));
        if (rows == 0) {
            throw new ServiceException("归还失败，请刷新后重试");
        }

        // 库存加回（图书仍存在时）
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            bookMapper.update(null, new UpdateWrapper<Book>()
                    .eq("id", record.getBookId())
                    .setSql("stock = stock + 1"));
        }
    }

    /**
     * 判断用户当前是否借阅了某本书（详情页展示状态用）
     */
    public boolean isBorrowing(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            return false;
        }
        Long count = borrowMapper.selectCount(new QueryWrapper<BorrowRecord>()
                .eq("user_id", userId)
                .eq("book_id", bookId)
                .eq("status", BorrowRecord.STATUS_BORROWING));
        return count != null && count > 0;
    }

    /**
     * 读者的借阅记录：先返回借阅中（按应还时间升序），再返回历史（按借出时间倒序）
     */
    public List<BorrowRecord> listMyRecords(Long userId) {
        List<BorrowRecord> active = borrowMapper.selectList(new QueryWrapper<BorrowRecord>()
                .eq("user_id", userId)
                .eq("status", BorrowRecord.STATUS_BORROWING)
                .orderByAsc("due_at"));
        List<BorrowRecord> history = borrowMapper.selectList(new QueryWrapper<BorrowRecord>()
                .eq("user_id", userId)
                .eq("status", BorrowRecord.STATUS_RETURNED)
                .orderByDesc("borrow_at"));

        List<BorrowRecord> all = new ArrayList<>(active.size() + history.size());
        all.addAll(active);
        all.addAll(history);
        enrichBooks(all);
        return all;
    }

    /**
     * 管理员分页查看全部借阅记录，可按状态和关键词（书名 / 用户名）筛选
     */
    public Page<BorrowRecord> pageAllRecords(long page, long size, String status, String keyword) {
        QueryWrapper<BorrowRecord> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();

            List<Long> bookIds = bookMapper.selectList(new QueryWrapper<Book>()
                            .like("title", kw).select("id")).stream()
                    .map(Book::getId).collect(Collectors.toList());
            List<Long> userIds = userMapper.selectList(new QueryWrapper<User>()
                            .and(w -> w.like("username", kw).or().like("display_name", kw))
                            .select("id")).stream()
                    .map(User::getId).collect(Collectors.toList());

            if (bookIds.isEmpty() && userIds.isEmpty()) {
                // 没有任何匹配，直接返回空页
                Page<BorrowRecord> empty = new Page<>(page, size);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            wrapper.and(w -> {
                if (!bookIds.isEmpty()) {
                    w.in("book_id", bookIds);
                }
                if (!userIds.isEmpty()) {
                    if (!bookIds.isEmpty()) {
                        w.or();
                    }
                    w.in("user_id", userIds);
                }
            });
        }
        wrapper.orderByDesc("borrow_at");

        Page<BorrowRecord> result = borrowMapper.selectPage(new Page<>(page, size), wrapper);
        enrichBooks(result.getRecords());
        enrichUsers(result.getRecords());
        return result;
    }

    /** 关联填充图书信息，并计算是否逾期 */
    private void enrichBooks(List<BorrowRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> bookIds = records.stream()
                .map(BorrowRecord::getBookId)
                .collect(Collectors.toSet());
        Map<Long, Book> bookMap = new HashMap<>();
        if (!bookIds.isEmpty()) {
            bookMapper.selectBatchIds(bookIds)
                    .forEach(b -> bookMap.put(b.getId(), b));
        }
        LocalDateTime now = LocalDateTime.now();
        Set<Long> overdueIds = new HashSet<>();
        for (BorrowRecord r : records) {
            Book b = bookMap.get(r.getBookId());
            if (b != null) {
                r.setBookTitle(b.getTitle());
                r.setBookAuthor(b.getAuthor());
                r.setCoverUrl(b.getCoverUrl());
            } else {
                // 图书可能已被管理员删除
                r.setBookTitle("（图书已删除）");
            }
            boolean overdue = BorrowRecord.STATUS_BORROWING.equals(r.getStatus())
                    && r.getDueAt() != null
                    && r.getDueAt().isBefore(now);
            r.setOverdue(overdue);
            if (overdue) {
                overdueIds.add(r.getId());
            }
        }
    }

    /** 关联填充借阅人信息（管理员页面用） */
    private void enrichUsers(List<BorrowRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> userIds = records.stream()
                .map(BorrowRecord::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds)
                    .forEach(u -> userMap.put(u.getId(), u));
        }
        for (BorrowRecord r : records) {
            User u = userMap.get(r.getUserId());
            if (u != null) {
                r.setUsername(u.getUsername());
                r.setDisplayName(u.getDisplayName());
            }
        }
    }
}
