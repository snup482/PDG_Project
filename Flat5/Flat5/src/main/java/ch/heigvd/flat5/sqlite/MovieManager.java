package ch.heigvd.flat5.sqlite;

import ch.heigvd.flat5.api.video.Episode;
import ch.heigvd.flat5.api.video.MovieInfos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe permettant de gérer les médias de type vidéos de la base de données.
 * @author Jan Purro
 */
public class MovieManager
{
    // Connection à la base de données de l'application.
    private Connection connection;

    /**
     * Construit un nouveau MovieManager
     * @param connector Un objet de type SQLiteConnector. Il doit lui-même être déjà connecté à la base de données.
     */
    public MovieManager(SQLiteConnector connector)
    {
        connection = connector.getConnection();
    }

    /**
     * Vérifier si un fichier vidéo est déjà connu de la base de donnée.
     * @param path Le chemin du fichier en question.
     * @return true si le fichier est déjà connu, false sinon.
     */
    public boolean isKnown(String path)
    {
        try
        {
            // On chercher le fichier parmi les films.
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM movies WHERE path = '" + path + "'");
            while(result.next())
            {
                return true;
            }

            // On cherche le fichier parmi les épisodes.
            result = statement.executeQuery("SELECT * FROM episodes WHERE path = '" + path + "'");
            while(result.next())
            {
                return true;
            }
        }
        catch ( Exception e )
        {
            System.err.println("Error while loocking for track " + e.getClass().getName() + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * Vérifier si une série est déjà connue de la base de donnée
     * @param path Le chemin de la série en question.
     * @return true si la série est connue, false sinon.
     */
    public boolean serieIsKnown (String path)
    {
        try
        {
            // On recherch le chemin parmi les séries.
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM movies WHERE  type = 'series' " +
                    "AND path = '" + path + "'");
            while(result.next())
            {
                return true;
            }
        }
        catch ( Exception e )
        {
            System.err.println("Error while loocking for serie " + e.getClass().getName() + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * Ajout d'un film ou d'une série à la base de donnée.
     * @param infos Les informations du film ou de la série
     * @param path Le chemin du fichier pour un film, du dossier pour une série.
     * @return L'id dans la base de donnée du film ou de la série. Si le l'insertion n'a pas été possible, retourne -1.
     */
    public int addMovie (MovieInfos infos, String path)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement( "INSERT INTO movies(" +
                    "path, title, runtime, year, type, releaseDate, genre, plot, imdbID, imdbRating," +
                    "imdbVotes, metaScore, poster) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, path);
            statement.setString(2, infos.getTitle());
            statement.setString(3, infos.getRuntime());
            statement.setString(4, infos.getYear());
            statement.setString(5, infos.getType());
            statement.setString(6, infos.getReleaseDate());
            statement.setString(7, infos.getGenre());
            statement.setString(8, infos.getPlot());
            statement.setString(9, infos.getImdbID());
            statement.setString(10, infos.getImdbRating());
            statement.setString(11, infos.getImdbVotes());
            statement.setString(12, infos.getMetaScore());
            statement.setString(13, infos.getPoster());
            statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();
            if(resultSet.next())
            {
                return resultSet.getInt(1);
            }
        }
        catch ( Exception e )
        {
            System.err.println("Error while adding movie " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retourne tous les films contenus dans la base de données.
     * @return La liste des filmes contenus dans la base de données.
     */
    public List<MovieInfos> getMovies()
    {
        List<MovieInfos> movies = new LinkedList<>();
        try
        {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM movies WHERE type = 'movie'");
            while(result.next())
            {
                movies.add(new MovieInfos(
                        result.getString("title"),
                        result.getString("year"),
                        result.getString("releaseDate"),
                        result.getString("runtime"),
                        result.getString("genre"),
                        result.getString("plot"),
                        result.getString("imdbRating"),
                        result.getString("imdbVotes"),
                        result.getString("type"),
                        result.getString("metaScore"),
                        result.getString("imdbID"),
                        result.getString("poster"),
                        result.getString("path"),
                        result.getInt("id")
                        ));
            }
        }
        catch ( Exception e )
        {
            System.err.println("Error while getting movies " + e.getClass().getName() + ": " + e.getMessage());
        }
        return movies;
    }

    /**
     * Retourne toutes les séries contenues dans la base de données.
     * @return Une liste des séries contenues dans la base de données.
     */
    public List<MovieInfos> getSeries()
    {
        List<MovieInfos> movies = new LinkedList<>();
        try
        {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM movies WHERE type = 'series'");
            while(result.next())
            {
                movies.add(new MovieInfos(
                        result.getString("title"),
                        result.getString("year"),
                        result.getString("releaseDate"),
                        result.getString("runtime"),
                        result.getString("genre"),
                        result.getString("plot"),
                        result.getString("imdbRating"),
                        result.getString("imdbVotes"),
                        result.getString("type"),
                        result.getString("metaScore"),
                        result.getString("imdbID"),
                        result.getString("poster"),
                        result.getString("path"),
                        result.getInt("id")
                ));
            }
        }
        catch ( Exception e )
        {
            System.err.println("Error while getting series " + e.getClass().getName() + ": " + e.getMessage());
        }
        return movies;
    }

    /**
     * Ajout d'un épisode d'une série dans la base de données.
     * @param title Le titre de l'épisode
     * @param serieId L'id, dans la base de données, de la série dont l'épisode fait partie.
     * @param season La saison de l'épisode.
     * @param episode Le numéro de l'épisode
     * @param path Le chemin de l'épisode dans la collection de l'utilisateur.
     */
    public void addEpisode(String title, int serieId, String season, String episode, String path)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO episodes(" +
                    "path, title, serieID, season, episode) " +
                    "VALUES(?, ?, ?, ?, ?)");

            statement.setString(1, path);
            statement.setString(2, title);
            statement.setInt(3, serieId);
            statement.setString(4, season);
            statement.setString(5, episode);
            statement.executeUpdate();
        }
        catch ( Exception e )
        {
            System.err.println("Error while adding epsiode " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Retourne l'ensembles des épisodes d'une série contenus dans la base de données.
     * @param serieId L'id de la série dont les épisodes sont recherchés.
     * @return La liste des épisodes de la série contenus dans la base de données.
     */
    public List<String> getSerieEpisodes(String serieId)
    {
        List<String> episodes = new LinkedList<>();
        try
        {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM episodes WHERE serieId = "+ new Integer(serieId));
            while(result.next())
            {
                episodes.add(result.getString("title"));
            }
        }
        catch ( Exception e )
        {
            System.err.println("Error while getting movies " + e.getClass().getName() + ": " + e.getMessage());
        }
        return episodes;
    }
}
