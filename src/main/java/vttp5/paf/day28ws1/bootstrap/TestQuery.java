package vttp5.paf.day28ws1.bootstrap;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import vttp5.paf.day28ws1.repositories.CommentsRepo;

@Component
public class TestQuery implements CommandLineRunner{
    
    @Autowired
    private CommentsRepo commentsRepo;

    @Override
    public void run(String... args) throws Exception {
        
        System.out.println(">>> Searching with first method - 1 doc/comment.");
        List<Document> result1 = commentsRepo.searchCommentsByUser1("paydirt");

        for (Document doc : result1)
        {
            System.out.printf(">>> %s\n\n", doc);
        }

        System.out.println(">>> Searching with second - 1 doc with an array of comments.");
        List<Document> result2 = commentsRepo.searchCommentsByUser2("paydirt");

        for (Document doc : result2)
        {
            System.out.printf(">>> %s\n\n", doc);

            List<Document> comments = doc.getList("comments", Document.class);
            
            for(Document c: comments)
            {
                System.out.printf("%s\n\n", c); // \t horizontal tab
            }
        }
    }

    //$push and $lookup both creates an array but differently day28-2
}
