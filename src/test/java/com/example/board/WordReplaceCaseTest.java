package com.example.board;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import com.example.board.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:replacecasedb;DB_CLOSE_DELAY=-1")
class WordReplaceCaseTest {

    @Autowired PostService service;
    @Autowired PostRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private Long save(String title, String content) {
        Post p = new Post();
        p.setTitle(title);
        p.setName("kim");
        p.setContent(content);
        return repository.save(p).getId();
    }

    @Test
    void 검색된_글은_대소문자가_달라도_변경된다() {
        Long a = save("KFC 후기", "Kfc 신메뉴");
        Long b = save("점심", "kfc 먹음");

        List<Long> found = service.searchIdsByWord("kfc");
        int changed = service.replaceWord(found, "kfc", "bhc");

        assertThat(found).containsExactly(a, b);
        assertThat(changed).isEqualTo(2);
        assertThat(service.get(a).getTitle()).isEqualTo("bhc 후기");
        assertThat(service.get(a).getContent()).isEqualTo("bhc 신메뉴");
        assertThat(service.get(b).getContent()).isEqualTo("bhc 먹음");
        assertThat(service.searchIdsByWord("kfc")).isEmpty();
    }

    @Test
    void 바뀌지_않은_글은_집계에_포함되지_않는다() {
        Long hit = save("kfc", "내용");
        Long miss = save("책", "독서");

        int changed = service.replaceWord(List.of(hit, miss, 9999L), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
        assertThat(service.get(miss).getTitle()).isEqualTo("책");
    }

    @Test
    void 치환_문자열의_특수문자는_그대로_쓰인다() {
        Long id = save("kfc", "kfc.com");

        service.replaceWord(List.of(id), "kfc", "$1\\x");

        assertThat(service.get(id).getTitle()).isEqualTo("$1\\x");
        assertThat(service.get(id).getContent()).isEqualTo("$1\\x.com");
    }
}
