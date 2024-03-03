package exercise.controller;

import exercise.model.Comment;
import exercise.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Optional;

import exercise.model.Post;
import exercise.repository.PostRepository;
import exercise.exception.ResourceNotFoundException;
import exercise.dto.PostDTO;
import exercise.dto.CommentDTO;

// BEGIN
@RestController
@RequestMapping("/posts")
public class PostsController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    @GetMapping(path = "")
    public List<PostDTO> index() {
        return postRepository.findAll().stream().map(this::toPostDto).toList();
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDTO> show(@PathVariable Long id) {
        Optional<Post> founded = postRepository.findById(id);
        return founded.map(post -> ResponseEntity.of(Optional.of(toPostDto(post))))
                .orElseGet(() -> {
                    var postDto = new PostDTO();
                    postDto.setBody("Post with id 100 not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(postDto);});
    }


    private PostDTO toPostDto(Post post) {
        var dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setBody(post.getBody());
        dto.setComments(commentRepository.findByPostId(post.getId()).stream().map(this::toCommentDto).toList());

        return dto;
    }

    private CommentDTO toCommentDto(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setBody(comment.getBody());

        return commentDTO;
    }
}
// END
