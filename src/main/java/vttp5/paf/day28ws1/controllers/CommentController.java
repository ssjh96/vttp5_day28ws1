package vttp5.paf.day28ws1.controllers;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import vttp5.paf.day28ws1.repositories.CommentsRepo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentController {
    
    @Autowired 
    private CommentsRepo commentsRepo;

    @GetMapping(path = "/comments1/{user}")
    public ResponseEntity<String> getCommentsByUser1(@PathVariable String user) {
        List<Document> results = commentsRepo.searchCommentsByUser1(user);

        if(results.size() <= 0)
        {
            return ResponseEntity.status(404).body("Not found: %s".formatted(user));
        }
        
        // return ResponseEntity.ok(results.get(0).toJson());

        // Manually convert List<Document> to JSON array string
        String jsonArray = results.stream()
                                .map(Document::toJson) // Step 1: Convert each Document to its JSON string - means for each Document in stream, call the .toJson() method
                                // :: operator in Java is called the method reference operator
                                // shorthand way to refer to methods or constructors without explicitly calling them
                                // Document::toJson is a method reference to the toJson() method in the Document class.
                                // For each Document in the stream, it automatically calls the toJson() method and transforms the Document into its JSON string representation.
                                // equivalent to writing lambda expresion like: .map(document -> document.toJson())
                                .toList()              // Step 2: Collect the JSON strings into a List<String>
                                .toString();           // Step 3: Convert the List<String> to a JSON array string

        return ResponseEntity.ok(jsonArray);
    }

    @GetMapping(path = "/comments2/{user}")
    public ResponseEntity<String> getCommentsByUser2(@PathVariable String user) {
        List<Document> results = commentsRepo.searchCommentsByUser2(user);

        if(results.size() <= 0)
        {
            return ResponseEntity.status(404).body("Not found: %s".formatted(user));
        }
        
        // return ResponseEntity.ok(results.get(0).toJson());
        return ResponseEntity.ok(results.get(0).toJson()); // only has 1 element in the list
    }
    
}
