package onlineshop;

import onlineshop.enums.Sorting;
import onlineshop.merchandise.Book;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Manages the Shop
 */
@SpringBootApplication
public class Shop {
    public static final DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private final static Logger log = LogManager.getLogger(Shop.class);
    private final static List<Book> books = new ArrayList<>(220);

    public static void main(String[] args) {
        readArticles("src/main/resources/books.csv", books);
        SpringApplication.run(Shop.class, args);
        log.info("Server started on http://localhost:8080");
    }

    public List<Book> getArticles() {
        return books;
    }

    /**
     * Returns a sublist of articles
     * @param from {@link Integer}
     * @param to {@link Integer}
     * @return articlesSublist {@link List<Book>}
     */
    public List<Book> getArticles(Sorting sorting, int from, int to) {
        sortArticles(sorting);
        return books.subList(from, to);
    }

    private void sortArticles(Sorting sorting) {
        switch (sorting) {
            case DEFAULT:
            case ALPHA_UP:
                books.sort(Comparator.comparing(Book::getTitle)); break;
            case ALPHA_DOWN:
                books.sort(Comparator.comparing(Book::getTitle).reversed()); break;
            case PRICE_UP:
                books.sort(Comparator.comparing(Book::getPrice)); break;
            case PRICE_DOWN:
                books.sort(Comparator.comparing(Book::getPrice).reversed()); break;
/*
            case AUTHOR_UP:
                books.sort(Comparator.comparing(Book::getAuthor)); break;
            case AUTHOR_DOWN:
                books.sort(Comparator.comparing(Book::getAuthor).reversed()); break;
*/
        }
    }

    public int getNumOfArticles() {
        return books.size();
    }

    /**
     * Read articles from a CSV file
     * @param fileName {@link String}
     * @param bookList    {@link List}
     */
    private static void readArticles(String fileName, List<Book> bookList) {
        try {
            Reader in = new FileReader(fileName);
            CSVFormat csvFormat = CSVFormat.EXCEL.withFirstRecordAsHeader().builder()
                    .setDelimiter(';')
                    .build();
            Iterable<CSVRecord> records = csvFormat.parse(in);

            for (CSVRecord record : records) {
                String title = record.get("title");
                String author = record.get("author");
                String publisher = record.get("publisher");
                String genre = record.get("genre");

                // TODO: random price between 5 and 20$
                double price = 9.99;
                String priceString = record.get("price");
                if (!priceString.isEmpty()) {
                    price = Double.parseDouble(priceString);
                }

                String image = record.get("image");
                if (image.isEmpty()) image = "/images/book-placeholder.png";
                Book book = new Book(title, author, publisher, genre, price, image);
                bookList.add(book);
            }
            in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("{} bookList imported", bookList.size());
    }

    /**
     * Gets a /book by its article number
     *
     * @param articleNo {@link Integer}
     * @return existingBook {@link Book}
     */
    public Book getArticleByNumber(int articleNo) {
        for (Book book : books) {
            if (book.getArticleNo() == articleNo)
                return book;
        }
        return null;
    }
}
