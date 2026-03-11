package com.dong.yuanmianai.mapper;

import com.dong.yuanmianai.model.entity.Post;
import java.util.Date;
import java.util.List;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子数据库操作测试
 *
 * @author <a href="">程序员远行</a>
 * @from <">公众号：所谓远行Misnearch</a>
 */
@SpringBootTest
class PostMapperTest {

    @Resource
    private PostMapper postMapper;

    @Test
    void listPostWithDelete() {
        List<Post> postList = postMapper.listPostWithDelete(new Date());
        Assertions.assertNotNull(postList);
    }
}