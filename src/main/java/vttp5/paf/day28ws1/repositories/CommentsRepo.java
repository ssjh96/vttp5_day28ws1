package vttp5.paf.day28ws1.repositories;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;

@Repository
public class CommentsRepo 
{
    @Autowired
    private MongoTemplate template;    

    // Method 1 - ($project) → One Document Per Comment
    // db.comments.aggregate([
    //     { $match: {'user': { $regex: 'paydirt', $options: 'i'}}}, // Filter comments to only comments made by username, case insensitive
    //     { $lookup: {from: 'games', localField: 'gid', foreignField: 'gid', as: 'games'} }, // Join comments with games 
    //     { $unwind: '$games'}, // Flatten 'games' array so it becomes an object
    //     { $project: {
    //         _id: 0, 
    //         gid: 1, // show game ID
    //         game_name: '$games.name', // Extract game name from 'games'
    //         rating: 1, // Show rating
    //         comments: '$c_text'
    //     }} // Rename 'c_text' to 'comments'
    // ])

    public List<Document> searchCommentsByUser1(String user)
    {
        // Match user 
        // Criteria is a helper class in to help write query condns
        Criteria criteria = Criteria.where("user")
                                    .regex(user, "i");

        MatchOperation findUserByName = Aggregation.match(criteria);

        // Join comments with games collection
        LookupOperation joinComments = Aggregation.lookup("games", "gid", "gid", "games");

        // Unwind games array that was created from lookup- When you do $lookup, MongoDB returns the matching documents inside an array.
        // but since each gid corresponds to exactly one game, the array is unnecessary
        UnwindOperation unwindGamesArray = Aggregation.unwind("games"); 
        // in MongoDB queries $games means use the field 'games' from the document, 
        // but Java's aggregation framework already knows it's refering to a field so no need $

        ProjectionOperation projectFields = Aggregation.project() // when using project, spring alr knows that the names are referencing fields
                                                        .andExclude("_id")                   // Exclude MongoDB's default "_id" field
                                                        .and("gid").as("gid")                   // Include "gid" field and rename it to "gid" (same name)
                                                        .and("games.name").as("games_name")     // Extract the "name" field from the "games" subdocument and rename it to "games_name"
                                                        .and("rating").as("rating")             // Include the "rating" field
                                                        .and("c_text").as("comments");          // Rename "c_text" to "comments"

        // ProjectionOperation projectFields2 = Aggregation.project("gid", "rating")
        // .and("games.name").as("games_name")
        // .and("c_text").as("comments")       
        // .andExclude("_id");   
        
        // ProjectionOperation projectFields3 = Aggregation.project()
        // .andInclude("gid", "rating")
        // .and("games.name").as("games_name")
        // .and("c_text").as("comments")       
        // .andExclude("_id");   

        // ProjectionOperation projectFields = Aggregation.project()
            // .andExpression("ranking * 10").as("weighted_ranking");  // Add a computed field
        // equivalent -> { $project: { weighted_ranking: { $multiply: ["$ranking", 10] } } }

        // Create pipeline
        Aggregation pipeline = Aggregation.newAggregation(findUserByName, joinComments, unwindGamesArray, projectFields);

        // Return List<Documents>
        return template.aggregate(pipeline, "comments", Document.class).getMappedResults(); 
    }
   


    // Method 2 - ($group & $push) → One Document Per User, each user has an Array of Comments containing all their game comments
    // db.comments.aggregate([
    //     { $match: {'user': { $regex: 'paydirt', $options: 'i'}}},        // Filter comments to only comments made by username, case insensitive
    //     { $lookup: {from: 'games', localField: 'gid', foreignField: 'gid', as: 'games'} },       // Join 'comments' with 'games' collection using 'gid', best to have index them both 
    //     { $unwind: '$games'},    // Flatten 'games' array so that games.name is no longer inside an array
    //     { $group: { 
    //         _id: '$user',        // Group all comments by the user
    //         comments: { 
    //             $push: { gid: '$gid', name: '$games.name', rating: '$rating', text: '$c_text'}       // Collect all comments into an array 
                                                                                                        // $games.name is extracted directly, not an array
    //         } 
    //     }}            
    // ])

    public List<Document> searchCommentsByUser2(String user)
    {
        // Match user 
        // Criteria is a helper class in to help write query condns
        Criteria criteria = Criteria.where("user")
                                    .regex(user, "i");

        MatchOperation findUserByName = Aggregation.match(criteria);

        // Join comments with games collection
        LookupOperation joinComments = Aggregation.lookup("games", "gid", "gid", "games");

        // Unwind games array that was created from lookup- When you do $lookup, MongoDB returns the matching documents inside an array.
        // but since each gid corresponds to exactly one game, the array is unnecessary
        UnwindOperation unwindGamesArray = Aggregation.unwind("games"); 
        // in MongoDB queries $games means use the field 'games' from the document, 
        // but Java's aggregation framework already knows it's refering to a field so no need $

        // Group the results by user
        GroupOperation groupByUser = Aggregation.group("user")
                                                .push(new BasicDBObject() // BasicDBObject is a MongoDB Java class that represents a JSON-style document (key-value pairs).
                                                .append("gid", "$gid")
                                                .append("name", "$games.name")
                                                .append("rating", "$rating")
                                                .append("text", "$c_text")
                                                ).as("comments");

        // Create pipeline
        Aggregation pipeline = Aggregation.newAggregation(findUserByName, joinComments, unwindGamesArray, groupByUser);

        // Return List<Documents>
        return template.aggregate(pipeline, "comments", Document.class).getMappedResults();                                     
    }

}
